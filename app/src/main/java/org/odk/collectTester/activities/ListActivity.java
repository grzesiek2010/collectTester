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
import android.provider.BaseColumns;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import org.odk.collectTester.utilities.ListElement;
import org.odk.collectTester.R;
import org.odk.collectTester.adapters.ListAdapter;

import java.util.ArrayList;
import java.util.List;

import static org.odk.collectTester.utilities.Constants.DISPLAY_NAME;
import static org.odk.collectTester.utilities.Constants.DISPLAY_SUBTEXT;
import static org.odk.collectTester.utilities.Constants.FORMS;
import static org.odk.collectTester.utilities.Constants.FORMS_URI;
import static org.odk.collectTester.utilities.Constants.INSTANCES_URI;
import static org.odk.collectTester.utilities.Constants.LIST_MODE_KEY;

public class ListActivity extends AbstractActivity {
    private String mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        mode = getIntent().getExtras().getString(LIST_MODE_KEY);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.list);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

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

        TextView emptyView = (TextView) findViewById(R.id.empty_view);
        if (getCursor().getCount() == 0) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }
    }

    private Cursor getCursor() {
        Uri uri;
        if (mode.equals(FORMS)) {
            uri = Uri.parse(FORMS_URI);
        } else {
            uri = Uri.parse(INSTANCES_URI);
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
                    listElements.add(new ListElement(id, text1, text2));
                }
            } finally {
                cursor.close();
            }
        }

        return listElements;
    }
}
