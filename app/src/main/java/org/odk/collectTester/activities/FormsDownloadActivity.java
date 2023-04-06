package org.odk.collectTester.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.odk.collectTester.R;
import org.odk.collectTester.utilities.Constants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.odk.collectTester.utilities.Constants.BundleKeys.PASSWORD;
import static org.odk.collectTester.utilities.Constants.BundleKeys.URL;
import static org.odk.collectTester.utilities.Constants.BundleKeys.USERNAME;

public class FormsDownloadActivity extends BaseActivity {
    private TextView statusTv;

    private List<String> downloadQueue = new ArrayList<>();

    private static final String DOWNLOAD_TEXT_KEY = "download_text";
    private static final String DOWNLOAD_QUEUE_KEY = "download_queue";

    // Bundle keys
    public static final String SUCCESS_KEY = "SUCCESSFUL";
    public static final String FORM_IDS = "FORM_IDS";
    public static final String MESSAGE = "MESSAGE";

    private static final int REQ_CODE = 28932;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_download);

        if (!isCollectAppInstalled()) {
            finish();
            Toast
                    .makeText(this, getString(R.string.collect_app_not_installed), Toast.LENGTH_LONG)
                    .show();
        }

        statusTv = findViewById(R.id.status_tv);

        if (savedInstanceState != null) {
            statusTv.setText(savedInstanceState.getString(DOWNLOAD_TEXT_KEY));
            downloadQueue = (List<String>) savedInstanceState.getSerializable(DOWNLOAD_QUEUE_KEY);
        }
    }

    private void updateStatus(String text) {
        statusTv.setText(text);
    }


    public void startFormsDownloadActivity(View view) {
        Intent intent = new Intent("org.odk.collect.android.FORM_DOWNLOAD");

        intent.putExtra(URL, getUrl());
        intent.putExtra(USERNAME, getUserName());
        intent.putExtra(PASSWORD, getPassword());

        String formIds = getFormIdsString();

        String[] formIdArray = formIds.split(",");

        ArrayList<String> formIdList = new ArrayList<>();

        for (String formId: formIdArray) {
            formId = formId.trim();

            if (!TextUtils.isEmpty(formId)) {
                formIdList.add(formId);

                downloadQueue.add(formId);
            }
        }

        intent.putExtra(Constants.BundleKeys.FORM_IDS, formIdList.toArray(new String[0]));
        intent.setType("vnd.android.cursor.dir/vnd.odk.form");

        startActivityForResult(intent, REQ_CODE);
    }

    private String getSuccessStatus(boolean success) {
        return success ? "Succeeded" : "Failed";
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(DOWNLOAD_TEXT_KEY, statusTv.getText().toString());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE) {
            if (resultCode == RESULT_OK) {
                StringBuilder status = new StringBuilder("FOREGROUND REQUEST DOWNLOAD RESULTS: \n");
                boolean successful = data.getBooleanExtra(SUCCESS_KEY, false);

                String message = data.getStringExtra(MESSAGE);
                HashMap<String, Boolean> resultFormIds = (HashMap<String, Boolean>) data.getSerializableExtra(FORM_IDS);

                for (String formId: downloadQueue) {
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
