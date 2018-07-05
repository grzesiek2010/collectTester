package org.odk.collectTester.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.odk.collectTester.R;
import org.odk.collectTester.utilities.Constants;

public class FormDownloadActivity extends AppCompatActivity {

    private TextView statusTv;
    private EditText formIdEdtv;
    private BroadcastReceiver broadcastReceiver;
    private String formId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_download);

        formIdEdtv = (EditText) findViewById(R.id.form_id_edt);
        statusTv = (TextView) findViewById(R.id.status_tv);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                boolean success = intent.getBooleanExtra(Constants.BundleKeys.SUCCESS_KEY, false);
                String errorReason = intent.getStringExtra(Constants.BundleKeys.ERROR_REASON);
                String resultFormId = intent.getStringExtra(Constants.BundleKeys.FORM_ID);

                String status = "DOWNLOAD STATUS: " + (success ? "SUCCESSFUL" : "FAILED") + "\nFORM "
                        + ((resultFormId == null) ? "NULL" : resultFormId ) + "\n" + (!success ? "ERROR REASON : " + errorReason : "");
                updateStatus(status);

                unregisterReceiver(broadcastReceiver);
            }
        };
    }

    private void updateStatus(String text) {
        statusTv.setText(text);
    }

    public void downloadForm(View view) {
        formId = formIdEdtv.getText().toString();

        Intent formDownloadIntent = new Intent();
        formDownloadIntent.setClassName(Constants.COLLECT_PACKAGE_NAME, Constants.FORM_DOWNLOAD_SERVICE_NAME);
        formDownloadIntent.putExtra(Constants.BundleKeys.FORM_ID, formId);

        startService(formDownloadIntent);
        updateStatus("FORM " + ((formId == null) ? "NULL" : formId ) + ": Download requested");
        registerReceiver(broadcastReceiver, new IntentFilter(Constants.FORM_DOWNLOAD_BROADCAST_ACTION));
    }
}
