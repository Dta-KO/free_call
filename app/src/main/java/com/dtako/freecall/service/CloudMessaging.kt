package com.dtako.freecall.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.Keep
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.dtako.freecall.MyApplication
import com.dtako.freecall.R
import com.dtako.freecall.activity.IncomingActivity
import com.dtako.freecall.activity.MainActivity
import com.dtako.freecall.utils.Constants
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


/**
 * Created by Nguyen Kim Khanh on 2/20/2021.
 */
@Keep
class CloudMessaging : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        if (Firebase.auth.currentUser != null) {
            FirebaseFirestore.getInstance().collection(Constants.USER)
                    .document(Firebase.auth.currentUser!!.uid)
                    .update(Constants.USER_TOKEN, token)
        }
    }
    // in my previous app., it run ok, but from yeesssterday, it wrong, funtion on messagRe not work
    // I just send a test mess
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM: ", "on receive")
        val type = remoteMessage.data[Constants.REMOTE_MSG_TYPE]
        if (type.equals(Constants.REMOTE_MSG_INVITATION)) {
            if (!MyApplication.isOnForeground) {
                //onreceive run ok in this line when app in background or kill
                Log.d("FCM: ", "ok")
                sendNotification(remoteMessage, R.string.notify_invite_call, AudioAttributes.USAGE_NOTIFICATION_RINGTONE, Settings.System.DEFAULT_RINGTONE_URI, Constants.NOTIFICATION_INVITATION_ID, true)
            } else {
                val intentTarget = Intent(applicationContext, IncomingActivity::class.java)
                intentTarget.putExtra(Constants.REMOTE_MSG_INVITER_TOKEN, remoteMessage.data[Constants.REMOTE_MSG_INVITER_TOKEN])
                intentTarget.putExtra(Constants.ROOM_ID, remoteMessage.data[Constants.ROOM_ID])
                startActivity(intentTarget)
            }

        } else if (type.equals(Constants.REMOTE_MSG_INVITATION_RESPONSE)) {

            if (!MyApplication.isOnForeground) {
                if (remoteMessage.data[Constants.REMOTE_MSG_INVITATION_RESPONSE] == Constants.REMOTE_MSG_INVITATION_CANCELED) {
                    cancelNotification()
                    sendNotification(remoteMessage, R.string.cancel_by_partner, AudioAttributes.USAGE_NOTIFICATION, Settings.System.DEFAULT_NOTIFICATION_URI, Constants.NOTIFICATION_CANCEL_ID, false)
                } else if (remoteMessage.data[Constants.REMOTE_MSG_INVITATION_RESPONSE] == Constants.REMOTE_MSQ_TIME_OUT) {
                    cancelNotification()
                    sendNotification(remoteMessage, R.string.missed_call, AudioAttributes.USAGE_NOTIFICATION, Settings.System.DEFAULT_NOTIFICATION_URI, Constants.NOTIFICATION_CANCEL_ID, false)
                }
            }
            val intent = Intent(Constants.REMOTE_MSG_INVITATION_RESPONSE)
            intent.putExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE, remoteMessage.data[Constants.REMOTE_MSG_INVITATION_RESPONSE])
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
        }
    }


    private fun cancelNotification() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(Constants.NOTIFICATION_INVITATION_ID)
    }


    private fun sendNotification(remoteMessage: RemoteMessage, message: Int, typeAudioAttributes: Int, notifyUri: Uri, notifyId: Int, isIncomingCall: Boolean) {
        val fullScreenIntent: Intent
        if (isIncomingCall) {
            fullScreenIntent = Intent(this, IncomingActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(Constants.ROOM_ID, remoteMessage.data[Constants.ROOM_ID])
                putExtra(Constants.REMOTE_MSG_INVITER_TOKEN, remoteMessage.data[Constants.REMOTE_MSG_INVITER_TOKEN])
            }
        } else {
            fullScreenIntent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(Constants.ROOM_ID, remoteMessage.data[Constants.ROOM_ID])
                putExtra(Constants.REMOTE_MSG_INVITER_TOKEN, remoteMessage.data[Constants.REMOTE_MSG_INVITER_TOKEN])
            }
        }

        val fullScreenPendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
            // Add the intent, which inflates the back stack
            addNextIntentWithParentStack(fullScreenIntent)
            // Get the PendingIntent containing the entire back stack
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        val audioAttributes = AudioAttributes.Builder()
                .setUsage(typeAudioAttributes)
                .build()
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(resources.getString(R.string.app_name), "Incoming call!", NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.description = getString(message)
            notificationChannel.setSound(notifyUri, audioAttributes)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val notificationBuilder =
                NotificationCompat.Builder(this, resources.getString(R.string.app_name))
                        .setSmallIcon(R.drawable.icon)
                        .setContentTitle("Incoming call")
                        .setContentText(getString(message))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setSound(notifyUri)
                        .setChannelId(resources.getString(R.string.app_name))
                        .setContentIntent(fullScreenPendingIntent)
                        .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.icon))

        val incomingCallNotification = notificationBuilder.build()
        notificationManager.notify(notifyId, incomingCallNotification)

    }
}