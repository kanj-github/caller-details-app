package com.kanj.apps.callercontact;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

/**
 * Created by kanj on 11/4/16.
 */
public class ShowDialogActivity extends AppCompatActivity {
    public static final String EXTRA_NAME = "EXTRA_NAME";
    public static final String EXTRA_TEXT = "EXTRA_TEXT";

    private String name, text;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        name = i.getStringExtra(EXTRA_NAME);
        text = i.getStringExtra(EXTRA_TEXT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dialog != null) {
            // Do not make dialog again
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(text).setTitle(name);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                finish();
            }
        });
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                finish();
            }
        });
        //dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();
    }
}
