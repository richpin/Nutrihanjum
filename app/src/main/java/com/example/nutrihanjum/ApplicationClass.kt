package com.example.nutrihanjum

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.multidex.MultiDexApplication
import com.google.firebase.FirebaseApp

import com.google.firebase.FirebaseOptions




class ApplicationClass : MultiDexApplication() {
    lateinit var CHANNEL_ID : String

    override fun onCreate() {
        super.onCreate()

        CHANNEL_ID = getString(R.string.default_notification_channel_id)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
            mChannel.description = descriptionText
            notificationManager.createNotificationChannel(mChannel)
        }
    }

}