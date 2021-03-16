package com.dtako.freecall.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dtako.freecall.databinding.ActivityCallingBinding
import com.dtako.freecall.web_rtc.SimpleSdpObserver
import org.webrtc.*


class CallingActivity : AppCompatActivity() {
    private lateinit var rootEglBase: EglBase
    private lateinit var videoTrackFromCamera: VideoTrack
    private lateinit var factory: PeerConnectionFactory
    private lateinit var localPeerConnection: PeerConnection
    private lateinit var remotePeerConnection: PeerConnection
    private lateinit var binding: ActivityCallingBinding

    companion object {
        const val VIDEO_RESOLUTION_WIDTH = 1280
        const val VIDEO_RESOLUTION_HEIGHT = 720
        const val FPS = 30
        private const val DTLS_SRTP_KEY_AGREEMENT_CONSTRAINT = "DtlsSrtpKeyAgreement"
        const val VIDEO_TRACK_ID = "ARDAMSv0"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //init surface view
        rootEglBase = EglBase.create()
        binding.surfaceViewUser.apply {
            init(rootEglBase.eglBaseContext, null)
            setEnableHardwareScaler(true)
            setMirror(true)
        }

        binding.surfaceViewPartner.apply {
            init(rootEglBase.eglBaseContext, null)
            setEnableHardwareScaler(true)
            setMirror(true)
        }

        //init peer connection factory
        val options = PeerConnectionFactory.InitializationOptions.builder(applicationContext)
                .setEnableInternalTracer(true)
        PeerConnectionFactory.initialize(options.createInitializationOptions())
        factory = PeerConnectionFactory.builder().createPeerConnectionFactory()

        //create video track from camera and show it
        val videoCapturer = createVideoCapturer()
        val videoSource: VideoSource
        if (videoCapturer != null) {
            val surfaceRenderer = SurfaceTextureHelper.create("CaptureThread", rootEglBase.eglBaseContext)
            videoSource = factory.createVideoSource(videoCapturer.isScreencast)
            videoCapturer.initialize(surfaceRenderer, this, videoSource.capturerObserver)


            videoTrackFromCamera = factory.createVideoTrack(VIDEO_TRACK_ID, videoSource)
            videoTrackFromCamera.setEnabled(true)
            videoTrackFromCamera.addSink { VideoSink { binding.surfaceViewUser } }
            videoCapturer.startCapture(VIDEO_RESOLUTION_WIDTH, VIDEO_RESOLUTION_HEIGHT, FPS)

            //init peer connections
            localPeerConnection = createPeerConnection(factory, true)
            remotePeerConnection = createPeerConnection(factory, false)

            //start stream video
            val mediaStream = factory.createLocalMediaStream("ARDAMS")
            mediaStream.addTrack(videoTrackFromCamera)
            localPeerConnection.addStream(mediaStream)

            val mediaConstraints = MediaConstraints()

            localPeerConnection.createOffer(object : SimpleSdpObserver() {
                override fun onCreateSuccess(p0: SessionDescription?) {
                    super.onCreateSuccess(p0)
                    localPeerConnection.setRemoteDescription(object : SimpleSdpObserver() {}, p0)
                    remotePeerConnection.setLocalDescription(object : SimpleSdpObserver() {}, p0)
                    remotePeerConnection.createAnswer(object : SimpleSdpObserver() {
                        override fun onCreateSuccess(p0: SessionDescription?) {
                            super.onCreateSuccess(p0)
                            localPeerConnection.setRemoteDescription(object : SimpleSdpObserver() {}, p0)
                            remotePeerConnection.setLocalDescription(object : SimpleSdpObserver() {}, p0)
                        }

                    }, mediaConstraints)
                }
            }, mediaConstraints)
        }

    }

    private fun createPeerConnection(factory: PeerConnectionFactory?, isLocal: Boolean): PeerConnection {
        val rtcConfiguration = PeerConnection.RTCConfiguration(ArrayList())
        val observer = object : PeerConnection.Observer {
            override fun onSignalingChange(p0: PeerConnection.SignalingState?) {

            }

            override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
            }

            override fun onIceConnectionReceivingChange(p0: Boolean) {
            }

            override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
            }

            override fun onIceCandidate(p0: IceCandidate?) {
                if (isLocal) {
                    remotePeerConnection.addIceCandidate(p0)
                } else {
                    localPeerConnection.addIceCandidate(p0)
                }
            }

            override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {
            }

            override fun onAddStream(p0: MediaStream?) {
                val videoTrack = p0!!.videoTracks[0]
                videoTrack.setEnabled(true)
                videoTrack.addSink { VideoSink { binding.surfaceViewPartner } }
            }

            override fun onRemoveStream(p0: MediaStream?) {
            }

            override fun onDataChannel(p0: DataChannel?) {
            }

            override fun onRenegotiationNeeded() {
            }

            override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {
            }

        }
        return factory!!.createPeerConnection(rtcConfiguration, observer)!!
    }

    private fun createVideoCapturer(): VideoCapturer? {
        return if (useCamera2()) {
            createCameraCapturer(Camera2Enumerator(this))
        } else {
            createCameraCapturer(Camera1Enumerator(true))
        }
    }

    private fun useCamera2(): Boolean {
        return Camera2Enumerator.isSupported(this)
    }

    private fun createCameraCapturer(cameraEnumerator: CameraEnumerator): VideoCapturer? {
        val deviceNames: Array<String> = cameraEnumerator.deviceNames

        for (deviceName in deviceNames) {
            if (cameraEnumerator.isFrontFacing(deviceName)) {
                val videoCapturer = cameraEnumerator.createCapturer(deviceName, null)
                if (videoCapturer != null) {
                    return videoCapturer
                }
            }
        }
        for (deviceName in deviceNames) {
            if (!cameraEnumerator.isFrontFacing(deviceName)) {
                return cameraEnumerator.createCapturer(deviceName, null)
            }
        }
        return null

    }
}