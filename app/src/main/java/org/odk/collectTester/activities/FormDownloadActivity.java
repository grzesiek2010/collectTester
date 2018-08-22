package org.odk.collectTester.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.odk.collectTester.R;
import org.odk.collectTester.utilities.Constants;
import org.odk.collectTester.utilities.FormDownloadStatus;

import java.util.HashMap;
import java.util.UUID;

public class FormDownloadActivity extends AppCompatActivity {

    private TextView statusTv;
    private EditText formIdEdtv;
    private BroadcastReceiver broadcastReceiver;
    private String formId;

    private HashMap<String, FormDownloadDetails> downloadQueue = new HashMap<>();

    private boolean downloadInBackground = true;

    public static final int PROGRESS_REQUEST_RECEIVED = 1;
    public static final int PROGRESS_REQUEST_BEING_PROCESSED = 2;
    public static final int PROGRESS_REQUEST_SATISFIED = 3;

    private static final String DOWNLOAD_TEXT_KEY = "download_text";
    private static final String DOWNLOAD_QUEUE_KEY = "download_queue";
    private static final String DOWNLOAD_MODE_FLAG_KEY = "download_mode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_download);

        formIdEdtv = (EditText) findViewById(R.id.form_id_edt);
        statusTv = (TextView) findViewById(R.id.status_tv);

        if (savedInstanceState != null) {
            statusTv.setText(savedInstanceState.getString(DOWNLOAD_TEXT_KEY));
            downloadInBackground = savedInstanceState.getBoolean(DOWNLOAD_MODE_FLAG_KEY);
            downloadQueue = (HashMap<String, FormDownloadDetails>) savedInstanceState.getSerializable(DOWNLOAD_QUEUE_KEY);
        }

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                boolean success = intent.getBooleanExtra(Constants.BundleKeys.SUCCESS_KEY, false);
                int progressStage = intent.getIntExtra(Constants.BundleKeys.PROGRESS_STAGE, -1);
                String transactionId = intent.getStringExtra(Constants.BundleKeys.TRANSACTION_ID);

                if (transactionId != null && downloadQueue.containsKey(transactionId)) {

                    // FormDownloadStatus formDownloadStatus = (progressStage == PROGRESS_REQUEST_RECEIVED) ? FormDownloadStatus.DOWNLOAD_REQUEST_RECEIVED : success ? FormDownloadStatus.DOWNLOAD_SUCCEEDED : FormDownloadStatus.DOWNLOAD_FAILED;
                    FormDownloadStatus formDownloadStatus = (progressStage == PROGRESS_REQUEST_RECEIVED) ? FormDownloadStatus.DOWNLOAD_REQUEST_RECEIVED
                            : progressStage == PROGRESS_REQUEST_BEING_PROCESSED ? FormDownloadStatus.DOWNLOAD_STARTED
                            : success ? FormDownloadStatus.DOWNLOAD_FAILED : FormDownloadStatus.DOWNLOAD_SUCCEEDED;

                    String status = "DOWNLOAD STATUS: ";

                    // Update the download queue
                    FormDownloadDetails formDownloadDetails = downloadQueue.get(transactionId);
                    formDownloadDetails.formDownloadStatus = formDownloadStatus;
                    downloadQueue.put(transactionId, formDownloadDetails);

                    String errorReason = intent.getStringExtra(Constants.BundleKeys.ERROR_REASON);
                    String resultFormId = intent.getStringExtra(Constants.BundleKeys.FORM_ID);

                    if (formDownloadStatus.equals(FormDownloadStatus.DOWNLOAD_REQUEST_RECEIVED)) {
                        status += "Request received for " + formDownloadDetails.formId + " AND IT " + getSuccessStatus(success);
                        if (!success) {
                            unregisterReceiver(transactionId);
                        }
                    } else if (formDownloadStatus.equals(FormDownloadStatus.DOWNLOAD_STARTED)) {
                        if (success) {
                            status += "Request is being processed for " + formDownloadDetails.formId;
                        } else {
                            status += "Request processing failed for " + formDownloadDetails.formId + "\nREASON: " + errorReason;
                            unregisterReceiver(transactionId);
                        }
                    } else {
                        status += "Request was completely processed for " + formDownloadDetails.formId + " AND IT " + getSuccessStatus(success);
                        unregisterReceiver(transactionId);
                    }

                    updateStatus(status);
                }
            }
        };

        if (downloadQueue.size() > 0) {
            registerReceiver(broadcastReceiver, new IntentFilter(Constants.FORM_DOWNLOAD_BROADCAST_ACTION));
        }
    }

    private void updateStatus(String text) {
        statusTv.setText(text);
    }

    private void unregisterReceiver(String transactionId) {
        downloadQueue.remove(transactionId);

        if (downloadQueue.size() < 1) {
            unregisterReceiver(broadcastReceiver);
        }
    }

    public void downloadForm(View view) {
        formId = formIdEdtv.getText().toString();

        String transactionId = UUID.randomUUID().toString();

        Intent intent = new Intent("org.odk.collect.android.FORM_DOWNLOAD");
        intent.putExtra(Constants.BundleKeys.FORM_ID, formId);
        intent.putExtra("TRANSACTION_ID", transactionId);

        intent.putExtra("URL", getValue("url", null));
        intent.putExtra("USERNAME", getValue("username", null));
        intent.putExtra("PASSWORD", getValue("password", null));

        FormDownloadDetails formDownloadDetails = new FormDownloadDetails();
        formDownloadDetails.formDownloadStatus = FormDownloadStatus.DOWNLOAD_REQUESTED;
        formDownloadDetails.formId = formId;

        downloadQueue.put(transactionId, formDownloadDetails);

        if (downloadInBackground) {
            sendBroadcast(intent);
        } else {
            startActivity(intent);
        }

        updateStatus("FORM " + ((formId == null) ? "NULL" : formId ) + ": Download requested");
        registerReceiver(broadcastReceiver, new IntentFilter(Constants.FORM_DOWNLOAD_BROADCAST_ACTION));
    }

    private String getValue(String key, String defValue) {
        return PreferenceManager.getDefaultSharedPreferences(this)
                .getString(key, defValue);
    }

    private String getSuccessStatus(boolean success) {
        return success ? "Succeeded" : "Failed";
    }

    public static class FormDownloadDetails {
        public FormDownloadStatus formDownloadStatus;
        public String formId;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        /*
         Save the following:
            - text on the download status
            - download queue(Keeps track of all state)
            - downloadInBackground flag
          */

        outState.putString(DOWNLOAD_TEXT_KEY, statusTv.getText().toString());
        outState.putSerializable(DOWNLOAD_QUEUE_KEY, downloadQueue);
        outState.putBoolean(DOWNLOAD_MODE_FLAG_KEY, downloadInBackground);
    }
}
