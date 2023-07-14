package com.webengage.notification_inbox_plugin.utils

import com.webengage.notification_inbox_plugin.WEInboxModule
import com.webengage.sdk.android.Logger
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object Utils {

    fun formatTimeStamp(timeStamp: String?, layoutType: String): String {
        try {
            val textLayoutDateFormat: String =
                WEInboxModule.get().getTimeFormat(WENotificationInboxConstants.TEXT)
            val bannerLayoutDateFormat: String =
                WEInboxModule.get().getTimeFormat(WENotificationInboxConstants.BANNER)

            return when (layoutType) {
                WENotificationInboxConstants.TEXT -> {
                    if (textLayoutDateFormat.isEmpty()) {
                        getDefaultFormat(timeStamp!!)
                    } else {
                        // User provided Date Format
                        getUpdatedFormat(timeStamp!!, textLayoutDateFormat)
                    }
                }

                WENotificationInboxConstants.BANNER -> {
                    if (bannerLayoutDateFormat.isEmpty()) {
                        getDefaultFormat(timeStamp!!)
                    } else {
                        // User provided Date Format
                        getUpdatedFormat(timeStamp!!, bannerLayoutDateFormat)
                    }
                }

                else -> getDefaultFormat(timeStamp!!)
            }
        } catch (e: ParseException) {
            Logger.e(WENotificationInboxConstants.TAG, "Date-Utils Cannot parse timestamp $e")
        }
        return ""
    }

    // custom date format
    private fun getUpdatedFormat(timeStamp: String, dateTimeFormat: String): String {
        val sdfInput = SimpleDateFormat(WENotificationInboxConstants.DATE_FORMAT_PATTERN, Locale.US)
        val date = sdfInput.parse(timeStamp)

        val sdfOutput = SimpleDateFormat(dateTimeFormat, Locale.US)
        return sdfOutput.format(date!!)
    }

    private fun getDefaultFormat(timeStamp: String): String {
        val dateFormat =
            SimpleDateFormat(WENotificationInboxConstants.DATE_FORMAT_PATTERN, Locale.US)
        dateFormat.timeZone = TimeZone.getTimeZone(WENotificationInboxConstants.INDIA_TIMEZONE)
        val parsedDate = dateFormat.parse(timeStamp)
        val now = Date()

        val milliseconds = now.time - parsedDate!!.time
        val seconds = (milliseconds / 1000).toInt()

        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        val weeks = days / 7
        val months = days / 30
        val years = days / 365
        return when {
            seconds < 60 -> getQuantityString(seconds, "second")
            minutes < 60 -> getQuantityString(minutes, "minute")
            hours < 24 -> getQuantityString(hours, "hour")
            days < 7 -> getQuantityString(days, "day")
            weeks < 4 -> getQuantityString(weeks, "week")
            months < 12 -> getQuantityString(months, "month")
            else -> getQuantityString(years, "year")
        }
    }


    private fun getQuantityString(quantity: Int, unit: String): String {
        return if (quantity == 1) {
            "1 $unit ago"
        } else {
            "$quantity ${unit}s ago"
        }
    }
}