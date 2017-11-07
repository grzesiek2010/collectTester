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
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import org.odk.collectTester.R;

public class AbstractActivity extends AppCompatActivity {

    public void startActivityIfAvailable(Intent i) {
        if (isActivityAvailable(i)) {
            startActivity(i);
        } else {
            Toast
                    .makeText(this, getString(R.string.activity_not_found), Toast.LENGTH_LONG)
                    .show();
        }
    }

    private boolean isActivityAvailable(Intent intent) {
        return getPackageManager()
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
                .size() > 0;
    }
}
