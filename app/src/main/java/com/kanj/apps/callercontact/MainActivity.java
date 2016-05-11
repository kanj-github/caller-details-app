package com.kanj.apps.callercontact;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

// This activity should acquire all the permissions
public class MainActivity extends AppCompatActivity {
    private static final String[] permissions = {Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.READ_CONTACTS};

    private TextView text;
    private boolean requestPermission;
    private ListView listView;
    private Switch enableSwitch;
    private SettingsListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text = (TextView) findViewById(R.id.text);
        listView = (ListView) findViewById(R.id.list);
        enableSwitch = (Switch) findViewById(R.id.enable_switch);

        requestPermission = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean check = true;

        for (String permission: permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                check = false;
            }
        }

        if (!check && requestPermission) {
            ActivityCompat.requestPermissions(this, permissions, 100);
            requestPermission = false;
        } else {
            text.setText(R.string.all_set);
            SharedPreferences sPref = getSharedPreferences(Constants.SHARED_PREFERENCE_FILE, MODE_PRIVATE);
            int maskSettings = sPref.getInt(Constants.MASK_SETTINGS_PREFERENCE_NAME, Constants.MASK_DEFAULT_ENABLE_ALL);

            enableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        listView.setVisibility(View.VISIBLE);
                    } else {
                        listView.setVisibility(View.INVISIBLE);
                    }
                }
            });
            if ((maskSettings & 1) == 1) {
                enableSwitch.setChecked(true);
            } else {
                enableSwitch.setChecked(false);
            }

            mAdapter = new SettingsListAdapter(this, getResources().getStringArray(R.array.setting_items), maskSettings);
            listView.setAdapter(mAdapter);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAdapter != null) {
            int mask = mAdapter.mask;
            if (enableSwitch.isChecked()) {
                mask |= 1;
            } else {
                mask &= 0xfffffffe;
            }

            Log.v("Kanj", "Saving " + Integer.toHexString(mask));
            SharedPreferences sPref = getSharedPreferences(Constants.SHARED_PREFERENCE_FILE, MODE_PRIVATE);
            SharedPreferences.Editor sPrefEditor = sPref.edit();
            sPrefEditor.putInt(Constants.MASK_SETTINGS_PREFERENCE_NAME, mask).apply();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (permissions.length != grantResults.length) {
            Log.v("Kanj", "weird bug");
            return;
        }

        boolean check = true;
        for (int i = 0; i< permissions.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                check = false;
                text.append(getString(R.string.not_granted, permissions[i]));
            }
        }

        if (check) {
            text.setText(R.string.all_set);
        }
    }
}
