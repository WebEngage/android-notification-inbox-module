package com.webengage.test

import android.app.Application
import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import com.webengage.sdk.android.WebEngage
import com.webengage.sdk.android.WebEngageActivityLifeCycleCallbacks
import com.webengage.sdk.android.WebEngageConfig
import com.webengage.sdk.android.actions.database.ReportingStrategy


class MainApplication : Application() {

    companion object {
        lateinit var mContext: Context
    }

    override fun onCreate() {
        super.onCreate()
        mContext = applicationContext

        // WebEngage Initialization
        val webEngageConfig = WebEngageConfig.Builder()
            .setWebEngageKey("WEBENGAGE_KEY")
            .setDebugMode(true) // only in development mode
            .setEventReportingStrategy(ReportingStrategy.FORCE_SYNC)
            .build()
        registerActivityLifecycleCallbacks(
            WebEngageActivityLifeCycleCallbacks(
                this,
                webEngageConfig
            )
        )
    }

}