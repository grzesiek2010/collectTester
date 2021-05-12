package org.odk.collectTester.activities;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.odk.collectTester.R;
import org.odk.collectTester.adapters.ListAdapter;
import org.odk.collectTester.utilities.ListElement;

import java.util.ArrayList;
import java.util.List;

import static org.odk.collectTester.utilities.Constants.PROJECTS_URI;
import static org.odk.collectTester.utilities.Constants.PROJECT_NAME;
import static org.odk.collectTester.utilities.Constants.PROJECT_UUID;

public class ProjectsListActivity extends BaseActivity {

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

        recyclerView = findViewById(R.id.list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onResume() {
        super.onResume();

        recyclerView.setAdapter(new ListAdapter(getListFromCursor(), null));

        if (recyclerView.getAdapter().getItemCount() == 0) {
            findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            findViewById(R.id.empty_view).setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private List<ListElement> getListFromCursor() {
        Cursor cursor = getContentResolver().query(Uri.parse(PROJECTS_URI), null, null, null, null);
        List<ListElement> listElements = new ArrayList<>();

        if (cursor != null) {
            try {
                int index = 0;
                while (cursor.moveToNext()) {
                    String uuid = cursor.getString(cursor.getColumnIndex(PROJECT_UUID));
                    String projectName = cursor.getString(cursor.getColumnIndex(PROJECT_NAME));
                    listElements.add(new ListElement(index++, projectName, uuid));
                }
            } finally {
                cursor.close();
            }
        }

        return listElements;
    }
}
