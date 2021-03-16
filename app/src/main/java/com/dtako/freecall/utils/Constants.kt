package com.dtako.freecall.utils

import androidx.annotation.Keep
import java.util.*

/**
 * Created by Nguyen Kim Khanh on 2/21/2021.
 */
@Keep
class Constants {
    companion object {
        const val BASE_URL_FIREBASE = "https://fcm.googleapis.com/fcm/"
        const val USER = "users"
        const val URL_SERVER = "https://meet.jit.si"
        const val USER_ID = "id"
        const val USER_STATUS = "status"
        const val USER_TIME_JOIN = "timeJoin"
        const val USER_TOKEN = "token"

        const val PREF_NOTIFICATION = "notifications"


        const val NOTIFICATION_CANCEL_ID = 101
        const val NOTIFICATION_INVITATION_ID = 102
        const val REMOTE_MSQ_TIME_OUT = "timeOut"
        const val REMOTE_MSG_INVITATION = "invitation"
        const val REMOTE_MSG_INVITATION_RESPONSE = "invitationResponse"
        const val REMOTE_MSG_INVITATION_ACCEPTED = "accepted"
        const val REMOTE_MSG_INVITATION_REFUSED = "refused"
        const val REMOTE_MSG_INVITATION_CANCELED = "canceled"
        const val REMOTE_MSG_INVITER_TOKEN = "invitationToken"

        const val ROOM_ID = "roomId"

        const val REMOTE_MSG_TYPE = "type"
        const val REMOTE_MSG_DATA = "data"
        const val REMOTE_MSG_PRIORITY = "priority"
        const val REMOTE_MSG_REGISTRATION_IDS = "registration_ids"
        private const val REMOTE_MSG_AUTHORIZATION = "Authorization"
        private const val REMOTE_MSG_CONTENT_TYPE = "Content-Type"
        fun getRemoteMessageHeaders(): HashMap<String, String> {
            val header = HashMap<String, String>()
            header[REMOTE_MSG_AUTHORIZATION] = "key=AAAANUceZg4:APA91bH333nJg3bG6LYoVKSTEW5AYhOhX2-3V_l_G-hGA0jgM3A_zDtImJJrkBbBKeHLeWu0e6z2ZfjsHqyQvT6OyggpGzR6-OD04Qx4HLLyTFLYUMvyhBvu68BLHtU9OvmDtGnXDmQt"
            header[REMOTE_MSG_CONTENT_TYPE] = "application/json"
            return header
        }
    }
}