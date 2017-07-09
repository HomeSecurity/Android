package com.hosec.homesecurity.activities.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hosec.homesecurity.R;
import com.hosec.homesecurity.model.Credentials;
import com.hosec.homesecurity.remote.RemoteAlarmSystem;
import com.hosec.homesecurity.remote.RemoteAlarmSystem.*;

/**
 * Simple dialog to change the username
 */
public class ChangeUsernameDialog extends DialogPreference {

    private String mUsername;
    private Button mPositiveButton;

    public ChangeUsernameDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.username_dialog);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        setDialogIcon(null);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        TextView tvCurrentName = (TextView) view.findViewById(R.id.tvCurrentName);
        tvCurrentName.setText(mUsername);
        final EditText etNewName = (EditText) view.findViewById(R.id.etNewName);
        etNewName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if(etNewName.getText().toString().length() != 0){
                    mPositiveButton.setEnabled(true);
                    mUsername = etNewName.getText().toString();
                }
            }
        });
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        mPositiveButton = ((AlertDialog)super.getDialog()).getButton(AlertDialog.BUTTON_POSITIVE);
        mPositiveButton.setEnabled(false);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            mUsername = this.getPersistedString(Credentials.DEFAULT_VALUE);
        } else {
            // Set default state from the XML attribute
            mUsername = (String) defaultValue;
            persistString(mUsername);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        // When the user selects "OK", persist the new value and call remote system
        if (positiveResult) {

            final Context context = this.getContext();
            Credentials creds = Credentials.getStoredCredentials(context);
            creds.setUsername(mUsername);

            RemoteAlarmSystem.getInstance(context).updateCredentials(creds, new ResultListener() {
                @Override
                public void onResult(Result result) {
                    String message;
                    if(result.success){
                        message = context.getString(R.string.updated_username);
                    }else{
                        message = result.message;
                    }

                    Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
                }
            });

            persistString(mUsername);
        }
    }
}
