package com.dtako.freecall.activity

import android.content.*
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.dtako.freecall.R
import com.dtako.freecall.model.User
import com.dtako.freecall.network.ApiCloudClient
import com.dtako.freecall.network.ApiCloudService
import com.dtako.freecall.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL
import java.util.*
import kotlin.random.Random

@Keep
class OutgoingActivity : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var motionEnd: MotionLayout
    private lateinit var txtStatus: TextView
    private lateinit var invitationToken: String
    private lateinit var roomId: String
    private var user: User? = null

    private val countDownTimer = object : CountDownTimer(28000, 1000) {
        override fun onTick(millisUntilFinished: Long) {

        }

        override fun onFinish() {
            if (user != null) {
                missCallTimeOut(user!!.token)
            }
            transitionToMain()
        }

    }.start()
    private val invitationResponseReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val type = intent!!.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE)

            when {
                type.equals(Constants.REMOTE_MSG_INVITATION_ACCEPTED) -> {
                    countDownTimer.cancel()
                    if (mediaPlayer != null) {
                        mediaPlayer!!.stop()
                    }
                    try {
                        val serverURL = URL(Constants.URL_SERVER)
                        val options = JitsiMeetConferenceOptions.Builder()
                                .setServerURL(serverURL)
                                .setWelcomePageEnabled(false)
                                .setVideoMuted(true)
                                .setAudioOnly(true)
                                .setRoom(roomId)
                                .build()
                        JitsiMeetActivity.launch(this@OutgoingActivity, options)
                        finish()
                    } catch (exception: java.lang.Exception) {
                        exception.printStackTrace()
                    }

                }
                type.equals(Constants.REMOTE_MSG_INVITATION_REFUSED) -> {
                    Toast.makeText(this@OutgoingActivity, resources.getString(R.string.cancel_by_partner), Toast.LENGTH_SHORT).show()
                    transitionToMain()
                }
                type.equals(Constants.REMOTE_MSQ_TIME_OUT) -> {
                    Toast.makeText(this@OutgoingActivity, getString(R.string.waiting_time_out), Toast.LENGTH_SHORT).show()

                    transitionToMain()
                }
            }
        }

    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(invitationResponseReceiver,
                IntentFilter(Constants.REMOTE_MSG_INVITATION_RESPONSE))
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(invitationResponseReceiver)
    }

    override fun onBackPressed() {
        if (user == null) {
            transitionToMain()
        } else {
            cancel(user!!.token).run { transitionToMain() }
        }
        super.onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_outgoing)

        mediaPlayer = MediaPlayer.create(applicationContext, R.raw.ringing_phone).apply { isLooping = true }

        FirebaseFirestore.getInstance().collection(Constants.USER)
                .whereEqualTo(Constants.USER_STATUS, true)
                .limit(10)
                .get()
                .addOnSuccessListener { it ->
                    if (it.isEmpty) {
                        Toast.makeText(this, getString(R.string.nobody_online_notifi), Toast.LENGTH_SHORT).show()
                        transitionToMain()
                    } else {
                        txtStatus.text = resources.getText(R.string.bell_ringing)
                        if (mediaPlayer != null) {
                            mediaPlayer!!.start()
                        }
                        val users = it.toObjects(User::class.java)
                        user = users[Random.nextInt(users.size)]
                        roomId = Random(10000).toString() + UUID.randomUUID()
                        FirebaseMessaging.getInstance().token.addOnCompleteListener {
                            if (it.isSuccessful) {
                                invitationToken = it.result!!
                                initiateMeeting(user!!.token, invitationToken)
                            }
                        }

                    }
                }

        txtStatus = findViewById(R.id.txt_status_outgoing)
        motionEnd = findViewById(R.id.motion_end)
        motionEnd.setTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {

            }

            override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {

            }

            override fun onTransitionCompleted(motion: MotionLayout?, currentId: Int) {
                if (user == null) {
                    transitionToMain()
                } else {
                    countDownTimer.cancel()
                    cancel(user!!.token).run { transitionToMain() }
                }
            }

            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {

            }

        })
    }

    private fun transitionToMain() {
        mediaPlayer!!.stop()
        val intent = Intent(this@OutgoingActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun initiateMeeting(receiverToken: String, invitationToken: String) {
        try {
            val tokens = JSONArray()
            tokens.put(receiverToken)

            val body = JSONObject()
            val data = JSONObject()

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION)
            data.put(Constants.REMOTE_MSG_INVITER_TOKEN, invitationToken)
            data.put(Constants.ROOM_ID, roomId)
            body.put(Constants.REMOTE_MSG_PRIORITY, 10)
            body.put(Constants.REMOTE_MSG_DATA, data)
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens)
            sendRemoteMessage(body.toString(), Constants.REMOTE_MSG_INVITATION)

        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    private fun cancel(receiverToken: String) {
        try {
            val tokens = JSONArray()
            tokens.put(receiverToken)

            val body = JSONObject()
            val data = JSONObject()

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION_RESPONSE)
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE, Constants.REMOTE_MSG_INVITATION_CANCELED)
            body.put(Constants.REMOTE_MSG_PRIORITY, 10)
            body.put(Constants.REMOTE_MSG_DATA, data)
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens)
            sendRemoteMessage(body.toString(), Constants.REMOTE_MSG_INVITATION_RESPONSE)
        } catch (exception: java.lang.Exception) {
            exception.printStackTrace()
        }
    }

    private fun missCallTimeOut(receiverToken: String) {
        try {
            val tokens = JSONArray()
            tokens.put(receiverToken)

            val body = JSONObject()
            val data = JSONObject()

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION_RESPONSE)
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE, Constants.REMOTE_MSQ_TIME_OUT)
            body.put(Constants.REMOTE_MSG_PRIORITY, 10)
            body.put(Constants.REMOTE_MSG_DATA, data)
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens)
            sendRemoteMessage(body.toString(), Constants.REMOTE_MSG_INVITATION_RESPONSE)
        } catch (exception: java.lang.Exception) {
            exception.printStackTrace()
        }
    }

    private fun sendRemoteMessage(remoteBody: String, type: String) {
        ApiCloudClient.getClient().create(ApiCloudService::class.java)
                .sendRemoteMessage(Constants.getRemoteMessageHeaders(), remoteBody)
                .enqueue(object : Callback<String> {
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        if (response.isSuccessful) {
                            if (type == Constants.REMOTE_MSG_INVITATION) {
                                Toast.makeText(this@OutgoingActivity, getString(R.string.invitation_success_notifi), Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@OutgoingActivity, response.message(), Toast.LENGTH_SHORT).show()

                            finish()
                        }
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        Toast.makeText(this@OutgoingActivity, t.message, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                })
    }
}