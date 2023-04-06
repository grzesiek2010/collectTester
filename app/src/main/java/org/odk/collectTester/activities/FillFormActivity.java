package org.odk.collectTester.activities;

import static org.odk.collectTester.utilities.Constants.COLLECT_PACKAGE_NAME;
import static org.odk.collectTester.utilities.Constants.FORMS_URI;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.odk.collectTester.R;

public class FillFormActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_form);
    }

    public void startFillForm(View view) {
        String id = ((TextView) findViewById(R.id.form_id_edt)).getText().toString().trim();

        Intent intent = new Intent();
        intent.setComponent(new ComponentName(COLLECT_PACKAGE_NAME, "org.odk.collect.android.activities.FormEntryActivity"));
        intent.setData(Uri.parse(FORMS_URI + "/" + id));
        startActivity(intent);
    }
}
