package com.dtako.freecall.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Toast
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.dtako.freecall.R
import com.dtako.freecall.network.ApiCloudClient
import com.dtako.freecall.network.ApiCloudService
import com.dtako.freecall.utils.Constants
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL
@Keep
class IncomingActivity : AppCompatActivity() {
    private lateinit var motionRefuse: MotionLayout
    private lateinit var motionAccept: MotionLayout
    private var notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
    private var ringtone: Ringtone? = null
    private val invitationResponseReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val type = intent!!.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE)
            if (type.equals(Constants.REMOTE_MSG_INVITATION_CANCELED)) {
                if (notification == null) {
                    notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    if (notification == null) {
                        notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALL)
                    }
                }
                ringtone = RingtoneManager.getRingtone(applicationContext, notification)
                ringtone!!.stop()
                Toast.makeText(this@IncomingActivity, getString(R.string.cancel_by_partner), Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incoming)
        //sound for incoming call
        if (notification == null) {
            notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            if (notification == null) {
                notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALL)
            }
        }
        ringtone = RingtoneManager.getRingtone(applicationContext, notification)
        ringtone!!.play()

        val intent = intent
        val invitationToken = intent.getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN)

        val countDownTimer = object : CountDownTimer(25000, 1000) {
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                ringtone!!.stop()
                sendInvitationResponse(invitationToken!!, Constants.REMOTE_MSQ_TIME_OUT)
            }

        }.start()
        motionRefuse = findViewById(R.id.motion_decline)
        motionAccept = findViewById(R.id.motion_accept)
        motionRefuse.setTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {

            }

            override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {

            }

            override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
                ringtone!!.stop()
                countDownTimer.cancel()
                sendInvitationResponse(invitationToken!!, Constants.REMOTE_MSG_INVITATION_REFUSED)
            }

            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {

            }

        })
        motionAccept.setTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {
            }

            override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {

            }

            override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
                ringtone!!.stop()
                countDownTimer.cancel()
                sendInvitationResponse(invitationToken!!, Constants.REMOTE_MSG_INVITATION_ACCEPTED)
            }

            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {

            }

        })
    }


    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(invitationResponseReceiver, IntentFilter(Constants.REMOTE_MSG_INVITATION_RESPONSE))
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(invitationResponseReceiver)
    }


    private fun sendInvitationResponse(receiverToken: String, type: String) {
        try {
            val tokens = JSONArray()
            tokens.put(receiverToken)

            val body = JSONObject()
            val data = JSONObject()

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION_RESPONSE)
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE, type)
            body.put(Constants.REMOTE_MSG_PRIORITY, "high")
            body.put(Constants.REMOTE_MSG_DATA, data)
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens)
            sendRemoteMessage(body.toString(), type)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    private fun sendRemoteMessage(remoteBody: String, type: String) {
        ApiCloudClient.getClient().create(ApiCloudService::class.java)
                .sendRemoteMessage(Constants.getRemoteMessageHeaders(), remoteBody)
                .enqueue(object : Callback<String> {
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        if (response.isSuccessful) {
                            if (type == Constants.REMOTE_MSG_INVITATION_ACCEPTED) {
//                                val target = Intent(this@IncomingActivity, CallingActivity::class.java)
//                                target.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                                startActivity(target)
                                try {
                                    val serverURL = URL(Constants.URL_SERVER)
                                    val options = JitsiMeetConferenceOptions.Builder()
                                            .setServerURL(serverURL)
                                            .setWelcomePageEnabled(false)
                                            .setVideoMuted(true)
                                            .setAudioOnly(true)
                                            .setRoom(intent.getStringExtra(Constants.ROOM_ID))
                                            .build()
                                    JitsiMeetActivity.launch(this@IncomingActivity, options)
                                    finish()
                                } catch (exception: java.lang.Exception) {
                                    exception.printStackTrace()
                                }
                            } else {
                                finish()
                            }
                        } else {
                            Toast.makeText(this@IncomingActivity, response.message(), Toast.LENGTH_SHORT).show()

                            finish()
                        }
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        Toast.makeText(this@IncomingActivity, t.message, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                })
    }

}