package com.dtako.freecall.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.dtako.freecall.R
import com.dtako.freecall.databinding.ActivityMainBinding
import com.dtako.freecall.fragment.SettingFragment
import com.dtako.freecall.utils.Constants
import com.facebook.login.LoginManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {
    private lateinit var adView: AdView
    private var sharedPreferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //admob banner
        adView = binding.adView
        MobileAds.initialize(this) {
            val request = AdRequest.Builder().build()
            adView.loadAd(request)
        }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(baseContext)

        binding.motionMain.setTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {

            }

            override fun onTransitionChange(motion: MotionLayout?, startId: Int, endId: Int, progress: Float) {

            }

            override fun onTransitionCompleted(motion: MotionLayout?, currentId: Int) {
                when (currentId) {
                    R.id.end -> {
                        motion?.setTransition(R.id.start_hoa, R.id.end_hoa)
                        motion?.transitionToEnd()
                    }
                    R.id.end_hoa -> {
                        motion?.setTransition(R.id.start_hoa2, R.id.end_hoa2)
                        motion?.transitionToEnd()
                    }
                    R.id.end_hoa2 -> {
                        motion?.setTransition(R.id.start, R.id.end)
                        motion?.transitionToEnd()
                    }
                }
            }

            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {

            }

        })
        binding.btnCall.motionCall.setTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {

            }

            override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {
            }

            override fun onTransitionCompleted(motion: MotionLayout?, currentId: Int) {
                if (currentId == R.id.end_call) {
                    motion?.setTransition(R.id.end_call, R.id.start_call)
                    motion?.transitionToEnd()
                }
            }

            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {
            }

        })
        binding.btnLogout.motionLogout.setTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {

            }

            override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {

            }

            override fun onTransitionCompleted(motion: MotionLayout?, currentId: Int) {
                if (currentId == R.id.end_logout) {
                    motion?.setTransition(R.id.end_logout, R.id.start_logout)
                    motion?.transitionToEnd()
                }
            }

            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {
            }

        })
        binding.btnLogout.motionLogout.setOnClickListener { logout() }
        binding.buttonSetting.setOnClickListener { setting() }
        binding.btnCall.motionCall.setOnClickListener {
            updateUi(binding.btnCall.root, false).run { call() }
        }
    }

    @SuppressLint("CommitPrefEdits")
    override fun onResume() {
        super.onResume()
        val request = AdRequest.Builder().build()
        adView.loadAd(request)
        updateStatus(true)
        if (Firebase.auth.currentUser != null) {
            Firebase.auth.currentUser!!.getIdToken(true).addOnCompleteListener {
                FirebaseFirestore.getInstance().collection(Constants.USER)
                        .document(Firebase.auth.currentUser!!.uid)
                        .update(Constants.USER_TOKEN, it.result!!.token)
            }
        }
        updateUi(findViewById(R.id.btn_call), true)

    }


    override fun onPause() {
        super.onPause()
        updateStatus(false)
    }

    private fun updateStatus(value: Boolean) {
        FirebaseFirestore.getInstance().collection("users")
                .document(Firebase.auth.currentUser!!.uid)
                .update(Constants.USER_STATUS, value)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            for (grantResult in grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    requirePermission()
                }
            }
        }
    }

    private fun logout() {
        Firebase.auth.signOut()
        LoginManager.getInstance().logOut()
        if (Firebase.auth.currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun requirePermission(): Boolean {
        val permission = Manifest.permission.RECORD_AUDIO
        val permission2 = Manifest.permission.MODIFY_AUDIO_SETTINGS
        val permission3 = Manifest.permission.CAMERA
        val permission4 = Manifest.permission.READ_PHONE_STATE
        val grant = ContextCompat.checkSelfPermission(this, permission)
        val grant2 = ContextCompat.checkSelfPermission(this, permission2)
        val grant3 = ContextCompat.checkSelfPermission(this, permission3)
        val grant4 = ContextCompat.checkSelfPermission(this, permission4)

        return if (grant != PackageManager.PERMISSION_GRANTED || grant2 != PackageManager.PERMISSION_GRANTED
                || grant3 != PackageManager.PERMISSION_GRANTED || grant4 != PackageManager.PERMISSION_GRANTED) {
            val permissions = arrayOfNulls<String>(4)
            permissions[0] = permission
            permissions[1] = permission2
            permissions[2] = permission3
            permissions[3] = permission4
            ActivityCompat.requestPermissions(this, permissions, 1)
            false
        } else {
            true
        }
    }

    private fun updateUi(view: View, isEnable: Boolean) {
        view.isEnabled = isEnable
    }

    private fun call() {
        if (requirePermission()) {
            Firebase.auth.currentUser!!.getIdToken(true).addOnCompleteListener {
                val intent = Intent(this, OutgoingActivity::class.java)
                intent.putExtra(Constants.USER_TOKEN, it.result.toString())
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }

        }
    }

    private fun setting() {
        supportFragmentManager
                .beginTransaction()
                .add(android.R.id.content, SettingFragment())
                .addToBackStack(MainActivity::class.java.simpleName)
                .commit()
    }

}