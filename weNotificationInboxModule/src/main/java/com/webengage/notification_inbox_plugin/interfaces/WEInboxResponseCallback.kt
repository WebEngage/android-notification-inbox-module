package com.webengage.notification_inbox_plugin.interfaces

import com.webengage.notification.inbox.data.models.WEInboxData

interface WEInboxResponseCallback {
    fun onSuccess(weInboxData: WEInboxData)
    fun onFailure(errorCode: Int, error: Map<String, Any?>)
}