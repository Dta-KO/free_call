package com.dtako.freecall

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.preference.PreferenceManager
import com.dtako.freecall.utils.Constants
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging

/**
 * Created by Nguyen Kim Khanh on 2/25/2021.
 */
class MyApplication : Application(), LifecycleObserver {
    companion object {
        var isOnForeground: Boolean = false
    }

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        isOnForeground = true
    }

    @SuppressLint("CommitPrefEdits")
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        isOnForeground = true
        if (Firebase.auth.currentUser != null) {
            FirebaseFirestore.getInstance().collection(Constants.USER)
                    .document(Firebase.auth.currentUser!!.uid)
                    .update(Constants.USER_STATUS, true)
//            FirebaseMessaging.getInstance().token.addOnCompleteListener {
//                FirebaseFirestore.getInstance().collection(Constants.USER)
//                        .document(Firebase.auth.currentUser!!.uid)
//                        .update(Constants.USER_TOKEN, it.result)
//            }

        }

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        isOnForeground = false
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(baseContext)
        if (sharedPreferences.getBoolean(Constants.PREF_NOTIFICATION, false)) {
            if (Firebase.auth.currentUser != null) {
                FirebaseFirestore.getInstance().collection(Constants.USER)
                        .document(Firebase.auth.currentUser!!.uid)
                        .update(Constants.USER_STATUS, true)
//                FirebaseMessaging.getInstance().token.addOnCompleteListener {
//                    if (Firebase.auth.currentUser != null) {
//                        FirebaseFirestore.getInstance().collection(Constants.USER)
//                                .document(Firebase.auth.currentUser!!.uid)
//                                .update(Constants.USER_TOKEN, it.result)
//                    }
//                }
            }

        } else {
            if (Firebase.auth.currentUser != null) {
                FirebaseFirestore.getInstance().collection(Constants.USER)
                        .document(Firebase.auth.currentUser!!.uid)
                        .update(Constants.USER_STATUS, false)
//                FirebaseMessaging.getInstance().token.addOnCompleteListener {
//                    FirebaseFirestore.getInstance().collection(Constants.USER)
//                            .document(Firebase.auth.currentUser!!.uid)
//                            .update(Constants.USER_TOKEN, "")
//                }
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        isOnForeground = false
    }

}