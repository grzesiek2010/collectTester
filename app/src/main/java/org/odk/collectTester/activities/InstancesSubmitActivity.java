package org.odk.collectTester.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.odk.collectTester.R;
import org.odk.collectTester.utilities.Constants;

import java.util.ArrayList;

import static org.odk.collectTester.utilities.Constants.INSTANCES_CHOOSER_INTENT_TYPE;
import static org.odk.collectTester.utilities.Constants.ODK_COLLECT_SUBMIT_INSTANCE_ACTION;

public class InstancesSubmitActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instance_upload);

        if (!isCollectAppInstalled()) {
            finish();
            Toast
                    .makeText(this, getString(R.string.collect_app_not_installed), Toast.LENGTH_LONG)
                    .show();
        }
    }

    public void onSubmitButtonClick(View view) {
        String stringInstanceIds = getInstanceIdsString();

        if (!TextUtils.isEmpty(stringInstanceIds)) {
            String[] ids = stringInstanceIds.split(",");
            ArrayList<Long> goodIds = new ArrayList<>();

            for (String id: ids) {
                id = id
                        .trim()
                        .replace(",", "");
                if (!TextUtils.isEmpty(id) && TextUtils.isDigitsOnly(id)) {
                    goodIds.add(Long.parseLong(id));
                }
            }

            Long[] objectIdsArray = goodIds.toArray(new Long[0]);
            Long[] primitiveIdsArray = new Long[objectIdsArray.length];

            System.arraycopy(objectIdsArray, 0, primitiveIdsArray, 0, primitiveIdsArray.length);

            if (primitiveIdsArray.length > 0) {
                submitInstances(primitiveIdsArray);
            }
        }
    }

    private String getInstanceIdsString() {
        return ((TextView) findViewById(R.id.et_listActivity_instanceIds)).getText().toString().trim();
    }

    private void submitInstances(Long[] instanceIds) {
        Intent intent = new Intent(ODK_COLLECT_SUBMIT_INSTANCE_ACTION);
        intent.setType(INSTANCES_CHOOSER_INTENT_TYPE);

        intent.putExtra(Constants.BundleKeys.INSTANCES, instanceIds);
        intent.putExtra(Constants.BundleKeys.URL, getUrl());

        String userName = getUserName();
        if (userName != null) {
            intent.putExtra(Constants.BundleKeys.USERNAME, userName);
        }

        String password = getPassword();
        if (password != null) {
            intent.putExtra(Constants.BundleKeys.PASSWORD, password);
        }

        startActivity(intent);
    }
}
