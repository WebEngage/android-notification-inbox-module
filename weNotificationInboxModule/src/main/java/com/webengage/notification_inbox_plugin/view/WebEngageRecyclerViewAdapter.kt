package com.webengage.notification_inbox_plugin.view

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.webengage.notification_inbox_plugin.interfaces.WEInboxAdapterInterface
import com.webengage.notification.inbox.data.models.PushNotificationTemplateData
import com.webengage.notification.inbox.data.models.WEInboxMessage
import com.webengage.notification_inbox_plugin.utils.WENotificationInboxConstants

class WebEngageRecyclerViewAdapter(
    weAdapter: WEInboxAdapterInterface,
    list: ArrayList<WEInboxMessage>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val mWEInboxAdapterInterface: WEInboxAdapterInterface
    private val mNotificationList: ArrayList<WEInboxMessage> = list
    private var weInboxItemClick: WEInboxItemClick? = null
    private var weInboxItemRead: WEInboxItemRead? = null
    private var weInboxItemUnread: WEInboxItemUnread? = null
    private var weInboxItemDelete: WEInboxItemDelete? = null

    init {
        mWEInboxAdapterInterface = weAdapter
    }

    override fun getItemViewType(position: Int): Int {
        if (mNotificationList.size > 0) {
            val weInboxMessage: WEInboxMessage = mNotificationList[position]
            val pushNotificationTemplateData: PushNotificationTemplateData =
                weInboxMessage.message as PushNotificationTemplateData
            val layoutType =
                pushNotificationTemplateData.messageMap?.get(WENotificationInboxConstants.LAYOUT_TYPE)
            if (layoutType != null) {
                // 0-banner  |  1-text  |  2-Carousel  |  3-Rating
                return when (layoutType) {
                    WENotificationInboxConstants.BANNER -> {
                        WENotificationInboxConstants.VIEW_TYPE_BANNER
                    }

                    WENotificationInboxConstants.RATING -> {
                        WENotificationInboxConstants.VIEW_TYPE_RATING
                    }

                    WENotificationInboxConstants.CAROUSEL -> {
                        WENotificationInboxConstants.VIEW_TYPE_CAROUSEL
                    }

                    WENotificationInboxConstants.TEXT -> {
                        WENotificationInboxConstants.VIEW_TYPE_TEXT
                    }

                    else -> {
                        // Clients can add dynamic view type when there is new type
                        mWEInboxAdapterInterface.getViewType(mNotificationList, position)
                    }
                }
            }
        }
        return super.getItemViewType(position)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return mWEInboxAdapterInterface.createViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        mWEInboxAdapterInterface.onBindViewHolder(holder, mNotificationList, position)
        // Pass registered listeners to child
        if (weInboxItemRead != null) {
            mWEInboxAdapterInterface.getReadListener(weInboxItemRead)
        }

        if (weInboxItemUnread != null) {
            mWEInboxAdapterInterface.getUnReadListener(weInboxItemUnread)
        }

        if (weInboxItemClick != null) {
            mWEInboxAdapterInterface.getClickListener(weInboxItemClick)
        }

        if (weInboxItemDelete != null) {
            mWEInboxAdapterInterface.getDeleteListener(weInboxItemDelete)
        }
    }

    // RecyclerView - Adapter method to pass list size
    override fun getItemCount(): Int {
        return mNotificationList.size
    }


    interface WEInboxItemClick {
        fun onItemClick(view: View?, position: Int)
    }

    interface WEInboxItemRead {
        fun onItemRead(view: View?, position: Int)
    }

    interface WEInboxItemUnread {
        fun onItemUnread(view: View?, position: Int)
    }

    interface WEInboxItemDelete {
        fun onItemDelete(view: View?, position: Int)
    }

    fun setClickListener(itemClickListener: WEInboxItemClick?) {
        weInboxItemClick = itemClickListener
    }

    fun setReadListener(itemReadListener: WEInboxItemRead?) {
        weInboxItemRead = itemReadListener
    }

    fun setUnreadListener(itemUnreadListener: WEInboxItemUnread?) {
        weInboxItemUnread = itemUnreadListener
    }

    fun setDeleteListener(itemDeleteListener: WEInboxItemDelete?) {
        weInboxItemDelete = itemDeleteListener
    }

}