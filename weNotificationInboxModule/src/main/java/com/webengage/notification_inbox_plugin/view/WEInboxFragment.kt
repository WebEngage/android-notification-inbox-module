package com.webengage.notification_inbox_plugin.view

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.notification_inbox_plugin.R
import com.webengage.notification_inbox_plugin.WEInboxModule
import com.webengage.notification_inbox_plugin.interfaces.WEInboxResponseCallback
import com.webengage.notification_inbox_plugin.repository.WEInboxDataRepository
import com.webengage.notification_inbox_plugin.utils.WENotificationInboxConstants
import com.webengage.notification.inbox.WENotificationInbox
import com.webengage.notification.inbox.data.models.WEInboxData
import com.webengage.notification.inbox.data.models.WEInboxMessage
import com.webengage.sdk.android.Logger

class WEInboxFragment : Fragment(), WebEngageRecyclerViewAdapter.WEInboxItemClick,
    WebEngageRecyclerViewAdapter.WEInboxItemDelete,
    WebEngageRecyclerViewAdapter.WEInboxItemRead,
    WebEngageRecyclerViewAdapter.WEInboxItemUnread, WEInboxResponseCallback {

    private var shouldRender = false
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: WebEngageRecyclerViewAdapter? = null
    private var mNotificationList: MutableList<WEInboxMessage>? = null
    private var mContext: Context? = null
    private var layoutManager: LinearLayoutManager? = null
    private lateinit var loadProgressBar: ProgressBar
    private var hasNext = true
    private var loading = false
    private var mErrorTextView: TextView? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var mErrorImageView: ImageView? = null
    val visitedPositions = HashSet<Int>()
    private lateinit var menuItem: MenuItem
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Disabled rotation for the hosting activity
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        // Resets Notification Count
        WENotificationInbox.get(this.requireContext()).onNotificationIconClick()

        val view: View = inflater.inflate(R.layout.fragment_we_inbox, container, false)
        mContext = this.context
        initViews(view)
        initErrorView(view)
        initData()
        initListeners()
        return view
    }


    override fun onStart() {
        super.onStart()
        shouldRender = true
    }

    override fun onStop() {
        super.onStop()
        shouldRender = false
    }

    private fun initListeners() {
        mAdapter!!.setClickListener(this)
        mAdapter!!.setReadListener(this)
        mAdapter!!.setUnreadListener(this)
        mAdapter!!.setDeleteListener(this)

        swipeRefreshLayout?.setOnRefreshListener {
            mNotificationList?.clear()
            mAdapter?.notifyDataSetChanged()
            mErrorImageView?.visibility = View.GONE
            mErrorTextView?.visibility = View.GONE
            initData()
        }

        mRecyclerView?.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val firstVisiblePosition = layoutManager!!.findFirstVisibleItemPosition()
                    val lastVisiblePosition = layoutManager!!.findLastVisibleItemPosition()
                    for (position in firstVisiblePosition..lastVisiblePosition) {
                        if (!visitedPositions.contains(position)) {
                            visitedPositions.add(position)
                            onItemView(position)
                        }
                    }
                    if ((mNotificationList!!.size > 0) && lastVisiblePosition != 0 && lastVisiblePosition > mNotificationList!!.size - 3 && hasNext && !loading) {
                        // Fetching Next set of Data
                        loading = true
                        loadProgressBar.visibility = View.VISIBLE
                        getNotificationDataOnRefresh(
                            mNotificationList!![mNotificationList!!.size - 1]
                        )
                    }
                }
            }
        )

        // Swipe to delete feature - UnComment to enable the feature
        // val itemTouchHelper = ItemTouchHelper(SwipeToDeleteCallback( this))
        // itemTouchHelper.attachToRecyclerView(mRecyclerView)
    }

    // More Menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.actionbar_layout, menu)
        menuItem = menu.findItem(R.id.more_menu)
        menuItem.setIcon(R.drawable.three_dots)
        menuItem.isVisible = mNotificationList?.size != 0
        requireActivity().title = getString(R.string.module_title)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.more_menu -> {
                displayPopMenu()
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // pop menu for ReadAll/DeleteAll
    private fun displayPopMenu(): Boolean  {
        val anchorView = requireActivity().findViewById<View>(R.id.more_menu)
        val popupMenu = PopupMenu(requireContext(), anchorView)
        popupMenu.menuInflater.inflate(R.menu.more_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.read_all -> {
                    readAll()
                    true
                }
                R.id.bulk_delete -> {
                    deleteAll()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
        return true
    }

    // Mark All unRead Items as Read
    private fun readAll() {
        for (position in 0 until mNotificationList?.size!!) {
            val itemStatus = mNotificationList!![position].status
            if (itemStatus == WENotificationInboxConstants.UNREAD) {
                onItemRead(null, position)
            }
        }
    }

    // Delete All Items
    private fun deleteAll() {
        val lastItem = mNotificationList!![mNotificationList!!.size - 1]
        for (position in mNotificationList?.size?.minus(1)!! downTo 0) {
            onItemDelete(null, position)
        }
        loadProgressBar.visibility = View.VISIBLE
        if(hasNext) {
            // Fetch next items if any further items are available
            getNotificationDataOnRefresh(lastItem)
        } else {
            displayNoNotificationListView()
        }
    }

    // Triggered Once when a view is visible in the viewport
    private fun onItemView(position: Int) {
        if (mNotificationList != null && mNotificationList?.size!! > 0) {
            mNotificationList!![position].trackView()
        }
    }

    // no notification and error in API/Network
    private fun initErrorView(view: View) {
        mErrorTextView = view.findViewById(R.id.we_notification_inbox_fragment_tv)
        mErrorImageView = view.findViewById(R.id.we_notification_inbox_fragment_image)
        mErrorTextView?.visibility = View.GONE
        mErrorImageView?.visibility = View.GONE
    }

    private fun initViews(view: View) {
        mNotificationList = ArrayList()
        swipeRefreshLayout =
            view.findViewById(R.id.we_notification_inbox_fragment_swipeRefresh)
        mRecyclerView =
            view.findViewById(R.id.we_notification_inbox_fragment_recyclerView)
        mRecyclerView?.layoutManager = LinearLayoutManager(mContext)
        loadProgressBar = view.findViewById(R.id.loaderImageView)
        loadProgressBar.visibility = View.GONE
        val latestAdapter =
            WEInboxModule.get().getWEAdapter()  // default- WEHolder or your CustomViewHolder
        mAdapter = WebEngageRecyclerViewAdapter(
            latestAdapter,
            mNotificationList as ArrayList<WEInboxMessage>
        )
        layoutManager = mRecyclerView?.layoutManager as LinearLayoutManager?
        mRecyclerView?.adapter = mAdapter
        mRecyclerView?.visibility = View.GONE
    }

    private fun getNotificationDataOnRefresh(weInboxMessage: WEInboxMessage) {
        if (mContext != null) {
            WEInboxDataRepository.getNotificationList(mContext!!, weInboxMessage, this)
        }
    }

    private fun initData() {
        mRecyclerView!!.visibility = View.GONE
        swipeRefreshLayout?.isRefreshing = true
        if (mContext != null) {
            WEInboxDataRepository.getNotificationList(mContext!!, this)
        }
    }

    private fun configureAdapter(messageList: List<WEInboxMessage>) {
        mNotificationList!!.addAll(messageList)
        val currentSizeInAdapter = mNotificationList?.size as Int
        val newNumberOfItemsAdding = messageList.size
        mAdapter!!.notifyItemRangeInserted(currentSizeInAdapter, newNumberOfItemsAdding)
        loading = false
        loadProgressBar.visibility = View.GONE
    }

    private fun displayNoNotificationListView() {
        menuItem.isVisible = false
        mErrorTextView!!.text = mContext?.getString(R.string.no_new_Notification)
        mErrorImageView!!.setImageDrawable(
            ResourcesCompat.getDrawable(
                mContext?.resources!!, R.drawable.bell, null
            )
        )
        mErrorImageView!!.visibility = View.VISIBLE
        mErrorTextView!!.visibility = View.VISIBLE
        loadProgressBar.visibility = View.GONE
    }

    override fun onItemClick(view: View?, position: Int) {
        mNotificationList!![position].trackClick()
    }

    override fun onItemRead(view: View?, position: Int) {
        mNotificationList!![position].markRead()
        mNotificationList!![position].status = WENotificationInbox.STATUS.READ.name
        mAdapter!!.notifyItemChanged(position, mNotificationList!![position])
    }

    override fun onItemUnread(view: View?, position: Int) {
        mNotificationList!![position].markUnread()
        mNotificationList!![position].status = WENotificationInbox.STATUS.UNREAD.name
        mAdapter!!.notifyItemChanged(position, mNotificationList!![position])
    }

    override fun onItemDelete(view: View?, position: Int) {
        mNotificationList!![position].markDelete()
        mNotificationList!!.removeAt(position)
        mAdapter!!.notifyItemRemoved(position)
    }

    override fun onSuccess(weInboxData: WEInboxData) {
        swipeRefreshLayout?.isRefreshing = false
        mRecyclerView!!.visibility = View.VISIBLE
        if (shouldRender) {
            loading = false
            loadProgressBar.visibility = View.GONE
            hasNext = weInboxData.hasNext
            if (weInboxData.messageList.isNotEmpty()) {
                menuItem.isVisible = true
                configureAdapter(weInboxData.messageList)
            } else {
                if (mNotificationList?.size == 0) {
                    displayNoNotificationListView()
                }
            }
        }
    }

    override fun onFailure(errorCode: Int, error: Map<String, Any?>) {
        loading = false
        loadProgressBar.visibility = View.GONE
        swipeRefreshLayout?.isRefreshing = false
        if (shouldRender) {
            mErrorImageView!!.setImageDrawable(
                ResourcesCompat.getDrawable(
                    mContext?.resources!!, R.drawable.danger, null
                )
            )
            mErrorImageView!!.visibility = View.VISIBLE
            Logger.e(
                WENotificationInboxConstants.TAG,
                "Notification-Inbox List errorCode : $errorCode\nerrorMessage : $error"
            )
            menuItem.isVisible = false
            mErrorTextView!!.visibility = View.VISIBLE
            mErrorTextView!!.text = mContext?.getString(R.string.something_went_wrong)
        }
    }
}