package com.webengage.notification_inbox_plugin.view

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.webengage.notification_inbox_plugin.interfaces.WEInboxAdapterInterface
import com.bumptech.glide.Glide
import com.example.notification_inbox_plugin.R
import com.webengage.notification_inbox_plugin.utils.Utils
import com.webengage.notification_inbox_plugin.utils.WENotificationInboxConstants
import com.webengage.notification.inbox.WENotificationInbox
import com.webengage.notification.inbox.data.models.PushNotificationTemplateData
import com.webengage.notification.inbox.data.models.WEInboxMessage
import com.webengage.sdk.android.Logger
import com.webengage.sdk.android.utils.htmlspanner.WEHtmlParserInterface


open class
WEInboxAdapter : WEInboxAdapterInterface() {

    var mContext: Context? = null
    private var mWEInboxDataList: List<WEInboxMessage>? = null
    private var mWeInboxItemClick: WebEngageRecyclerViewAdapter.WEInboxItemClick? = null
    private var mWeInboxItemRead: WebEngageRecyclerViewAdapter.WEInboxItemRead? = null
    private var mWeInboxItemUnread: WebEngageRecyclerViewAdapter.WEInboxItemUnread? = null
    private var mWeInboxItemDelete: WebEngageRecyclerViewAdapter.WEInboxItemDelete? = null

    override fun createViewHolder(parent: ViewGroup, type: Int): RecyclerView.ViewHolder {
        mContext = parent.context

        when (type) {
            WENotificationInboxConstants.VIEW_TYPE_TEXT -> return ViewHolderTextObject(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_we_text_inbox, parent, false)
            )

            WENotificationInboxConstants.VIEW_TYPE_BANNER -> return ViewHolderBannerObject(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_we_banner_inbox, parent, false)
            )

            else -> return ViewHolderTextObject(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_we_text_inbox, parent, false)
            )

        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        mNotificationList: ArrayList<WEInboxMessage>,
        position: Int
    ) {
        mWEInboxDataList = mNotificationList
        if (mWEInboxDataList?.size!! > 0) {
            val weInboxMessage: WEInboxMessage = mWEInboxDataList!![position]
            when (holder) {
                // ViewType = Text
                is ViewHolderTextObject -> {
                    renderTextViewType(holder, position, weInboxMessage)
                }

                // ViewType = Banner
                is ViewHolderBannerObject -> {
                    renderBannerViewType(holder, weInboxMessage, position)

                }
            }
        }
    }


    private fun renderTextViewType(
        holder: ViewHolderTextObject,
        position: Int,
        weInboxMessage: WEInboxMessage
    ) {
        val pushNotificationTemplateData =
            mWEInboxDataList?.get(position)?.message as PushNotificationTemplateData
        val weHtmlParserInterface = WEHtmlParserInterface()

        if (weInboxMessage.channelType == WENotificationInbox.CHANNEL.PUSH_NOTIFICATION.name) {
            renderTitle(holder.mTitleTextView, pushNotificationTemplateData, weHtmlParserInterface)
            renderDescription(
                holder.mMessageTextView,
                weHtmlParserInterface,
                pushNotificationTemplateData,
            )
            showReceivedTimer(weInboxMessage, holder.mTimeStamp, WENotificationInboxConstants.TEXT)
            renderIcons(
                weInboxMessage,
                holder.mReadButton,
                holder.mUnReadButton,
                holder.mContainer,
                holder.itemView
            )
            addButtonListeners(
                holder.mReadButton,
                holder.mUnReadButton,
                holder.mDeleteButton,
                holder.mContainer,
                position
            )
        } else {
            displayUnsupportedChannel(
                holder.mTitleTextView,
                holder.mMessageTextView,
                holder.mReadButton,
                holder.mUnReadButton,
                holder.mDeleteButton,
                holder.mTimeStamp,
                null
            )
        }
    }

    private fun renderBannerViewType(
        holder: ViewHolderBannerObject,
        weInboxMessage: WEInboxMessage,
        position: Int
    ) {
        val weHtmlParserInterface = WEHtmlParserInterface()
        if (weInboxMessage.channelType == WENotificationInbox.CHANNEL.PUSH_NOTIFICATION.name) {
            val pushNotificationTemplateData: PushNotificationTemplateData =
                weInboxMessage.message as PushNotificationTemplateData
            renderImageView(pushNotificationTemplateData, holder)
            renderTitle(holder.mTitleTextView, pushNotificationTemplateData, weHtmlParserInterface)
            renderDescription(
                holder.mMessageTextView,
                weHtmlParserInterface,
                pushNotificationTemplateData,
            )
            showReceivedTimer(weInboxMessage, holder.mTimeStamp, WENotificationInboxConstants.BANNER)
            renderIcons(
                weInboxMessage,
                holder.mReadButton,
                holder.mUnReadButton,
                holder.mContainer,
                holder.itemView
            )
            addButtonListeners(
                holder.mReadButton,
                holder.mUnReadButton,
                holder.mDeleteButton,
                holder.mContainer,
                position
            )

        } else {
            displayUnsupportedChannel(
                holder.mTitleTextView,
                holder.mMessageTextView,
                holder.mReadButton,
                holder.mUnReadButton,
                holder.mDeleteButton,
                holder.mTimeStamp,
                holder.mImageView,
            )
        }
    }



    private fun renderTitle(
        titleView: TextView,
        pushNotificationTemplateData: PushNotificationTemplateData,
        weHtmlParserInterface: WEHtmlParserInterface
    ) {
        titleView.text = weHtmlParserInterface.fromHtml(
            pushNotificationTemplateData.title
        )
    }

    private fun renderDescription(
        descriptionView: TextView,
        weHtmlParserInterface: WEHtmlParserInterface,
        pushNotificationTemplateData: PushNotificationTemplateData,
    ) {
        if (pushNotificationTemplateData.description != null) descriptionView.text =
            weHtmlParserInterface.fromHtml((pushNotificationTemplateData.description)) else descriptionView.visibility =
            View.GONE
    }

    private fun renderImageView(
        pushNotificationTemplateData: PushNotificationTemplateData,
        holder: ViewHolderBannerObject
    ) {
        if (pushNotificationTemplateData.messageMap?.get(WENotificationInboxConstants.ANDROID_DETAILS) != null) {
            val androidDetailsMap: HashMap<String, Any?>? =
                pushNotificationTemplateData.messageMap?.get(WENotificationInboxConstants.ANDROID_DETAILS) as? HashMap<String, Any?>?
            try {
                if (androidDetailsMap != null && androidDetailsMap[WENotificationInboxConstants.EXPANDABLE_DETAILS] != null) {
                    val expandableDetails =
                        androidDetailsMap[WENotificationInboxConstants.EXPANDABLE_DETAILS] as? HashMap<String, Any>?
                    if (expandableDetails != null && (expandableDetails[WENotificationInboxConstants.STYLE] == WENotificationInboxConstants.BIG_PICTURE)) {
                        holder.mImageView.visibility = View.VISIBLE
                        if (mContext != null) {
                            Glide.with(mContext!!).load(expandableDetails[WENotificationInboxConstants.IMAGE])
                                .into(holder.mImageView)
                        }
                        holder.mImageView.visibility = View.VISIBLE
                    } else {
                        holder.mImageView.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                Logger.e(
                    WENotificationInboxConstants.TAG,
                    "Exception occurred while rendering Image - " + e.printStackTrace()
                )
            }
        }
    }


    private fun renderIcons(
        weInboxMessage: WEInboxMessage,
        readButton: Button,
        unReadButton: Button,
        container: LinearLayoutCompat,
        itemView: View
    ) {
        if (weInboxMessage.status.equals(WENotificationInboxConstants.READ, ignoreCase = true)) {
            readButton.visibility = View.GONE
            unReadButton.visibility = View.VISIBLE
            container.setBackgroundColor(
                container.resources.getColor(
                    R.color.white,
                    itemView.context.theme
                )
            )
        } else {
            readButton.visibility = View.VISIBLE
            unReadButton.visibility = View.GONE
            container.setBackgroundColor(
                container.resources.getColor(
                    R.color.logged_in,
                    itemView.context.theme
                )
            )
        }
    }

    private fun showReceivedTimer(
        weInboxMessage: WEInboxMessage,
        timeView: TextView,
        viewType: String
    ) {
        val formattedTimeStamp: String =
            (Utils.formatTimeStamp(weInboxMessage.creationTime, viewType))

        if (!TextUtils.isEmpty(formattedTimeStamp)) {
            timeView.visibility = View.VISIBLE
            timeView.text = formattedTimeStamp
        } else {
            timeView.visibility = View.GONE
        }
    }

    private fun addButtonListeners(
        readButton: Button,
        unReadButton: Button,
        deleteButton: Button,
        container: LinearLayoutCompat,
        position: Int
    ) {
        readButton.setOnClickListener {
            markAsRead(position)
        }

        unReadButton.setOnClickListener {
            markAsUnRead(position)
        }
        deleteButton.setOnClickListener {
            deleteItem(position)
        }
        container.setOnClickListener {
            trackClick(position)
        }
    }

    private fun displayUnsupportedChannel(
        titleTextView: AppCompatTextView,
        messageTextView: AppCompatTextView,
        readButton: Button,
        unReadButton: Button,
        deleteButton: Button,
        timeStamp: AppCompatTextView,
        imageView: ImageView?,
    ) {
        titleTextView.text = mContext?.getString(R.string.push_support_title)
        messageTextView.text = mContext?.getString(R.string.push_support_description)
        readButton.visibility = View.GONE
        unReadButton.visibility = View.GONE
        deleteButton.visibility = View.GONE
        timeStamp.visibility = View.GONE
        imageView?.visibility = View.GONE
    }


    private fun markAsRead(position: Int) {
        if (mWeInboxItemRead != null) {
            mWeInboxItemRead!!.onItemRead(null, position)
        }
    }

    private fun markAsUnRead(position: Int) {
        if (mWeInboxItemUnread != null) {
            mWeInboxItemUnread!!.onItemUnread(null, position)
        }
    }

    private fun trackClick(position: Int) {
        if (mWeInboxItemClick != null) {
            mWeInboxItemClick!!.onItemClick(null, position)
        }
    }

    private fun deleteItem(position: Int) {
        if (mWeInboxItemDelete != null) {
            mWeInboxItemDelete!!.onItemDelete(null, position)
        }
    }

    override fun getViewType(notificationList: ArrayList<WEInboxMessage>, position: Int): Int {
        // only called if unsupported type appears.WebEngage by default passes Text type
        return WENotificationInboxConstants.VIEW_TYPE_TEXT
    }
    override fun getReadListener(weInboxItemRead: WebEngageRecyclerViewAdapter.WEInboxItemRead?) {
        mWeInboxItemRead = weInboxItemRead
    }

    override fun getUnReadListener(weInboxItemUnread: WebEngageRecyclerViewAdapter.WEInboxItemUnread?) {
        mWeInboxItemUnread = weInboxItemUnread
    }

    override fun getClickListener(weInboxItemClick: WebEngageRecyclerViewAdapter.WEInboxItemClick?) {
        mWeInboxItemClick = weInboxItemClick
    }

    override fun getDeleteListener(weInboxItemDelete: WebEngageRecyclerViewAdapter.WEInboxItemDelete?) {
        mWeInboxItemDelete = weInboxItemDelete
    }

    open class ViewHolderTextObject(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mTitleTextView: AppCompatTextView =
            itemView.findViewById(R.id.we_notification_inbox_text_title)
        val mMessageTextView: AppCompatTextView =
            itemView.findViewById(R.id.we_notification_inbox_text_message)
        val mReadButton: Button =
            itemView.findViewById(R.id.we_notification_inbox_text_markReadButton)
        val mUnReadButton: Button =
            itemView.findViewById(R.id.we_notification_inbox_text_markUnreadButton)
        val mDeleteButton: Button =
            itemView.findViewById(R.id.we_notification_inbox_text_deleteButton)
        val mContainer: LinearLayoutCompat = itemView.findViewById(R.id.text_container)
        val mTimeStamp: AppCompatTextView =
            itemView.findViewById(R.id.we_notification_inbox_text_time)

    }

    class ViewHolderBannerObject(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mTitleTextView: AppCompatTextView =
            itemView.findViewById(R.id.we_notification_inbox_banner_title)
        val mMessageTextView: AppCompatTextView =
            itemView.findViewById(R.id.we_notification_inbox_banner_message)
        val mImageView: ImageView =
            itemView.findViewById(R.id.we_notification_inbox_banner_Image)
        val mReadButton: Button =
            itemView.findViewById(R.id.we_notification_inbox_banner_markReadButton)
        val mUnReadButton: Button =
            itemView.findViewById(R.id.we_notification_inbox_banner_markUnreadButton)
        val mDeleteButton: Button =
            itemView.findViewById(R.id.we_notification_inbox_banner_deleteButton)
        val mContainer: LinearLayoutCompat = itemView.findViewById(R.id.banner_container)
        val mTimeStamp: AppCompatTextView =
            itemView.findViewById(R.id.we_notification_inbox_banner_time)
    }


}