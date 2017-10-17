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

package org.odk.collectTester.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import android.support.v4.app.DialogFragment;
import android.webkit.WebView;

import org.odk.collectTester.R;

public class SimpleDialog extends DialogFragment {

    public static final String INFO_DIALOG_TAG = "infoDialogTag";

    private static final String MESSAGE = "message";
    private static final String CODE_FRAGMENT = "codeFragment";

    public static SimpleDialog newInstance(String message, String codeFragment) {
        Bundle bundle = new Bundle();
        bundle.putString(MESSAGE, message);
        bundle.putString(CODE_FRAGMENT, codeFragment);

        SimpleDialog dialogFragment = new SimpleDialog();
        dialogFragment.setArguments(bundle);
        return dialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setCancelable(false);

        AlertDialog alertDialog =  new AlertDialog.Builder(getActivity())
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();
                    }
                })
                .create();

        String codeFragment = getArguments().getString(CODE_FRAGMENT);

        if (codeFragment != null) {
            WebView webView = new WebView(getContext());
            webView.loadUrl(getArguments().getString(CODE_FRAGMENT));
            webView.getSettings().setLoadWithOverviewMode(true);
            webView.getSettings().setUseWideViewPort(true);
            webView.getSettings().setMinimumFontSize(60);

            alertDialog.setView(webView);
        } else {
            alertDialog.setMessage(getArguments().getString(MESSAGE));
        }

        return alertDialog;
    }
}
