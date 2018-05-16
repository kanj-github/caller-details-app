package com.kanj.apps.callercontact

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.support.v4.app.NotificationCompat
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.text.TextUtils
import com.kanj.apps.callercontact.Constants.Companion.NOTIFICATION_CHANNEL

class IncomingCallReceiver : BroadcastReceiver() {
    companion object {
        val ACTION_EXPECTED_BROADCAST = "android.intent.action.PHONE_STATE"
        val PROJECTION = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup.LOOKUP_KEY)
        val PROJECTION_FOR_DETAILS = arrayOf(
                ContactsContract.Contacts.Data.MIMETYPE,
                ContactsContract.Contacts.Data.DATA1,
                ContactsContract.Contacts.Data.DATA2,
                ContactsContract.Contacts.Data.DATA3,
                ContactsContract.Contacts.Data.DATA4,
                ContactsContract.Contacts.Data.DATA5,
                ContactsContract.Contacts.Data.DATA6,
                ContactsContract.Contacts.Data.DATA7,
                ContactsContract.Contacts.Data.DATA8,
                ContactsContract.Contacts.Data.DATA9,
                ContactsContract.Contacts.Data.DATA10
        )
        val SELECTION_FOR_DETAILS = ContactsContract.Contacts.LOOKUP_KEY + " = ?"
    }

    private var maskSettings = Constants.MASK_DEFAULT_ENABLE_ALL

    override fun onReceive(context: Context, intent: Intent) {
        if (ACTION_EXPECTED_BROADCAST.equals(intent.action)) {
            val sPref = context.getSharedPreferences(Constants.SHARED_PREFERENCE_FILE, Context.MODE_PRIVATE)
            maskSettings = sPref.getInt(Constants.MASK_SETTINGS_PREFERENCE_NAME, Constants.MASK_DEFAULT_ENABLE_ALL)
            if (maskSettings and Constants.MASK_ENABLE != 0) {
                try {
                    val tmgr = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                    tmgr.listen(MyPhoneStateListener(context, goAsync()), PhoneStateListener.LISTEN_CALL_STATE)
                } catch (e: Exception) {}
            }
        }
    }

    inner class MyPhoneStateListener(val mContext: Context, val pending: PendingResult) : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, incomingNumber: String?) {
            if (state == TelephonyManager.CALL_STATE_RINGING && incomingNumber != null) {
                try {
                    val tmgr = mContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                    tmgr.listen(this, PhoneStateListener.LISTEN_NONE)
                } catch (e: Exception) {}

                handleIncomingCall(mContext, incomingNumber)

                try {
                    pending.finish()
                } catch (e: Exception) {
                    // Maybe "java.lang.IllegalStateException: Broadcast already finished"
                }
            }
        }
    }

    private fun handleIncomingCall(context: Context, phoneNumber: String) {
        val contentResolver = context.contentResolver
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber))
        val cursor = contentResolver.query(uri, PROJECTION, null, null, null)
        if (cursor != null) {
            if (cursor.count > 0) {
                cursor.moveToFirst()
                val name = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME))
                val lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.LOOKUP_KEY))
                val text = StringBuilder()

                val details = contentResolver.query(
                        ContactsContract.Data.CONTENT_URI,
                        PROJECTION_FOR_DETAILS,
                        SELECTION_FOR_DETAILS,
                        arrayOf(lookupKey),
                        null
                )

                if (details != null) {
                    details.moveToPosition(-1)

                    while (details.moveToNext()) {
                        val mimeType = details.getString(details.getColumnIndex(ContactsContract.Contacts.Data.MIMETYPE))
                        when (mimeType) {
                            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE -> if (isSet(Constants.MASK_EMAIL)) {
                                val email = details.getString(details.getColumnIndex(ContactsContract.Contacts.Data.DATA1))
                                appendDetail(text, context.getString(R.string.field_email), email)
                            }

                            ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE -> if (isSet(Constants.MASK_NICKNAME)) {
                                val nickname = details.getString(details.getColumnIndex(ContactsContract.Contacts.Data.DATA1))
                                appendDetail(text, context.getString(R.string.field_nickname), nickname)
                            }

                            ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE -> if (isSet(Constants.MASK_NOTE)) {
                                val note = details.getString(details.getColumnIndex(ContactsContract.Contacts.Data.DATA1))
                                appendDetail(text, context.getString(R.string.field_note), note)
                            }

                            ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE -> {
                                if (isSet(Constants.MASK_ORG)) {
                                    val organization = details.getString(details.getColumnIndex(ContactsContract.Contacts.Data.DATA1))
                                    appendDetail(text, context.getString(R.string.field_org), organization)
                                }
                                if (isSet(Constants.MASK_TITLE)) {
                                    val title = details.getString(details.getColumnIndex(ContactsContract.Contacts.Data.DATA4))
                                    title?.let {
                                        appendDetail(text, context.getString(R.string.field_title), it)
                                    }
                                }
                            }

                            ContactsContract.CommonDataKinds.Relation.CONTENT_ITEM_TYPE -> if (isSet(Constants.MASK_RELATION)) {
                                val relation = details.getString(details.getColumnIndex(ContactsContract.Contacts.Data.DATA1))
                                appendDetail(text, context.getString(R.string.field_relation), relation)
                            }

                            ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE -> if (isSet(Constants.MASK_ADDRESS)) {
                                val type = details.getInt(details.getColumnIndex(ContactsContract.Contacts.Data.DATA2))
                                when (type) {
                                    ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME ->
                                        text.append(context.getString(R.string.field_name_home_address))
                                    ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK ->
                                        text.append(context.getString(R.string.field_name_work_address))
                                    ContactsContract.CommonDataKinds.StructuredPostal.TYPE_OTHER ->
                                        text.append(context.getString(R.string.field_name_other_address))
                                    else -> {
                                        val label = details.getString(details.getColumnIndex(ContactsContract.Contacts.Data.DATA3))
                                        label?.let {
                                            text.append(it).append(":\n")
                                        } ?: text.append(context.getString(R.string.field_name_address))
                                    }
                                }
                                var str = details.getString(details.getColumnIndex(ContactsContract.Contacts.Data.DATA1))
                                text.append(str).append("\n\n")
                                if (isSet(Constants.MASK_STREET)) {
                                    str = details.getString(details.getColumnIndex(ContactsContract.Contacts.Data.DATA4))
                                    str?.let { appendDetail(text, context.getString(R.string.field_street), it) }
                                }
                                if (isSet(Constants.MASK_PO_BOX)) {
                                    str = details.getString(details.getColumnIndex(ContactsContract.Contacts.Data.DATA5))
                                    str?.let { appendDetail(text, context.getString(R.string.field_po_box), it) }
                                }
                                if (isSet(Constants.MASK_HOOD)) {
                                    str = details.getString(details.getColumnIndex(ContactsContract.Contacts.Data.DATA6))
                                    str?.let { appendDetail(text, context.getString(R.string.field_hood), it) }
                                }
                                if (isSet(Constants.MASK_CITY)) {
                                    str = details.getString(details.getColumnIndex(ContactsContract.Contacts.Data.DATA7))
                                    str?.let { appendDetail(text, context.getString(R.string.field_city), it) }
                                }
                                if (isSet(Constants.MASK_REGION)) {
                                    str = details.getString(details.getColumnIndex(ContactsContract.Contacts.Data.DATA8))
                                    str?.let { appendDetail(text, context.getString(R.string.field_region), it) }
                                }
                                if (isSet(Constants.MASK_POSTCODE)) {
                                    str = details.getString(details.getColumnIndex(ContactsContract.Contacts.Data.DATA9))
                                    str?.let { appendDetail(text, context.getString(R.string.field_postcode), it) }
                                }
                                if (isSet(Constants.MASK_COUNTRY)) {
                                    str = details.getString(details.getColumnIndex(ContactsContract.Contacts.Data.DATA10))
                                    str?.let { appendDetail(text, context.getString(R.string.field_country), it) }
                                }
                            }
                        }
                    }

                    details.close()
                    if (TextUtils.isEmpty(text)) {
                        text.append(context.getString(R.string.no_details_found))
                    }
                    showNotification(context, name, text.toString())
                }
            }
            cursor.close()
        }
    }

    private fun isSet(mask: Int) = maskSettings and mask != 0

    private fun appendDetail(stringBuilder: StringBuilder, detailField: String, detailValue: String) {
        if (!TextUtils.isEmpty(detailValue)) {
            stringBuilder.append(detailField).append(detailValue).append("\n")
        }
    }

    private fun showNotification(context: Context, name: String, text: String) {
        val i = Intent(context, ShowDialogActivity::class.java)
        i.putExtra(ShowDialogActivity.EXTRA_NAME, name)
        i.putExtra(ShowDialogActivity.EXTRA_TEXT, text)
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val nBuilder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_contact_phone_black_24dp)
                .setContentTitle(context.getString(R.string.noti_title, name))
                .setContentText(text)
                .setContentIntent(PendingIntent.getActivity(
                        context,
                        0,
                        i,
                        PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_UPDATE_CURRENT
                ))
        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL,
                    NOTIFICATION_CHANNEL, NotificationManager.IMPORTANCE_DEFAULT)
            mNotificationManager.createNotificationChannel(notificationChannel)
        }
        mNotificationManager.notify(Constants.NOTIFICATION_ID, nBuilder.build())
    }
}