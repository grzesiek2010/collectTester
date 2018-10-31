package org.odk.collectTester.activities;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.odk.collectTester.R;

import java.util.HashMap;
import java.util.Map;

import static org.odk.collectTester.utilities.Constants.FORMS_URI;

public class OpenBlankFormActivity extends AbstractActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_blank_form);
    }

    public void openBlankForm(View view) {
        Map<String, Integer> forms = getFormList(getFormsCursor());
        String expectedJrFormId = ((EditText) findViewById(R.id.jr_form_id_field)).getText().toString();

        if (forms.containsKey(expectedJrFormId)) {
            int id = forms.get(expectedJrFormId);
            Intent i = new Intent(Intent.ACTION_EDIT, Uri.parse(FORMS_URI + "/" + id));
            startActivityIfAvailable(i);
        } else {
            Toast
                    .makeText(this, getString(R.string.form_does_not_exist), Toast.LENGTH_LONG)
                    .show();
        }
    }


    private Cursor getFormsCursor() {
        return getContentResolver().query(Uri.parse(FORMS_URI), null, null, null, null);
    }

    private Map<String, Integer> getFormList(Cursor cursor) {
        Map<String, Integer> forms = new HashMap<>();

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
                    String jrFormId = cursor.getString(cursor.getColumnIndex(getString(R.string.jr_form_id)));

                    forms.put(jrFormId, id);
                }
            } finally {
                cursor.close();
            }
        }

        return forms;
    }
}
