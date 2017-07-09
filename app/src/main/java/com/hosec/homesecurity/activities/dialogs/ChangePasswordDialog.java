package com.hosec.homesecurity.activities.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
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

/**
 * Simple Password Change Dialog
 */
public class ChangePasswordDialog extends DialogPreference {

    private Button mPositiveButton;
    private String mPassword;

    public ChangePasswordDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.password_dialog);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        setDialogIcon(null);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        final TextView tvProblem = (TextView)view.findViewById(R.id.tvPasswordProblem);
        final EditText etEnterNew = (EditText) view.findViewById(R.id.etEnterNew);
        etEnterNew.setEnabled(false);
        final EditText etConfirmNew = (EditText) view.findViewById(R.id.etConfirmNew);
        etConfirmNew.setEnabled(false);
        final EditText etEnterOld = (EditText)view.findViewById(R.id.etEnterOld);
        etEnterOld.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                if(etEnterOld.getText().toString().equals(mPassword)){
                    etEnterNew.setEnabled(true);
                    etConfirmNew.setEnabled(true);
                    etEnterOld.setEnabled(false);
                }
            }
        });

        etConfirmNew.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                if(etConfirmNew.getText().toString().equals(etEnterNew.getText().toString())){
                    mPositiveButton.setEnabled(true);
                    etConfirmNew.setEnabled(false);
                    etEnterNew.setEnabled(false);
                    tvProblem.setText("");
                    mPassword =  etConfirmNew.getText().toString();
                }else{
                    tvProblem.setText(R.string.password_problem_message);
                    tvProblem.setTextColor(Color.RED);
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
            mPassword = this.getPersistedString(Credentials.DEFAULT_VALUE);
        } else {
            // Set default state from the XML attribute
            mPassword = (String) defaultValue;
            persistString(mPassword);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        // When the user selects "OK", persist the new value
        if (positiveResult) {

            final Context context = this.getContext();
            Credentials creds = Credentials.getStoredCredentials(context);
            creds.setPassword(mPassword);

            RemoteAlarmSystem.getInstance(context).updateCredentials(creds, new RemoteAlarmSystem.ResultListener() {
                @Override
                public void onResult(RemoteAlarmSystem.Result result) {
                    String message;
                    if(result.success){
                        message = context.getString(R.string.updated_password);
                    }else{
                        message = result.message;
                    }

                    Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
                }
            });

            persistString(mPassword);
        }
    }

}
