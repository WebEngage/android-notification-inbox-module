package com.webengage.test

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.webengage.notification_inbox_plugin.view.WebEngageRecyclerViewAdapter
import com.bumptech.glide.Glide
import com.webengage.notification_inbox_plugin.utils.Utils
import com.webengage.notification_inbox_plugin.view.WEInboxAdapter
import com.webengage.notification_inbox_plugin.utils.WENotificationInboxConstants
import com.webengage.notification.inbox.data.models.PushNotificationTemplateData
import com.webengage.notification.inbox.data.models.WEInboxMessage
import com.webengage.sdk.android.Logger
import com.webengage.sdk.android.utils.htmlspanner.WEHtmlParserInterface

class CustomWEInboxAdapter : WEInboxAdapter() {

    private var mWEInboxDataList: List<WEInboxMessage>? = null
    private var mWeInboxItemRead: WebEngageRecyclerViewAdapter.WEInboxItemRead? = null
    private var mWeInboxItemUnread: WebEngageRecyclerViewAdapter.WEInboxItemUnread? = null
    private var mWeInboxItemDelete: WebEngageRecyclerViewAdapter.WEInboxItemDelete? = null


    //    Choose your Layout based on the VIEW_TYPE
    override fun createViewHolder(parent: ViewGroup, type: Int): ViewHolder {
        mContext = parent.context
        // 0-banner  |  1-text  |  2-Carousel  |  3-Rating
        return when (type) {
            // click on the below layout to watch the view design layout
            WENotificationInboxConstants.VIEW_TYPE_TEXT -> return CustomViewHolderTextObject(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.custom_we_text_inbox, parent, false)
            )

            WENotificationInboxConstants.VIEW_TYPE_CAROUSEL -> return CustomViewHolderCarouselObject(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.custom_we_carousel_inbox, parent, false)
            )
            // Return default viewHolder for other ViewType which you have not override
            else ->
                super.createViewHolder(parent, type)
        }

    }

    // Binds the data to the ViewHolder in the RecyclerView
    override fun onBindViewHolder(
        holder: ViewHolder,
        mNotificationList: ArrayList<WEInboxMessage>,
        position: Int
    ) {
        super.onBindViewHolder(holder, mNotificationList, position)
        mWEInboxDataList = mNotificationList
        if (mWEInboxDataList != null && mWEInboxDataList?.size!! > 0) {
            val weInboxMessage: WEInboxMessage = mWEInboxDataList!![position]
            // Updating only Text and Carousel Type - Banner/Rating has default views
            when (holder) {
                is CustomViewHolderTextObject -> {
                    bindCustomText(holder, weInboxMessage, position)
                }

                is CustomViewHolderCarouselObject -> {
                    bindCustomCarousel(holder, position)
                }
            }
        }
    }

    private fun bindCustomCarousel(holder: CustomViewHolderCarouselObject, position: Int) {
        val pushNotificationTemplateData =
            mWEInboxDataList?.get(position)?.message as PushNotificationTemplateData
        // Title
        renderTitle(holder.carouselTitle, pushNotificationTemplateData.title)
        // Description
        renderDescription(holder.carouselMessage, pushNotificationTemplateData.description)
        //Image
        renderCarouselImage(pushNotificationTemplateData, holder)
    }


    private fun bindCustomText(
        holder: CustomViewHolderTextObject,
        weInboxMessage: WEInboxMessage,
        position: Int
    ) {
        val pushNotificationTemplateData =
            mWEInboxDataList?.get(position)?.message as PushNotificationTemplateData
        // Title
        renderTitle(holder.customTitle, pushNotificationTemplateData.title)
        // Description
        renderDescription(holder.customMessage, pushNotificationTemplateData.description)
        // Time
        renderMessageReceivedTimer(holder.customArrivalTime, weInboxMessage)
        // Display / Hide Read or UnRead Buttons
        renderTextIcons(weInboxMessage, holder)
        //ButtonListeners
        addButtonListeners(
            holder.customReadButton,
            holder.customUnReadButton,
            holder.customDeleteButton,
            position
        )

    }

    private fun renderTitle(customTitle: TextView, title: String?) {
        customTitle.text =
            WEHtmlParserInterface().fromHtml(title)
    }

    private fun renderDescription(descriptionView: TextView, descriptionText: String?) {
        if (descriptionView != null) descriptionView.text =
            WEHtmlParserInterface().fromHtml((descriptionText)) else descriptionView.visibility =
            View.GONE
    }

    private fun renderMessageReceivedTimer(timerView: TextView, weInboxMessage: WEInboxMessage) {
        val formattedArrivalTime: String? =
            (Utils.formatTimeStamp(
                weInboxMessage.creationTime,
                WENotificationInboxConstants.TEXT
            ))
        if (!TextUtils.isEmpty(formattedArrivalTime)) {
            timerView.visibility = View.VISIBLE
            timerView.text = formattedArrivalTime
        } else {
            timerView.visibility = View.GONE
        }
    }

    // Image rendering
    private fun renderCarouselImage(
        pushNotificationTemplateData: PushNotificationTemplateData,
        holder: ViewHolder
    ) {
        holder as CustomViewHolderCarouselObject
        if (pushNotificationTemplateData.messageMap?.get(WENotificationInboxConstants.ANDROID_DETAILS) != null) {
            val androidDetailsMap: HashMap<String, Any?>? =
                pushNotificationTemplateData.messageMap?.get(WENotificationInboxConstants.ANDROID_DETAILS) as HashMap<String, Any?>?
            try {
                if (androidDetailsMap != null && androidDetailsMap[WENotificationInboxConstants.EXPANDABLE_DETAILS] != null) {
                    val expandableDetails =
                        androidDetailsMap[WENotificationInboxConstants.EXPANDABLE_DETAILS] as HashMap<String, Any>?
                    val expandableItems =
                        expandableDetails?.get(WENotificationInboxConstants.ITEMS) as ArrayList<Any>?
                    val firstItem = expandableItems?.get(0) as HashMap<String, Any>?
                    val imageUri = firstItem?.get(WENotificationInboxConstants.IMAGE)
                    if (mContext != null) {
                        Glide.with(mContext!!).load(imageUri)
                            .into(holder.carouselImage)
                        holder.carouselImage.visibility = View.VISIBLE
                    } else {
                        holder.carouselImage.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                Logger.e(
                    WENotificationInboxConstants.TAG,
                    "Exception occurred while rendering Image  " + e.printStackTrace()
                )
            }
        }
    }

    private fun renderTextIcons(
        weInboxMessage: WEInboxMessage,
        holder: CustomViewHolderTextObject
    ) {
        if (weInboxMessage.status.equals(WENotificationInboxConstants.READ, ignoreCase = true)) {
            holder.customReadButton.visibility = View.GONE
            holder.customUnReadButton.visibility = View.VISIBLE
            holder.customContainer.setBackgroundColor(holder.customContainer.resources.getColor(R.color.white))
        } else {
            holder.customReadButton.visibility = View.VISIBLE
            holder.customUnReadButton.visibility = View.GONE
            holder.customContainer.setBackgroundColor(holder.customContainer.resources.getColor(R.color.logged_in))
        }
    }

    private fun addButtonListeners(
        readButton: Button,
        unReadButton: Button,
        deleteButton: Button,
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

    private fun deleteItem(position: Int) {
        if (mWeInboxItemDelete != null) {
            mWeInboxItemDelete!!.onItemDelete(null, position)
        }
    }

    // Sets the listener for handling read events of the WEInbox items in the RecyclerView.
    override fun getReadListener(weInboxItemRead: WebEngageRecyclerViewAdapter.WEInboxItemRead?) {
        mWeInboxItemRead = weInboxItemRead
        super.getReadListener(weInboxItemRead)
    }

    override fun getUnReadListener(weInboxItemUnread: WebEngageRecyclerViewAdapter.WEInboxItemUnread?) {
        mWeInboxItemUnread = weInboxItemUnread
        super.getUnReadListener(weInboxItemUnread)
    }

    override fun getDeleteListener(weInboxItemDelete: WebEngageRecyclerViewAdapter.WEInboxItemDelete?) {
        mWeInboxItemDelete = weInboxItemDelete
        super.getDeleteListener(weInboxItemDelete)
    }

    // Called only when new type available other than Banner,text,carousel and survey
    override fun getViewType(notificationList: ArrayList<WEInboxMessage>, position: Int): Int {
        if (notificationList.size > 0) {
            val weInboxMessage: WEInboxMessage = notificationList[position]
            val pushNotificationTemplateData: PushNotificationTemplateData =
                weInboxMessage.message as PushNotificationTemplateData
            val layoutType =
                pushNotificationTemplateData.messageMap?.get(WENotificationInboxConstants.LAYOUT_TYPE)
            if (layoutType != null) {
                if (layoutType == WENotificationInboxConstants.BANNER) {
                    // Choose any numbers, Where number >= 4
                    val newType = 4
                    return newType
                }
            }
        }
        return super.getViewType(notificationList, position)
    }


    // Custom ViewHolder for Text Type
    class CustomViewHolderTextObject(itemView: View) : ViewHolder(itemView) {
        val customTitle: TextView =
            itemView.findViewById(R.id.we_notification_custom_inbox_text_title)
        val customMessage: TextView =
            itemView.findViewById(R.id.we_notification_custom_inbox_text_message)
        val customReadButton: Button =
            itemView.findViewById(R.id.we_notification_custom_inbox_text_markReadButton)
        val customUnReadButton: Button =
            itemView.findViewById(R.id.we_notification_custom_inbox_text_markUnreadButton)
        val customDeleteButton: Button =
            itemView.findViewById(R.id.we_notification_custom_inbox_text_deleteButton)
        val customContainer: LinearLayout =
            itemView.findViewById(R.id.we_notification_custom_text_container)
        val customArrivalTime: TextView =
            itemView.findViewById(R.id.we_notification_custom_inbox_text_time)
    }

    // Custom ViewHolder for Carousel Type
    class CustomViewHolderCarouselObject(itemView: View) : ViewHolder(itemView) {
        val carouselTitle: TextView =
            itemView.findViewById(R.id.we_notification_custom_inbox_carousel_title)
        val carouselMessage: TextView =
            itemView.findViewById(R.id.we_notification_custom_carousel_message)
        val carouselContainer: LinearLayout =
            itemView.findViewById(R.id.we_notification_custom_carousel_container)
        val carouselImage: ImageView =
            itemView.findViewById(R.id.we_notification_custom_carousel_Image)
    }

}