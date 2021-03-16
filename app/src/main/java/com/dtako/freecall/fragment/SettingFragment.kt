package com.dtako.freecall.fragment

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.dtako.freecall.R


class SettingFragment : PreferenceFragmentCompat() {


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.fragment_setting, rootKey)
    }

}