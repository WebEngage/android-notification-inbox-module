package com.webengage.test

import android.content.Context
import android.content.SharedPreferences
import android.util.Log


class SharedPrefsManager private constructor(context: Context) {
    init {
        mSharedPrefs = context.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE)
    }

    private fun doEdit() {
        if (mEditor == null) {
            mEditor = mSharedPrefs.edit()
        }
    }

    private fun doCommit() {
        if (mEditor != null) {
            mEditor!!.apply()
            mEditor = null
        }
    }

    operator fun contains(key: String?): Boolean {
        return mSharedPrefs.contains(key)
    }

    fun put(key: String, value: String) {
        doEdit()
        Log.d(TAG , " > putString() > " + key + ": " + value)
        mEditor!!.putString(key, value).apply()
        doCommit()
    }

    fun getString(key: String?, defaultValue: String?): String? {
        return mSharedPrefs.getString(key, defaultValue)
    }

    fun remove(vararg keys: String) {
        doEdit()
        for (key in keys) {
            Log.d(TAG , " > remove() > key: " + key)
            mEditor!!.remove(key)
        }
        doCommit()
    }

    companion object {
        private const val MY_PREFERENCES = "notification_inbox_module"
        private val TAG = SharedPrefsManager::class.java.simpleName
        private var mEditor: SharedPreferences.Editor? = null
        private lateinit var mSharedPrefs: SharedPreferences
        private var mSharedPrefsManager: SharedPrefsManager? = null
        private fun initialize() {
            synchronized(SharedPrefsManager::class.java) {
                if (mSharedPrefsManager == null) {
                    mSharedPrefsManager =
                        SharedPrefsManager(MainApplication.mContext)
                }
            }
        }

        fun get(): SharedPrefsManager? {
            if (mSharedPrefsManager == null) {
                initialize()
            }
            return mSharedPrefsManager
        }
    }
}