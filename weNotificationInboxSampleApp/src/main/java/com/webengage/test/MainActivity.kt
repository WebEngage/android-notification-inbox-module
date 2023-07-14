package com.webengage.test

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.webengage.notification_inbox_plugin.WEInboxModule
import com.webengage.notification_inbox_plugin.utils.WENotificationInboxConstants
import com.webengage.sdk.android.WebEngage
import com.webengage.test.utils.Constants
import com.webengage.notification.inbox.WEInboxConfig
import com.webengage.notification.inbox.WENotificationInbox
import com.webengage.notification.inbox.callbacks.WEInboxCallback
import com.webengage.sdk.android.Logger
import com.webengage.test.utils.Utils

class MainActivity : AppCompatActivity() {
    private var notificationsMenuTV: TextView? = null
    private var mContext: Context? = null
    private var loginButton: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mContext = this
        initViews()
        getPushPermission()

        // Set your CustomWEInboxAdapter as the adapter for the WebEngage Notification Inbox Module
         val customAdapter = CustomWEInboxAdapter()
         WEInboxModule.get().setWEAdapter(customAdapter)


        // Set your customized date format
        // WEInboxModule.get().updateDateFormat("MM-dd HH:mm:ss", WEAdapterConstants.TEXT)
        // WEInboxModule.get().updateDateFormat("dd/mm/yyyy", WEAdapterConstants.BANNER)
    }

    private fun getPushPermission() {
        if (checkSelfPermission(Constants.PUSH_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Constants.PUSH_PERMISSION),
                102
            )
            WebEngage.get().user().setDevicePushOptIn(false)
        } else {
            WebEngage.get().user().setDevicePushOptIn(true)
        }
    }

    override fun onResume() {
        super.onResume()
        if (notificationsMenuTV != null) setupBadge()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.notification_inbox, menu)
        val menuItem = menu.findItem(R.id.menu_notification)
        loginButton = menu.findItem(R.id.menu_login_button)
        updateLoginButtonLabel()
        val actionView = menuItem.actionView
        notificationsMenuTV = actionView!!.findViewById<View>(R.id.notifications_badge) as TextView
        setupBadge()
        actionView.setOnClickListener { onOptionsItemSelected(menuItem) }
        return super.onCreateOptionsMenu(menu)
    }

    // Notification Icon with number of notification size
    private fun setupBadge() {
        notificationsMenuTV!!.visibility = View.GONE
        WENotificationInbox.get(this.applicationContext, WEInboxConfig(true))
            .getUserNotificationCount(this, object : WEInboxCallback<String> {
                override fun onSuccess(result: String) {
                    notificationsMenuTV!!.visibility = View.VISIBLE
                    notificationsMenuTV!!.text = result
                }

                override fun onError(errorCode: Int, error: Map<String, *>) {
                    notificationsMenuTV!!.text = ""
                    notificationsMenuTV!!.visibility = View.GONE
                }
            })

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_notification -> {
                goToNotificationInbox()
                true
            }

            R.id.menu_login_button -> {
                if (Utils.getLoggedIn()) {
                    logoutUser()
                } else {
                    showLoginDialog()
                }
                true
            }

            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun logoutUser() {
        SharedPrefsManager.get()?.remove(Constants.CUID)
        SharedPrefsManager.get()?.remove(Constants.JWT)
        updateLoginButtonLabel()
        WebEngage.get().user().logout()
    }

    private fun showLoginDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_login, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        builder.setCancelable(true)
            .setPositiveButton(
                Constants.LOGIN
            ) { _, _ ->
            }
            .setNegativeButton(
                Constants.CANCEL
            ) { _, _ ->
            }
        val alertDialog = builder.create()
        alertDialog.show()
        alertDialog.getButton(-1).setOnClickListener(View.OnClickListener {
            val jwt =
                (dialogView.findViewById<View>(R.id.passwordEditText) as EditText).text.toString()
                    .trim { it <= ' ' }
            val username =
                (dialogView.findViewById<View>(R.id.usernameEditText) as EditText).text.toString()
                    .trim { it <= ' ' }
            if (!Utils.isBlank(username)) {
                login(username, jwt)
                alertDialog.dismiss()
                return@OnClickListener
            }
        })
    }

    private fun login(username: String, jwt: String) {
        WebEngage.get().user().login(username, jwt)
        SharedPrefsManager.get()?.put(Constants.CUID, username)
        SharedPrefsManager.get()?.put(Constants.JWT, jwt)
        updateLoginButtonLabel()
    }

    private fun initViews() {
        try {
            supportActionBar!!.title = Constants.APP_TITLE
            supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        } catch (e: Exception) {
            Logger.d(WENotificationInboxConstants.TAG," Error in initializing Action bar")
        }
    }

    private fun updateLoginButtonLabel() {
        if (Utils.getLoggedIn()) {
            loginButton?.title = Constants.LOGOUT
        } else {
            loginButton?.title = Constants.LOGIN
        }
    }

    private fun goToNotificationInbox() {
        WEInboxModule.get().navigateToWEInboxModule(mContext!!)
    }

}
