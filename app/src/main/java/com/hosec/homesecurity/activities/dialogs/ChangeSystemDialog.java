package com.hosec.homesecurity.activities.dialogs;

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
import com.hosec.homesecurity.model.Credentials;

/**
 * Dialog which lets the user change her alarm system
 */
public class ChangeSystemDialog extends DialogPreference {

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
            mHost = this.getPersistedString(Credentials.DEFAULT_VALUE);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        // When the user selects "OK", persist the new value
        if (positiveResult) {
            persistString(Credentials.DEFAULT_VALUE);
        }
    }
}
