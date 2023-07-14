package com.webengage.notification_inbox_plugin

import android.content.Context
import android.content.Intent
import com.webengage.notification_inbox_plugin.interfaces.WEInboxAdapterInterface
import com.webengage.notification_inbox_plugin.interfaces.WEInboxModuleInterface
import com.webengage.notification_inbox_plugin.view.WEInboxAdapter
import com.webengage.notification_inbox_plugin.view.WENotificationInboxActivity

class WEInboxModule : WEInboxModuleInterface {

    private val dateTimeFormatMap: MutableMap<String, String> = mutableMapOf()
    private var adapterHolder: WEInboxAdapterInterface? = null

    companion object {
        private var INSTANCE: WEInboxModule? = null

        fun get(): WEInboxModule {
            INSTANCE ?: synchronized(this) {
                INSTANCE = WEInboxModule()
            }
            return INSTANCE!!
        }
    }

    override fun setWEAdapter(weAdapterHolder: WEInboxAdapterInterface) {
        adapterHolder = weAdapterHolder
    }

    override fun updateDateFormat(timeFormat: String, layoutType: String) {
        dateTimeFormatMap[layoutType] = timeFormat
    }

    fun getTimeFormat(layoutType: String): String {
        return dateTimeFormatMap[layoutType] ?: ""
    }

    fun getWEAdapter(): WEInboxAdapterInterface {
        if (adapterHolder == null) {
            adapterHolder = WEInboxAdapter()
        }
        return adapterHolder!!
    }

    fun navigateToWEInboxModule(context: Context) {
        val intent = Intent(context, WENotificationInboxActivity::class.java)
        context.startActivity(intent)
    }

}