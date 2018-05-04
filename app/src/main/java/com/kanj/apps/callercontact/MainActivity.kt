package com.kanj.apps.callercontact

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    companion object {
        val permissions = arrayOf(Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.READ_CONTACTS)
    }

    private var requestPermission = true
    private var mAdapter: SettingsListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()

        var check = true
        permissions.forEach {
            if (ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED) {
                check = false
            }
        }

        if (!check && requestPermission) {
            ActivityCompat.requestPermissions(this, permissions, 100)
            requestPermission = false
        } else {
            text.setText(R.string.all_set)
            val sPref = getSharedPreferences(Constants.SHARED_PREFERENCE_FILE, Context.MODE_PRIVATE)
            val maskSettings = sPref.getInt(Constants.MASK_SETTINGS_PREFERENCE_NAME, Constants.MASK_DEFAULT_ENABLE_ALL)
            enable_switch.setOnCheckedChangeListener({_, isChecked ->
                list.visibility = if (isChecked) {
                    View.VISIBLE
                } else {
                    View.INVISIBLE
                }
            })

            enable_switch.isChecked = (maskSettings.and(1)) == 1
            mAdapter = SettingsListAdapter(this, resources.getStringArray(R.array.setting_items), maskSettings)
            list.adapter = mAdapter
        }

    }

    override fun onPause() {
        super.onPause()

        mAdapter?.let{
            var mask = it.mask
            if (enable_switch.isChecked) {
                mask = mask or 1
            } else {
                mask = mask and 0xfffffffe.toInt()
            }
            val sPrefEditor = getSharedPreferences(Constants.SHARED_PREFERENCE_FILE, Context.MODE_PRIVATE).edit()
            sPrefEditor.putInt(Constants.MASK_SETTINGS_PREFERENCE_NAME, mask).apply()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (permissions.size == grantResults.size) {
            var check = true
            grantResults.forEachIndexed { index, result ->
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check = false
                    text.append(getString(R.string.not_granted, permissions[index]))
                }
            }

            if (check) {
                text.setText(R.string.all_set)
            }
        }
    }
}