package com.kanj.apps.callercontact;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

// This activity should acquire all the permissions
public class MainActivity extends AppCompatActivity {
    private static final String[] permissions = {Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.READ_CONTACTS};

    private TextView text;
    private boolean requestPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text = (TextView) findViewById(R.id.text);
        requestPermission = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean check = true;

        for (String permission: permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                check = false;
            } else {
                Log.v("Kanj",permission + " granted");
            }
        }

        if (!check && requestPermission) {
            ActivityCompat.requestPermissions(this, permissions, 100);
            requestPermission = false;
        } else {
            text.setText(R.string.all_set);
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
