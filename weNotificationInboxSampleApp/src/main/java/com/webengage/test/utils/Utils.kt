package com.webengage.test.utils

import com.webengage.test.SharedPrefsManager

object Utils {

    fun isBlank(str: String?): Boolean {
        return str == null || str.trim { it <= ' ' }.isEmpty()
    }

    fun getLoggedIn(): Boolean {
        return !isBlank(
            SharedPrefsManager.get()?.getString(Constants.CUID, "")
        )
    }

}
