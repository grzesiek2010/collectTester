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

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.Toast;

import org.odk.collectTester.utilities.ListElement;
import org.odk.collectTester.R;
import org.odk.collectTester.adapters.ListAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.odk.collectTester.utilities.Constants.DATE;
import static org.odk.collectTester.utilities.Constants.DISPLAY_NAME;
import static org.odk.collectTester.utilities.Constants.FORMS;
import static org.odk.collectTester.utilities.Constants.FORMS_URI;
import static org.odk.collectTester.utilities.Constants.INSTANCES_URI;
import static org.odk.collectTester.utilities.Constants.LIST_MODE_KEY;
import static org.odk.collectTester.utilities.Constants.STATUS;
import static org.odk.collectTester.utilities.Constants.STATUS_COMPLETE;
import static org.odk.collectTester.utilities.Constants.STATUS_INCOMPLETE;
import static org.odk.collectTester.utilities.Constants.STATUS_SUBMISSION_FAILED;
import static org.odk.collectTester.utilities.Constants.STATUS_SUBMITTED;

public class ListActivity extends BaseActivity {
    private String mode;

    private RecyclerView recyclerView;

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

        setUpMode();

        recyclerView = findViewById(R.id.list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setUpMode() {
        Bundle bundle = getIntent().getExtras();
        mode = bundle != null ? bundle.getString(LIST_MODE_KEY) : FORMS;
    }

    private Cursor getCursor() {
        Uri uri = mode.equals(FORMS) ? Uri.parse(FORMS_URI) : Uri.parse(INSTANCES_URI);
        return getContentResolver().query(uri, null, null, null, null);
    }

    private List<ListElement> getListFromCursor(Cursor cursor) {
        List<ListElement> listElements = new ArrayList<>();

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
                    String text1 = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));
                    String status = mode.equals(FORMS) ? null : cursor.getString(getCursor().getColumnIndex(STATUS));
                    String text2 = getSubtext(this, status, new Date(cursor.getLong(cursor.getColumnIndex(DATE))));

                    listElements.add(new ListElement(id, text1, text2));
                }
            } finally {
                cursor.close();
            }
        }

        return listElements;
    }

    public static String getSubtext(Context context, String state, Date date) {
        try {
            if (state == null) {
                return new SimpleDateFormat(context.getString(R.string.added_on_date_at_time),
                        Locale.getDefault()).format(date);
            } else if (STATUS_INCOMPLETE.equalsIgnoreCase(state)) {
                return new SimpleDateFormat(context.getString(R.string.saved_on_date_at_time),
                        Locale.getDefault()).format(date);
            } else if (STATUS_COMPLETE.equalsIgnoreCase(state)) {
                return new SimpleDateFormat(context.getString(R.string.finalized_on_date_at_time),
                        Locale.getDefault()).format(date);
            } else if (STATUS_SUBMITTED.equalsIgnoreCase(state)) {
                return new SimpleDateFormat(context.getString(R.string.sent_on_date_at_time),
                        Locale.getDefault()).format(date);
            } else if (STATUS_SUBMISSION_FAILED.equalsIgnoreCase(state)) {
                return new SimpleDateFormat(
                        context.getString(R.string.sending_failed_on_date_at_time),
                        Locale.getDefault()).format(date);
            } else {
                return new SimpleDateFormat(context.getString(R.string.added_on_date_at_time),
                        Locale.getDefault()).format(date);
            }
        } catch (IllegalArgumentException e) {
            return "";
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        recyclerView.setAdapter(new ListAdapter(getListFromCursor(getCursor()), new ListAdapter.OnItemClickListener() {
            @Override public void onItemClick(ListElement item) {
                if (mode.equals(FORMS)) {
                    Intent i = new Intent(Intent.ACTION_EDIT, Uri.parse(FORMS_URI + "/" + item.getId()));
                    startActivityIfAvailable(i);
                } else {
                    Intent i = new Intent(Intent.ACTION_EDIT, Uri.parse(INSTANCES_URI + "/" + item.getId()));
                    startActivityIfAvailable(i);
                }
            }
        }));

        if (getListFromCursor(getCursor()).isEmpty()) {
            findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            findViewById(R.id.empty_view).setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}
