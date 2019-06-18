package org.odk.collectTester.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.odk.collectTester.R;
import org.odk.collectTester.utilities.Constants;
import org.odk.collectTester.utilities.FormDownloadStatus;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class FormsDownloadActivity extends AppCompatActivity {

    private TextView statusTv;
    private EditText formIdEdtv;
    private BroadcastReceiver broadcastReceiver;
    private String formIds;
    private Switch backgroundDownloadSwitch;

    private HashMap<String, FormDownloadDetails> downloadQueue = new HashMap<>();

    private boolean downloadInBackground = false;

    public static final int PROGRESS_REQUEST_RECEIVED = 1;
    public static final int PROGRESS_REQUEST_BEING_PROCESSED = 2;
    public static final int PROGRESS_REQUEST_SATISFIED = 3;

    private static final String DOWNLOAD_TEXT_KEY = "download_text";
    private static final String DOWNLOAD_QUEUE_KEY = "download_queue";
    private static final String DOWNLOAD_MODE_FLAG_KEY = "download_mode";

    // Bundle keys
    public static final String SUCCESS_KEY = "SUCCESSFUL";
    public static final String FORM_IDS = "FORM_IDS";
    public static final String MESSAGE = "MESSAGE";

    private static final int REQ_CODE = 28932;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_download);

        formIdEdtv = (EditText) findViewById(R.id.form_id_edt);
        statusTv = (TextView) findViewById(R.id.status_tv);
        backgroundDownloadSwitch = (Switch) findViewById(R.id.switch_background_download);

        if (savedInstanceState != null) {
            statusTv.setText(savedInstanceState.getString(DOWNLOAD_TEXT_KEY));
            downloadInBackground = savedInstanceState.getBoolean(DOWNLOAD_MODE_FLAG_KEY);
            backgroundDownloadSwitch.setChecked(downloadInBackground);
            downloadQueue = (HashMap<String, FormDownloadDetails>) savedInstanceState.getSerializable(DOWNLOAD_QUEUE_KEY);
        }

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                boolean success = intent.getBooleanExtra(Constants.BundleKeys.SUCCESS_KEY, false);
                int progressStage = intent.getIntExtra(Constants.BundleKeys.PROGRESS_STAGE, -1);
                String transactionId = intent.getStringExtra(Constants.BundleKeys.TRANSACTION_ID);

                if (transactionId != null && downloadQueue.containsKey(transactionId)) {
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

        if (downloadQueue.size() > 0 && downloadInBackground) {
            registerReceiver(broadcastReceiver, new IntentFilter(Constants.FORM_DOWNLOAD_BROADCAST_ACTION));
        }

        backgroundDownloadSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked && downloadInBackground && downloadQueue.size() > 0) {
                    Toast.makeText(FormsDownloadActivity.this, R.string.download_mode_change_during_bg_downloads_is_restricted, Toast.LENGTH_LONG)
                            .show();

                    backgroundDownloadSwitch.setChecked(true);
                }

                downloadInBackground = backgroundDownloadSwitch.isChecked();
            }
        });
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
        Intent intent = new Intent("org.odk.collect.android.FORM_DOWNLOAD");

        intent.putExtra("URL", getValue("url", null));
        intent.putExtra("USERNAME", getValue("username", null));
        intent.putExtra("PASSWORD", getValue("password", null));

        formIds = formIdEdtv.getText().toString();
        formIds = formIds.trim();

        String[] formIdArray = formIds.split(",");

        if (downloadInBackground) {
            for (String formId: formIdArray) {
                formId = formId.trim();

                if (!TextUtils.isEmpty(formId)) {
                    intent.putExtra(Constants.BundleKeys.FORM_ID, formId);

                    String transactionId = UUID.randomUUID().toString();
                    intent.putExtra("TRANSACTION_ID", transactionId);

                    FormDownloadDetails formDownloadDetails = new FormDownloadDetails();
                    formDownloadDetails.formDownloadStatus = FormDownloadStatus.DOWNLOAD_REQUESTED;
                    formDownloadDetails.formId = formId;

                    downloadQueue.put(transactionId, formDownloadDetails);
                    sendBroadcast(intent);
                    if (downloadQueue.size() <  2) {
                        registerReceiver(broadcastReceiver, new IntentFilter(Constants.FORM_DOWNLOAD_BROADCAST_ACTION));
                    }

                    updateStatus("FORM " + ((formId == null) ? "NULL" : formId) + ": Download requested");
                }
            }
        } else {
            ArrayList<String> formIdList = new ArrayList<>();

            for (String formId: formIdArray) {
                formId = formId.trim();

                if (!TextUtils.isEmpty(formId)) {
                    formIdList.add(formId);

                    FormDownloadDetails formDownloadDetails = new FormDownloadDetails();
                    formDownloadDetails.formDownloadStatus = FormDownloadStatus.DOWNLOAD_REQUESTED;
                    formDownloadDetails.formId = formId;

                    downloadQueue.put(formId, formDownloadDetails);
                }
            }

            intent.putExtra(Constants.BundleKeys.FORM_IDS, formIdList.toArray(new String[formIdList.size()]));
            intent.setType("vnd.android.cursor.dir/vnd.odk.form");

            startActivityForResult(intent, REQ_CODE);
        }

    }

    private String getValue(String key, String defValue) {
        return PreferenceManager.getDefaultSharedPreferences(this)
                .getString(key, defValue);
    }

    private String getSuccessStatus(boolean success) {
        return success ? "Succeeded" : "Failed";
    }

    public static class FormDownloadDetails implements Serializable {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE) {
            if (resultCode == RESULT_OK) {
                String status = "FOREGROUND REQUEST DOWNLOAD RESULTS: \n";
                boolean successful = data.getBooleanExtra(SUCCESS_KEY, false);

                String message = data.getStringExtra(MESSAGE);
                HashMap<String, Boolean> resultFormIds = (HashMap<String, Boolean>) data.getSerializableExtra(FORM_IDS);

                for (String formId: downloadQueue.keySet()) {
                    Boolean result = resultFormIds.remove(formId);
                    if (result != null) {
                        downloadQueue.remove(formId);
                        status += "FORM ID: " + formId + " SUCESSFULL: " + getSuccessStatus(result) + "\n";
                    }
                }

                status += "\nSUCCESSFUL: " + getSuccessStatus(successful) + "\nMESSAGE: " + message;

                updateStatus(status);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
