package com.webengage.notification_inbox_plugin.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.notification_inbox_plugin.R

class WENotificationInboxActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Fragment is attached in this activity. This activity will refer to WEInboxFragment
        setContentView(R.layout.activity_wenotification_inbox)
    }
}