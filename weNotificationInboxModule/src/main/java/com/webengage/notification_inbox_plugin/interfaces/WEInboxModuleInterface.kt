package com.webengage.notification_inbox_plugin.interfaces

interface WEInboxModuleInterface {
    fun setWEAdapter(weAdapterHolder: WEInboxAdapterInterface)
    fun updateDateFormat(timeFormat: String, layoutType: String)
}