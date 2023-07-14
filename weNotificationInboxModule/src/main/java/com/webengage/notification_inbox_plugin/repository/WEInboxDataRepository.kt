package com.webengage.notification_inbox_plugin.repository

import android.content.Context
import com.webengage.notification_inbox_plugin.interfaces.WEInboxResponseCallback
import com.webengage.notification.inbox.WEInboxConfig
import com.webengage.notification.inbox.WENotificationInbox
import com.webengage.notification.inbox.callbacks.WEInboxCallback
import com.webengage.notification.inbox.data.models.WEInboxData
import com.webengage.notification.inbox.data.models.WEInboxMessage

object WEInboxDataRepository {

    fun getNotificationList(
        context: Context,
        weInboxMessage: WEInboxMessage,
        weInboxResponseCallback: WEInboxResponseCallback
    ) {
        WENotificationInbox.get(context)
            .getNotificationList(context, weInboxMessage, object :
                WEInboxCallback<WEInboxData> {
                override fun onSuccess(result: WEInboxData) {
                    weInboxResponseCallback.onSuccess(result)
                }

                override fun onError(errorCode: Int, error: Map<String, Any?>) {
                    weInboxResponseCallback.onFailure(errorCode, error)
                }
            })
    }

    fun getNotificationList(context: Context, weInboxResponseCallback: WEInboxResponseCallback) {
        WENotificationInbox.get(context, WEInboxConfig(true))
            .getNotificationList(context, object : WEInboxCallback<WEInboxData> {
                override fun onSuccess(result: WEInboxData) {
                    weInboxResponseCallback.onSuccess(result)
                }

                override fun onError(errorCode: Int, error: Map<String, Any?>) {
                    weInboxResponseCallback.onFailure(errorCode, error)
                }
            })
    }
}