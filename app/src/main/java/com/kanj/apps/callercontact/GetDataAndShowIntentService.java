package com.kanj.apps.callercontact;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.provider.ContactsContract.Contacts.Data;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class GetDataAndShowIntentService extends IntentService {
    public static final String ACTION_GET_DATA_AND_SHOW = "com.kanj.apps.callercontact.action.GET_DATA_AND_SHOW";
    public static final String EXTRA_PHONE_NUMBER = "com.kanj.apps.callercontact.extra.PHONE_NUMBER";
    public static final String EXTRA_MASK_SETTINGS = "com.kanj.apps.callercontact.extra.MASK_SETTINGS";

    private static final String PROJECTION[] = {ContactsContract.PhoneLookup.DISPLAY_NAME,
            ContactsContract.PhoneLookup.LOOKUP_KEY};
    private static final String[] PROJECTION_FOR_DETAILS = {
            Data.MIMETYPE,
            Data.DATA1,
            Data.DATA2,
            Data.DATA3,
            Data.DATA4,
            Data.DATA5,
            Data.DATA6,
            Data.DATA7,
            Data.DATA8,
            Data.DATA9,
            Data.DATA10/*,
            Data.DATA11,
            Data.DATA12,
            Data.DATA13,
            Data.DATA14,
            Data.DATA15*/
    };
    private static final String SELECTION_FOR_DETAILS = ContactsContract.Contacts.LOOKUP_KEY + " = ?";

    private Intent i;

    public GetDataAndShowIntentService() {
        super("GetDataAndShowIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GET_DATA_AND_SHOW.equals(action)) {
                i = intent;
                final String phoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER);
                final int settings = intent.getIntExtra(EXTRA_MASK_SETTINGS, Constants.MASK_DEFAULT_ENABLE_ALL);
                handleAction(phoneNumber, settings);
            }
        }
    }

    private void handleAction(String phoneNumber, int settings) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        ContentResolver contentResolver = getContentResolver();
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
                    String mimeType = details.getString(details.getColumnIndex(Data.MIMETYPE));
                    if (ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE.equals(mimeType)
                            && (settings & Constants.MASK_EMAIL) != 0) {
                        String email = details.getString(details.getColumnIndex(Data.DATA1));
                        text.append(getString(R.string.field_email)).append(email).append("\n");
                    } else if (ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE.equals(mimeType)
                            && (settings & Constants.MASK_NICKNAME) != 0) {
                        String nickname = details.getString(details.getColumnIndex(Data.DATA1));
                        text.append(getString(R.string.field_nickname)).append(nickname).append("\n");
                    } else if (ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE.equals(mimeType)
                            && (settings & Constants.MASK_NOTE) != 0) {
                        String note = details.getString(details.getColumnIndex(Data.DATA1));
                        text.append(getString(R.string.field_note)).append(note).append("\n");
                    } else if (ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE.equals(mimeType)) {
                        if ((settings & Constants.MASK_ORG) != 0) {
                            String organization = details.getString(details.getColumnIndex(Data.DATA1));
                            text.append(getString(R.string.field_org)).append(organization).append("\n");
                        }

                        if ((settings & Constants.MASK_TITLE) != 0) {
                            String title = details.getString(details.getColumnIndex(Data.DATA4));
                            if (title != null) {
                                text.append(getString(R.string.field_title)).append(title).append("\n");
                            }
                        }
                    }  else if (ContactsContract.CommonDataKinds.Relation.CONTENT_ITEM_TYPE.equals(mimeType)
                            && (settings & Constants.MASK_RELATION) != 0) {
                        String relation = details.getString(details.getColumnIndex(Data.DATA1));
                        text.append(getString(R.string.field_relation)).append(relation).append("\n");
                    }  else if (ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE.equals(mimeType)
                            && (settings & Constants.MASK_ADDRESS) != 0) {
                        int type = details.getInt(details.getColumnIndex(Data.DATA2));
                        switch (type) {
                            case ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME:
                                text.append(getString(R.string.field_name_home_address));
                                break;
                            case ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK:
                                text.append(getString(R.string.field_name_work_address));
                                break;
                            case ContactsContract.CommonDataKinds.StructuredPostal.TYPE_OTHER:
                                text.append(getString(R.string.field_name_other_address));
                                break;
                            default:
                                String label = details.getString(details.getColumnIndex(Data.DATA3));
                                if (label != null) {
                                    text.append(label).append(":\n");
                                } else {
                                    text.append(getString(R.string.field_name_address));
                                }
                        }
                        String str = details.getString(details.getColumnIndex(Data.DATA1));
                        text.append(str).append("\n\n");

                        if ((settings & Constants.MASK_STREET) != 0) {
                            str = details.getString(details.getColumnIndex(Data.DATA4));
                            if (str != null) {
                                text.append(getString(R.string.field_street)).append(str).append("\n");
                            }
                        }

                        if ((settings & Constants.MASK_PO_BOX) != 0) {
                            str = details.getString(details.getColumnIndex(Data.DATA5));
                            if (str != null) {
                                text.append(getString(R.string.field_po_box)).append(str).append("\n");
                            }
                        }

                        if ((settings & Constants.MASK_HOOD) != 0) {
                            str = details.getString(details.getColumnIndex(Data.DATA6));
                            if (str != null) {
                                text.append(getString(R.string.field_hood)).append(str).append("\n");
                            }
                        }

                        if ((settings & Constants.MASK_CITY) != 0) {
                            str = details.getString(details.getColumnIndex(Data.DATA7));
                            if (str != null) {
                                text.append(getString(R.string.field_city)).append(str).append("\n");
                            }
                        }

                        if ((settings & Constants.MASK_REGION) != 0) {
                            str = details.getString(details.getColumnIndex(Data.DATA8));
                            if (str != null) {
                                text.append(getString(R.string.field_region)).append(str).append("\n");
                            }
                        }

                        if ((settings & Constants.MASK_POSTCODE) != 0) {
                            str = details.getString(details.getColumnIndex(Data.DATA9));
                            if (str != null) {
                                text.append(getString(R.string.field_postcode)).append(str).append("\n");
                            }
                        }

                        if ((settings & Constants.MASK_COUNTRY) != 0) {
                            str = details.getString(details.getColumnIndex(Data.DATA10));
                            if (str != null) {
                                text.append(getString(R.string.field_country)).append(str).append("\n");
                            }
                        }
                    }
                }
                details.close();
                Intent i = new Intent(this, ShowDialogActivity.class);
                i.putExtra(ShowDialogActivity.EXTRA_NAME, name);
                i.putExtra(ShowDialogActivity.EXTRA_TEXT, text.toString());
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_contact_phone_black_24dp)
                        .setContentTitle(getString(R.string.noti_title, name))
                        .setContentText(text.toString())
                        .setContentIntent(PendingIntent.getActivity(
                                this,
                                0,
                                i,
                                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_UPDATE_CURRENT
                                )
                        );
                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(Constants.NOTIFICATION_ID, nBuilder.build());
            }
            c.close();

            /*try {
                Thread.sleep(7000);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }*/

            IncomingCallReceiver.completeWakefulIntent(i);
        }
    }
}
