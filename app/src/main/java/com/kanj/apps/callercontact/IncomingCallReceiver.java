package com.kanj.apps.callercontact;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import static com.kanj.apps.callercontact.Constants.NOTIFICATION_CHANNEL;

/**
 * Created by kanj on 6/5/16.
 */
public class IncomingCallReceiver extends BroadcastReceiver {
    private static final String ACTION_EXPECTED_BROADCAST = "android.intent.action.PHONE_STATE";

    private static final String PROJECTION[] = {ContactsContract.PhoneLookup.DISPLAY_NAME,
            ContactsContract.PhoneLookup.LOOKUP_KEY};
    private static final String[] PROJECTION_FOR_DETAILS = {
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
            ContactsContract.Contacts.Data.DATA10/*,
            Data.DATA11,
            Data.DATA12,
            Data.DATA13,
            Data.DATA14,
            Data.DATA15*/
    };
    private static final String SELECTION_FOR_DETAILS = ContactsContract.Contacts.LOOKUP_KEY + " = ?";

    private int maskSettings;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!ACTION_EXPECTED_BROADCAST.equals(intent.getAction())) {
            return;
        }

        SharedPreferences sPref = context.getSharedPreferences(Constants.SHARED_PREFERENCE_FILE, Context.MODE_PRIVATE);
        maskSettings = sPref.getInt(Constants.MASK_SETTINGS_PREFERENCE_NAME, Constants.MASK_DEFAULT_ENABLE_ALL);
        if ((maskSettings & Constants.MASK_ENABLE) == 0) {
            // Not enabled
            return;
        }

        try {
            TelephonyManager tmgr = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            MyPhoneStateListener mPhoneListener = new MyPhoneStateListener(context, goAsync());
            tmgr.listen(mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
        } catch (Exception e) {
        }
    }

    class MyPhoneStateListener extends PhoneStateListener {
        private Context mContext;
        private PendingResult pending;

        public MyPhoneStateListener(Context mContext, PendingResult pending) {
            this.mContext = mContext;
            this.pending = pending;
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            if (state == TelephonyManager.CALL_STATE_RINGING && incomingNumber != null) {
                stopListeningForCallStateChanges();
                handleIncomingCall(mContext, incomingNumber);
            }

            try {
                pending.finish();
            } catch (Exception e) {
                // Maybe "java.lang.IllegalStateException: Broadcast already finished"
            }
        }

        private void stopListeningForCallStateChanges() {
            try {
                TelephonyManager tmgr = (TelephonyManager) mContext
                        .getSystemService(Context.TELEPHONY_SERVICE);
                tmgr.listen(this, PhoneStateListener.LISTEN_NONE);
            } catch (Exception e) {
            }
        }
    }

    private void handleIncomingCall(Context context, String phoneNumber) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor c = contentResolver.query(uri, PROJECTION, null, null, null);

        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            String name = c.getString(c.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            String lookupKey = c.getString(c.getColumnIndex(ContactsContract.PhoneLookup.LOOKUP_KEY));
            StringBuilder text = new StringBuilder();

            Cursor details = contentResolver.query(
                    ContactsContract.Data.CONTENT_URI,
                    PROJECTION_FOR_DETAILS,
                    SELECTION_FOR_DETAILS,
                    new String[]{lookupKey},
                    null
            );

            if (details != null) {
                details.moveToPosition(-1);
                while (details.moveToNext()) {
                    String mimeType = details.getString(details.getColumnIndex(ContactsContract.Contacts.Data.MIMETYPE));
                    if (ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE.equals(mimeType)
                            && (maskSettings & Constants.MASK_EMAIL) != 0) {
                        String email = details.getString(details.getColumnIndex(ContactsContract.Contacts.Data.DATA1));
                        appendDetail(text, context.getString(R.string.field_email), email);
                    } else if (ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE.equals(mimeType)
                            && (maskSettings & Constants.MASK_NICKNAME) != 0) {
                        String nickname = details.getString(details.getColumnIndex(ContactsContract.Contacts.Data.DATA1));
                        appendDetail(text, context.getString(R.string.field_nickname), nickname);
                    } else if (ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE.equals(mimeType)
                            && (maskSettings & Constants.MASK_NOTE) != 0) {
                        String note = details.getString(details.getColumnIndex(ContactsContract.Contacts.Data.DATA1));
                        appendDetail(text, context.getString(R.string.field_note), note);
                    } else if (ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE.equals(mimeType)) {
                        if ((maskSettings & Constants.MASK_ORG) != 0) {
                            String organization = details.getString(details.getColumnIndex(ContactsContract.Contacts.Data.DATA1));
                            appendDetail(text, context.getString(R.string.field_org), organization);
                        }

                        if ((maskSettings & Constants.MASK_TITLE) != 0) {
                            String title = details.getString(details.getColumnIndex(ContactsContract.Contacts.Data.DATA4));
                            if (title != null) {
                                appendDetail(text, context.getString(R.string.field_title), title);
                            }
                        }
                    }  else if (ContactsContract.CommonDataKinds.Relation.CONTENT_ITEM_TYPE.equals(mimeType)
                            && (maskSettings & Constants.MASK_RELATION) != 0) {
                        String relation = details.getString(details.getColumnIndex(ContactsContract.Contacts.Data.DATA1));
                        appendDetail(text, context.getString(R.string.field_relation), relation);
                    }  else if (ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE.equals(mimeType)
                            && (maskSettings & Constants.MASK_ADDRESS) != 0) {
                        int type = details.getInt(details.getColumnIndex(ContactsContract.Contacts.Data.DATA2));
                        switch (type) {
                            case ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME:
                                text.append(context.getString(R.string.field_name_home_address));
                                break;
                            case ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK:
                                text.append(context.getString(R.string.field_name_work_address));
                                break;
                            case ContactsContract.CommonDataKinds.StructuredPostal.TYPE_OTHER:
                                text.append(context.getString(R.string.field_name_other_address));
                                break;
                            default:
                                String label = details.getString(details.getColumnIndex(ContactsContract.Contacts.Data.DATA3));
                                if (label != null) {
                                    text.append(label).append(":\n");
                                } else {
                                    text.append(context.getString(R.string.field_name_address));
                                }
                        }
                        String str = details.getString(details.getColumnIndex(ContactsContract.Contacts.Data.DATA1));
                        text.append(str).append("\n\n");

                        if ((maskSettings & Constants.MASK_STREET) != 0) {
                            str = details.getString(details.getColumnIndex(ContactsContract.Contacts.Data.DATA4));
                            if (str != null) {
                                appendDetail(text, context.getString(R.string.field_street), str);
                            }
                        }

                        if ((maskSettings & Constants.MASK_PO_BOX) != 0) {
                            str = details.getString(details.getColumnIndex(ContactsContract.Contacts.Data.DATA5));
                            if (str != null) {
                                appendDetail(text, context.getString(R.string.field_po_box), str);
                            }
                        }

                        if ((maskSettings & Constants.MASK_HOOD) != 0) {
                            str = details.getString(details.getColumnIndex(ContactsContract.Contacts.Data.DATA6));
                            if (str != null) {
                                appendDetail(text, context.getString(R.string.field_hood), str);
                            }
                        }

                        if ((maskSettings & Constants.MASK_CITY) != 0) {
                            str = details.getString(details.getColumnIndex(ContactsContract.Contacts.Data.DATA7));
                            if (str != null) {
                                appendDetail(text, context.getString(R.string.field_city), str);
                            }
                        }

                        if ((maskSettings & Constants.MASK_REGION) != 0) {
                            str = details.getString(details.getColumnIndex(ContactsContract.Contacts.Data.DATA8));
                            if (str != null) {
                                appendDetail(text, context.getString(R.string.field_region), str);
                            }
                        }

                        if ((maskSettings & Constants.MASK_POSTCODE) != 0) {
                            str = details.getString(details.getColumnIndex(ContactsContract.Contacts.Data.DATA9));
                            if (str != null) {
                                appendDetail(text, context.getString(R.string.field_postcode), str);
                            }
                        }

                        if ((maskSettings & Constants.MASK_COUNTRY) != 0) {
                            str = details.getString(details.getColumnIndex(ContactsContract.Contacts.Data.DATA10));
                            if (str != null) {
                                appendDetail(text, context.getString(R.string.field_country), str);
                            }
                        }
                    }
                }
                details.close();

                if (TextUtils.isEmpty(text)) {
                    text.append(context.getString(R.string.no_details_found));
                }

                showNotification(context, name, text.toString());
            }
            c.close();
        }
    }

    private void appendDetail(StringBuilder stringBuilder, String detailField,
                              String detailValue) {
        if (!TextUtils.isEmpty(detailValue)) {
            stringBuilder.append(detailField).append(detailValue).append("\n");
        }
    }

    private void showNotification(Context context, String name, String text) {
        Intent i = new Intent(context, ShowDialogActivity.class);
        i.putExtra(ShowDialogActivity.EXTRA_NAME, name);
        i.putExtra(ShowDialogActivity.EXTRA_TEXT, text);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_contact_phone_black_24dp)
                .setContentTitle(context.getString(R.string.noti_title, name))
                .setContentText(text)
                .setContentIntent(PendingIntent.getActivity(
                        context,
                        0,
                        i,
                        PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_UPDATE_CURRENT
                        )
                );
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (mNotificationManager == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL,
                    NOTIFICATION_CHANNEL, NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }

        mNotificationManager.notify(Constants.NOTIFICATION_ID, nBuilder.build());
    }
}
