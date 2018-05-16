package com.kanj.apps.callercontact

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity

class ShowDialogActivity : AppCompatActivity() {
    companion object {
        val EXTRA_NAME = "EXTRA_NAME"
        val EXTRA_TEXT = "EXTRA_TEXT"
    }

    private lateinit var name: String
    private lateinit var text: String
    private var dialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = getIntent()
        name = intent.getStringExtra(EXTRA_NAME)
        text = intent.getStringExtra(EXTRA_TEXT)

        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.cancel(Constants.NOTIFICATION_ID)
    }

    override fun onResume() {
        super.onResume()
        if (dialog == null) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage(text).setTitle(name)
            builder.setPositiveButton(R.string.ok, {dialog, _ ->
                dialog.dismiss()
                finish()
            })
            builder.setOnCancelListener({
                it.dismiss()
                finish()
            })
            with(builder.create()) {
                dialog = this
                this.setCanceledOnTouchOutside(false)
                this.show()
            }
        }
    }
}