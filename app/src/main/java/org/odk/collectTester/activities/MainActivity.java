/*
 * Copyright 2017 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collectTester.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.odk.collectTester.R;
import org.odk.collectTester.fragments.SimpleDialog;

import static org.odk.collectTester.utilities.Constants.COLLECT_PACKAGE_NAME;
import static org.odk.collectTester.utilities.Constants.DOWNLOAD_FORM_CODE;
import static org.odk.collectTester.utilities.Constants.EDIT_SAVED_FORM_CODE;
import static org.odk.collectTester.utilities.Constants.FETCH_LIST_OF_FORMS_CODE;
import static org.odk.collectTester.utilities.Constants.FETCH_LIST_OF_INSTANCES_CODE;
import static org.odk.collectTester.utilities.Constants.FILL_BLANK_FORM_CODE;
import static org.odk.collectTester.utilities.Constants.FORMS;
import static org.odk.collectTester.utilities.Constants.FORMS_CHOOSER_INTENT_TYPE;
import static org.odk.collectTester.utilities.Constants.FORMS_TO_DOWNLOAD_CODE;
import static org.odk.collectTester.utilities.Constants.FORM_MODE;
import static org.odk.collectTester.utilities.Constants.INSTANCES;
import static org.odk.collectTester.utilities.Constants.INSTANCES_CHOOSER_INTENT_TYPE;
import static org.odk.collectTester.utilities.Constants.LIST_MODE_KEY;
import static org.odk.collectTester.utilities.Constants.SEND_FINALIZED_FORM_CODE;
import static org.odk.collectTester.utilities.Constants.SENT_FORMS_INTENT_TYPE;
import static org.odk.collectTester.utilities.Constants.START_ODK_COLLECT;
import static org.odk.collectTester.utilities.Constants.VIEW_INSTANCE_SUBMISSION_CODE;
import static org.odk.collectTester.utilities.Constants.VIEW_SENT;
import static org.odk.collectTester.utilities.Constants.VIEW_SENT_FORM_CODE;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startODKCollect(View view) {
        Intent i = getPackageManager().getLaunchIntentForPackage(COLLECT_PACKAGE_NAME);
        startActivityIfAvailable(i);
    }

    public void startFormChooserList(View view) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setType(FORMS_CHOOSER_INTENT_TYPE);
        startActivityIfAvailable(i);
    }

    public void startInstanceChooserList(View view) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setType(INSTANCES_CHOOSER_INTENT_TYPE);
        startActivityIfAvailable(i);
    }

    public void startInstanceUploaderList(View view) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        startActivityIfAvailable(i);
    }

    public void startSentFormList(View view) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setType(SENT_FORMS_INTENT_TYPE);
        i.putExtra(FORM_MODE, VIEW_SENT);
        startActivityIfAvailable(i);
    }

    public void startFormsToDownloadList(View view) {
        Intent i = new Intent("org.odk.collect.android.FORM_DOWNLOAD");
        i.setType("vnd.android.cursor.dir/vnd.odk.form");
        startActivityIfAvailable(i);
    }

    public void startFormList(View view) {
        Intent i = new Intent(this, ListActivity.class);
        i.putExtra(LIST_MODE_KEY, FORMS);
        startActivity(i);
    }

    public void startInstancesList(View view) {
        Intent i = new Intent(this, ListActivity.class);
        i.putExtra(LIST_MODE_KEY, INSTANCES);
        startActivity(i);
    }

    public void startInstancesSubmitActivity(View view) {
        startActivity(new Intent(this, InstancesSubmitActivity.class));
    }

    public void startFormsDownloadActivity(View view) {
        startActivity(new Intent(this, FormsDownloadActivity.class));
    }

    public void startODKCollectInfo(View view) {
        showDialogInfo(null, START_ODK_COLLECT);
    }

    public void startFormChooserListInfo(View view) {
        showDialogInfo(null, FILL_BLANK_FORM_CODE);
    }

    public void startInstanceChooserListInfo(View view) {
        showDialogInfo(null, EDIT_SAVED_FORM_CODE);
    }

    public void startInstanceUploaderListInfo(View view) {
        showDialogInfo(null, SEND_FINALIZED_FORM_CODE);
    }

    public void startSentFormListInfo(View view) {
        showDialogInfo(null, VIEW_SENT_FORM_CODE);
    }

    public void startFormsToDownloadListInfo(View view) {
        showDialogInfo(null, FORMS_TO_DOWNLOAD_CODE);
    }

    public void startFormListInfo(View view) {
        showDialogInfo(null, FETCH_LIST_OF_FORMS_CODE);
    }

    public void startInstancesListInfo(View view) {
        showDialogInfo(null, FETCH_LIST_OF_INSTANCES_CODE);
    }

    public void startInstancesSubmissionInfo(View view) {
        showDialogInfo(null, VIEW_INSTANCE_SUBMISSION_CODE);
    }

    public void startFormsDownloadInfo(View view) {
        showDialogInfo(null, DOWNLOAD_FORM_CODE);
    }

    private void showDialogInfo(String message, String codeFragment) {
        SimpleDialog simpleDialog = SimpleDialog.newInstance(message, codeFragment);
        simpleDialog.show(getSupportFragmentManager(), SimpleDialog.INFO_DIALOG_TAG);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }
}
