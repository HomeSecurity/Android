package com.hosec.homesecurity.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.hosec.homesecurity.R;

/**
 * Created by D062572 on 10.06.2017.
 */

public class ChangeSystemDialog extends DialogPreference {

    public  static final String DEFAULT_VALUE = "";
    public static final String SYSTEM_PREF_KEY = "pref_key_system_host";
    private String mHost;

    public ChangeSystemDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.system_dialog);
        setPositiveButtonText(R.string.log_out);
        setNegativeButtonText(android.R.string.cancel);
        setDialogIcon(null);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        TextView tvCurrentName = (TextView) view.findViewById(R.id.tvHostName);
        tvCurrentName.setText(mHost);

    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        Button positiveButton = ((AlertDialog)super.getDialog()).getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(Color.RED);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            mHost = this.getPersistedString(DEFAULT_VALUE);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        // When the user selects "OK", persist the new value
        if (positiveResult) {
            persistString(DEFAULT_VALUE);
        }
    }
}
