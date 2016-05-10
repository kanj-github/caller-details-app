package com.kanj.apps.callercontact;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.provider.ContactsContract.Contacts.Data;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class GetDataAndShowIntentService extends IntentService {
    public static final String ACTION_GET_DATA_AND_SHOW = "com.kanj.apps.callercontact.action.GET_DATA_AND_SHOW";
    public static final String EXTRA_PHONE_NUMBER = "com.kanj.apps.callercontact.extra.PHONE_NUMBER";
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
                handleAction(phoneNumber);
            }
        }
    }

    private void handleAction(String phoneNumber) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        ContentResolver contentResolver = getContentResolver();
        Cursor c = contentResolver.query(uri, PROJECTION, null, null, null);
        if (c != null) {
            c.moveToFirst();
            String name = c.getString(c.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            String lookupKey = c.getString(c.getColumnIndex(ContactsContract.PhoneLookup.LOOKUP_KEY));
            StringBuffer text = new StringBuffer();

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
                    if (ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE.equals(mimeType)) {
                        String email = details.getString(details.getColumnIndex(Data.DATA1));
                        text.append("Email ID: ").append(email).append("\n");
                    } else if (ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE.equals(mimeType)) {
                        String nickname = details.getString(details.getColumnIndex(Data.DATA1));
                        text.append("Nickname: ").append(nickname).append("\n");
                    } else if (ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE.equals(mimeType)) {
                        String note = details.getString(details.getColumnIndex(Data.DATA1));
                        text.append("Note: ").append(note).append("\n");
                    } else if (ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE.equals(mimeType)) {
                        String organization = details.getString(details.getColumnIndex(Data.DATA1));
                        text.append("Organization: ").append(organization).append("\n");
                        String title = details.getString(details.getColumnIndex(Data.DATA4));
                        if (title != null) {
                            text.append("Title: ").append(title).append("\n");
                        }
                    }  else if (ContactsContract.CommonDataKinds.Relation.CONTENT_ITEM_TYPE.equals(mimeType)) {
                        String relation = details.getString(details.getColumnIndex(Data.DATA1));
                        text.append("Relation: ").append(relation).append("\n");
                    }  else if (ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE.equals(mimeType)) {
                        int type = details.getInt(details.getColumnIndex(Data.DATA2));
                        switch (type) {
                            case ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME:
                                text.append("Home address:\n");
                                break;
                            case ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK:
                                text.append("Work address:\n");
                                break;
                            case ContactsContract.CommonDataKinds.StructuredPostal.TYPE_OTHER:
                                text.append("Other address:\n");
                                break;
                            default:
                                String label = details.getString(details.getColumnIndex(Data.DATA3));
                                if (label != null) {
                                    text.append(label).append(":\n");
                                } else {
                                    text.append("Address:\n");
                                }
                        }
                        String str = details.getString(details.getColumnIndex(Data.DATA1));
                        text.append(str).append("\n\n");
                        str = details.getString(details.getColumnIndex(Data.DATA4));
                        if (str != null) {
                            text.append("Street: ").append(str).append("\n");
                        }
                        str = details.getString(details.getColumnIndex(Data.DATA5));
                        if (str != null) {
                            text.append("PO Box: ").append(str).append("\n");
                        }
                        str = details.getString(details.getColumnIndex(Data.DATA6));
                        if (str != null) {
                            text.append("Neighbourhood: ").append(str).append("\n");
                        }
                        str = details.getString(details.getColumnIndex(Data.DATA7));
                        if (str != null) {
                            text.append("City: ").append(str).append("\n");
                        }
                        str = details.getString(details.getColumnIndex(Data.DATA8));
                        if (str != null) {
                            text.append("Region: ").append(str).append("\n");
                        }
                        str = details.getString(details.getColumnIndex(Data.DATA9));
                        if (str != null) {
                            text.append("Postcode: ").append(str).append("\n");
                        }
                        str = details.getString(details.getColumnIndex(Data.DATA10));
                        if (str != null) {
                            text.append("Country: ").append(str).append("\n");
                        }
                    }
                }
                details.close();
            }
            c.close();

            Intent i = new Intent(this, ShowDialogActivity.class);
            i.putExtra(ShowDialogActivity.EXTRA_NAME, name);
            i.putExtra(ShowDialogActivity.EXTRA_TEXT, text.toString());
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);

            /*try {
                Thread.sleep(7000);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }*/

            IncomingCallReceiver.completeWakefulIntent(i);
        }
    }
}
