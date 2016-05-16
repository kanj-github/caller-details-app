package com.kanj.apps.callercontact;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Created by kanj on 6/5/16.
 */
public class IncomingCallReceiver extends WakefulBroadcastReceiver {
    int maskSettings;
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v("Kanj", "Got broadcast");
        SharedPreferences sPref = context.getSharedPreferences(Constants.SHARED_PREFERENCE_FILE, Context.MODE_PRIVATE);
        maskSettings = sPref.getInt(Constants.MASK_SETTINGS_PREFERENCE_NAME, Constants.MASK_DEFAULT_ENABLE_ALL);
        if ((maskSettings & Constants.MASK_ENABLE) == 0) {
            // Not enabled
            return;
        }

        try {
            TelephonyManager tmgr = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            MyPhoneStateListener mPhoneListener = new MyPhoneStateListener(context);
            tmgr.listen(mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class MyPhoneStateListener extends PhoneStateListener {
        private Context mContext;

        public MyPhoneStateListener(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            Log.v("Kanj", "Got call state change, number="+incomingNumber);
            if (state == TelephonyManager.CALL_STATE_RINGING && incomingNumber != null) {
                Intent i = new Intent(mContext, GetDataAndShowIntentService.class);
                i.setAction(GetDataAndShowIntentService.ACTION_GET_DATA_AND_SHOW);
                i.putExtra(GetDataAndShowIntentService.EXTRA_PHONE_NUMBER, incomingNumber);
                i.putExtra(GetDataAndShowIntentService.EXTRA_MASK_SETTINGS, maskSettings);
                startWakefulService(mContext, i);
            }
            stopListeningForCallStateChanges();
        }

        private void stopListeningForCallStateChanges() {
            try {
                TelephonyManager tmgr = (TelephonyManager) mContext
                        .getSystemService(Context.TELEPHONY_SERVICE);
                tmgr.listen(this, PhoneStateListener.LISTEN_NONE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
