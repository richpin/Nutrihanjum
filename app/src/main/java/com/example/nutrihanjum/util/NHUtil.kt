package com.example.nutrihanjum.util

import android.content.Context
import com.example.nutrihanjum.R

object NHUtil {
    private object TIME_MAXIMUM {
        const val SEC = 60
        const val MIN = 60
        const val HOUR = 24
        const val DAY = 30
        const val MONTH = 12
    }

    fun formatTime(mContext: Context, regTime: Long): String {
        val currentTime = System.currentTimeMillis()
        val diffSEC = (currentTime - regTime) / 1000
        val diffMIN = diffSEC / TIME_MAXIMUM.SEC
        val diffHOUR = diffMIN / TIME_MAXIMUM.MIN
        val diffDAY = diffHOUR / TIME_MAXIMUM.HOUR
        val diffMONTH = diffDAY / TIME_MAXIMUM.DAY
        val diffYEAR = diffMONTH / TIME_MAXIMUM.MONTH

        val msg: String = when {
            diffSEC < TIME_MAXIMUM.SEC -> mContext.getString(R.string.time_just_now)
            diffMIN < TIME_MAXIMUM.MIN -> diffMIN.toString() + mContext.getString(R.string.time_minute_before)
            diffHOUR < TIME_MAXIMUM.HOUR -> diffHOUR.toString() + mContext.getString(R.string.time_hour_before)
            diffDAY < TIME_MAXIMUM.DAY -> diffDAY.toString() + mContext.getString(R.string.time_day_before)
            diffMONTH < TIME_MAXIMUM.MONTH -> diffMONTH.toString() + mContext.getString(R.string.time_month_before)
            else -> diffYEAR.toString() + mContext.getString(R.string.time_year_before)
        }

        return msg
    }
}