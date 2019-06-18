package org.odk.collectTester.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.odk.collectTester.R;
import org.odk.collectTester.utilities.Constants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static org.odk.collectTester.utilities.Constants.BundleKeys.PASSWORD;
import static org.odk.collectTester.utilities.Constants.BundleKeys.URL;
import static org.odk.collectTester.utilities.Constants.BundleKeys.USERNAME;

public class FormsDownloadActivity extends BaseActivity {

    private enum FormDownloadStatus {
        DOWNLOAD_REQUESTED, DOWNLOAD_REQUEST_RECEIVED, DOWNLOAD_STARTED, DOWNLOAD_FAILED, DOWNLOAD_SUCCEEDED;
    }

    private TextView statusTv;
    private BroadcastReceiver broadcastReceiver;
    private Switch backgroundDownloadSwitch;

    private HashMap<String, FormDownloadDetails> downloadQueue = new HashMap<>();

    private boolean downloadInBackground;

    public static final int PROGRESS_REQUEST_RECEIVED = 1;
    public static final int PROGRESS_REQUEST_BEING_PROCESSED = 2;

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

        statusTv = findViewById(R.id.status_tv);
        backgroundDownloadSwitch = findViewById(R.id.switch_background_download);

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

        if (!downloadQueue.isEmpty() && downloadInBackground) {
            registerReceiver(broadcastReceiver, new IntentFilter(Constants.FORM_DOWNLOAD_BROADCAST_ACTION));
        }

        backgroundDownloadSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked && downloadInBackground && !downloadQueue.isEmpty()) {
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

        if (downloadQueue.isEmpty()) {
            unregisterReceiver(broadcastReceiver);
        }
    }

    public void startFormsDownloadActivity(View view) {
        Intent intent = new Intent("org.odk.collect.android.FORM_DOWNLOAD");

        intent.putExtra(URL, getUrl());
        intent.putExtra(USERNAME, getUserName());
        intent.putExtra(PASSWORD, getPassword());

        String formIds = getFormIdsString();

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

                    updateStatus("FORM " + formId + ": Download requested");
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

            intent.putExtra(Constants.BundleKeys.FORM_IDS, formIdList.toArray(new String[0]));
            intent.setType("vnd.android.cursor.dir/vnd.odk.form");

            startActivityForResult(intent, REQ_CODE);
        }

    }

    private String getSuccessStatus(boolean success) {
        return success ? "Succeeded" : "Failed";
    }

    public static class FormDownloadDetails implements Serializable {
        FormDownloadStatus formDownloadStatus;
        String formId;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(DOWNLOAD_TEXT_KEY, statusTv.getText().toString());
        outState.putSerializable(DOWNLOAD_QUEUE_KEY, downloadQueue);
        outState.putBoolean(DOWNLOAD_MODE_FLAG_KEY, downloadInBackground);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE) {
            if (resultCode == RESULT_OK) {
                StringBuilder status = new StringBuilder("FOREGROUND REQUEST DOWNLOAD RESULTS: \n");
                boolean successful = data.getBooleanExtra(SUCCESS_KEY, false);

                String message = data.getStringExtra(MESSAGE);
                HashMap<String, Boolean> resultFormIds = (HashMap<String, Boolean>) data.getSerializableExtra(FORM_IDS);

                for (String formId: downloadQueue.keySet()) {
                    Boolean result = resultFormIds.remove(formId);
                    if (result != null) {
                        status.append("FORM ID: ")
                                .append(formId)
                                .append(" SUCESSFULL: ")
                                .append(getSuccessStatus(result))
                                .append('\n');
                    }
                }

                status
                        .append("\nSUCCESSFUL: ")
                        .append(getSuccessStatus(successful))
                        .append("\nMESSAGE: ")
                        .append(message);

                updateStatus(status.toString());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private String getFormIdsString() {
        return ((TextView) findViewById(R.id.form_id_edt)).getText().toString().trim();
    }
}
