package com.dtako.freecall.web_rtc

import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

/**
 * Created by Nguyen Kim Khanh on 3/5/2021.
 */
open class SimpleSdpObserver : SdpObserver {
    override fun onCreateSuccess(p0: SessionDescription?) {

    }

    override fun onSetSuccess() {
    }

    override fun onCreateFailure(p0: String?) {
    }

    override fun onSetFailure(p0: String?) {
    }
}