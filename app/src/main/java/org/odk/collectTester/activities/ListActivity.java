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
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.odk.collectTester.utilities.Constants;
import org.odk.collectTester.utilities.ListElement;
import org.odk.collectTester.R;
import org.odk.collectTester.adapters.ListAdapter;

import java.util.ArrayList;
import java.util.List;

import static org.odk.collectTester.utilities.Constants.DISPLAY_NAME;
import static org.odk.collectTester.utilities.Constants.DISPLAY_SUBTEXT;
import static org.odk.collectTester.utilities.Constants.FORMS;
import static org.odk.collectTester.utilities.Constants.FORMS_URI;
import static org.odk.collectTester.utilities.Constants.INSTANCES_CHOOSER_INTENT_TYPE;
import static org.odk.collectTester.utilities.Constants.INSTANCES_URI;
import static org.odk.collectTester.utilities.Constants.INSTANCE_SUBMISSION;
import static org.odk.collectTester.utilities.Constants.LIST_MODE_KEY;
import static org.odk.collectTester.utilities.Constants.ODK_COLLECT_SUBMIT_INSTANCE_ACTION;
import static org.odk.collectTester.utilities.Constants.STATUS;
import static org.odk.collectTester.utilities.Constants.STATUS_SUBMITTED;
import static org.odk.collectTester.utilities.Constants.STATUS_COMPLETE;
import static org.odk.collectTester.utilities.Constants.STATUS_SUBMISSION_FAILED;

public class ListActivity extends AbstractActivity {
    private String mode;

    private String url = null;
    private String password = null;
    private String username = null;

    private RecyclerView recyclerView;
    private EditText instanceIds;
    private Button submitInstancesBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        if (!isCollectAppInstalled()) {
            finish();
            Toast
                    .makeText(this, getString(R.string.collect_app_not_installed), Toast.LENGTH_LONG)
                    .show();
            return;
        }

        Bundle bundle = getIntent().getExtras();
        mode = bundle.getString(LIST_MODE_KEY);

        recyclerView = (RecyclerView) findViewById(R.id.list);
        instanceIds = (EditText) findViewById(R.id.et_listActivity_instanceIds);
        submitInstancesBtn = (Button) findViewById(R.id.btn_listActivity_submitInstances);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        url = getPrefValue("url", null);
        username = getPrefValue("username", null);
        password = getPrefValue("password", null);
    }

    private Cursor getCursor() {
        Uri uri;
        if (mode.equals(FORMS)) {
            uri = Uri.parse(FORMS_URI);
        } else {
            uri = Uri.parse(INSTANCES_URI);
            if (mode.equals(INSTANCE_SUBMISSION)) {
                return getContentResolver().query(uri, null, STATUS + " = ? OR " + STATUS + " = ?", new String[]{STATUS_COMPLETE, STATUS_SUBMISSION_FAILED}, null);
            }
        }

        return getContentResolver().query(uri, null, null, null, null);
    }

    private List<ListElement> getListFromCursor(Cursor cursor) {
        List<ListElement> listElements = new ArrayList<>();

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
                    String text1 = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));
                    String text2 = cursor.getString(cursor.getColumnIndex(DISPLAY_SUBTEXT));
                    if (mode.equals(FORMS) || !STATUS_SUBMITTED.equals(cursor.getString(cursor.getColumnIndex(STATUS)))) {
                        listElements.add(new ListElement(id, text1, text2));
                    }
                }
            } finally {
                cursor.close();
            }
        }

        return listElements;
    }

    @Override
    protected void onResume() {
        super.onResume();

        recyclerView.setAdapter(new ListAdapter(getListFromCursor(getCursor()), new ListAdapter.OnItemClickListener() {
            @Override public void onItemClick(ListElement item) {
                if (mode.equals(FORMS)) {
                    Intent i = new Intent(Intent.ACTION_EDIT, Uri.parse(FORMS_URI + "/" + item.getId()));
                    startActivityIfAvailable(i);
                } else if (mode.equals(INSTANCE_SUBMISSION)) {
                    submitInstances(new long[]{item.getId()});
                } else {
                    Intent i = new Intent(Intent.ACTION_EDIT, Uri.parse(INSTANCES_URI + "/" + item.getId()));
                    startActivityIfAvailable(i);
                }
            }
        }));

        TextView emptyView = (TextView) findViewById(R.id.empty_view);
        if (getCursor() == null || getCursor().getCount() == 0) {
            recyclerView.setVisibility(View.GONE);

            if (mode.equals(INSTANCE_SUBMISSION)) {
                findViewById(R.id.ll_listActivity_instanceUploadLayout)
                        .setVisibility(View.VISIBLE);

                submitInstancesBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String stringInstanceIds = instanceIds.getText().toString();
                        stringInstanceIds = stringInstanceIds.trim();

                        if (!TextUtils.isEmpty(stringInstanceIds)) {
                            String[] ids = stringInstanceIds.split(",");
                            ArrayList<Long> goodIds = new ArrayList<>();

                            for (String id: ids) {
                                id = id.trim();
                                id = id.replace(",", "");
                                if (!TextUtils.isEmpty(id) && TextUtils.isDigitsOnly(id)) {
                                    goodIds.add(Long.parseLong(id));
                                }
                            }

                            Long[] objectIdsArray = goodIds.toArray(new Long[goodIds.size()]);
                            long[] primitiveIdsArray = new long[objectIdsArray.length];

                            for (int i = 0; i < objectIdsArray.length; i++) {
                                primitiveIdsArray[i] = objectIdsArray[i];
                            }

                            if (primitiveIdsArray.length > 0) {
                                submitInstances(primitiveIdsArray);
                            }
                        }
                    }
                });

            } else {
                findViewById(R.id.ll_listActivity_instanceUploadLayout)
                        .setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            }
        } else {
            emptyView.setVisibility(View.GONE);
            findViewById(R.id.ll_listActivity_instanceUploadLayout)
                    .setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private String getPrefValue(String key, String defValue) {
        return PreferenceManager
                .getDefaultSharedPreferences(this)
                .getString(key, defValue);
    }

    private void submitInstances(long[] instanceIds) {
        Intent intent = new Intent(ODK_COLLECT_SUBMIT_INSTANCE_ACTION);
        intent.setType(INSTANCES_CHOOSER_INTENT_TYPE);

        intent.putExtra(Constants.BundleKeys.INSTANCES, instanceIds);
        intent.putExtra(Constants.BundleKeys.URL, url);

        if (username != null) {
            intent.putExtra(Constants.BundleKeys.USERNAME, username);
        }

        if (password != null) {
            intent.putExtra(Constants.BundleKeys.PASSWORD, password);
        }

        startActivity(intent);
    }
}
