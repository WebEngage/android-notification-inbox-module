package com.webengage.test
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.webengage.notification_inbox_plugin.utils.WENotificationInboxConstants
import com.webengage.sdk.android.Logger
import com.webengage.sdk.android.WebEngage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val data = remoteMessage.data
        Logger.d(WENotificationInboxConstants.TAG, "onMessageReceived")
        if (data != null) {
            if (data.containsKey("source") && "webengage" == data["source"]) {
                WebEngage.get().receive(data)
            }
        }
    }


    override fun onCreate() {
        super.onCreate()
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            try {
                val token: String? = task.result
                Logger.d(WENotificationInboxConstants.TAG, "FirebaseMessaging onCreate - $token")

                WebEngage.get().setRegistrationID(token)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}