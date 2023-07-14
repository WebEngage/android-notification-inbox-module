package com.webengage.notification_inbox_plugin.interfaces

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.webengage.notification_inbox_plugin.view.WebEngageRecyclerViewAdapter
import com.webengage.notification.inbox.data.models.WEInboxMessage

abstract class WEInboxAdapterInterface {

    abstract fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        mNotificationList: ArrayList<WEInboxMessage>,
        position: Int
    )

    abstract fun getViewType(notificationList: ArrayList<WEInboxMessage>, position: Int): Int

    abstract fun createViewHolder(parent: ViewGroup, type: Int) : RecyclerView.ViewHolder

    abstract fun getReadListener(weInboxItemRead: WebEngageRecyclerViewAdapter.WEInboxItemRead?)

    abstract fun getUnReadListener(weInboxItemUnread: WebEngageRecyclerViewAdapter.WEInboxItemUnread?)

    abstract fun getClickListener(weInboxItemClick: WebEngageRecyclerViewAdapter.WEInboxItemClick?)

    abstract fun getDeleteListener(weInboxItemDelete: WebEngageRecyclerViewAdapter.WEInboxItemDelete?)

}