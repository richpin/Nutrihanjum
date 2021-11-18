package com.example.nutrihanjum.util

import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.NotificationTarget
import com.example.nutrihanjum.ApplicationClass
import com.example.nutrihanjum.MainActivity
import com.example.nutrihanjum.R
import com.example.nutrihanjum.community.CommentActivity
import com.example.nutrihanjum.community.NoticeActivity
import com.example.nutrihanjum.repository.UserRepository
import com.example.nutrihanjum.user.MyPostActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class NHFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        Log.wtf("FCM", "Refreshed token: $token")

        UserRepository.updateToken(token)
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)
        Log.wtf("OnMessageReceived", p0.notification.toString())

        p0.notification?.let {
            val resultIntent = Intent(this, NoticeActivity::class.java)
            val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
                addNextIntentWithParentStack(resultIntent)
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            }

            val bitmap = Glide.with(this).asBitmap().load(it.imageUrl).submit().get()

            val builder =
                NotificationCompat.Builder(this, (application as ApplicationClass).CHANNEL_ID)
                    .setSmallIcon(R.drawable.app_logo)
                    .setContentTitle(it.title)
                    .setContentText(it.body)
                    .setLargeIcon(bitmap)
                    .setStyle(NotificationCompat.BigPictureStyle()
                        .bigPicture(bitmap)
                        .bigLargeIcon(null))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(resultPendingIntent)
                    .setAutoCancel(true)

            NotificationManagerCompat.from(this).notify(11111, builder.build())
        }
    }

    override fun onDeletedMessages() {
        super.onDeletedMessages()
        Log.wtf("OnDeletedMessages", "FAILED")
    }

}