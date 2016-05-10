package com.kanj.apps.callercontact;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Created by kanj on 6/5/16.
 */
public class IncomingCallReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v("Kanj", "Got broadcast");
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
