package com.hosec.homesecurity.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.hosec.homesecurity.R;
import com.hosec.homesecurity.remote.RemoteAlarmSystem;

/**
 * Created by D062572 on 08.06.2017.
 */

public class ChangePasswordDialog extends DialogPreference {

    public static final String DEFAULT_VALUE = "";
    public static final String KEY = "pref_key_pwd";
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
            mPassword = this.getPersistedString(DEFAULT_VALUE);
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
            RemoteAlarmSystem.changePassword(mPassword);
            persistString(mPassword);
        }
    }

//    //COPIED FROM GOOGLE
//    private static class SavedState extends BaseSavedState {
//        // Member that holds the setting's value
//        // Change this data type to match the type saved by your Preference
//        String value;
//
//        public SavedState(Parcelable superState) {
//            super(superState);
//        }
//
//        public SavedState(Parcel source) {
//            super(source);
//            // Get the current preference's value
//            value = source.readString();  // Change this to read the appropriate data type
//        }
//
//        @Override
//        public void writeToParcel(Parcel dest, int flags) {
//            super.writeToParcel(dest, flags);
//            // Write the preference's value
//            dest.writeString(value);  // Change this to write the appropriate data type
//        }
//
//        // Standard creator object using an instance of this class
//        public static final Parcelable.Creator<SavedState> CREATOR =
//                new Parcelable.Creator<SavedState>() {
//
//                    public SavedState createFromParcel(Parcel in) {
//                        return new SavedState(in);
//                    }
//
//                    public SavedState[] newArray(int size) {
//                        return new SavedState[size];
//                    }
//                };
//    }
//
//    @Override
//    protected Parcelable onSaveInstanceState() {
//        final Parcelable superState = super.onSaveInstanceState();
//        // Check whether this Preference is persistent (continually saved)
//        if (isPersistent()) {
//            // No need to save instance state since it's persistent,
//            // use superclass state
//            return superState;
//        }
//
//        // Create instance of custom BaseSavedState
//        final SavedState myState = new SavedState(superState);
//        // Set the state's value with the class member that holds current
//        // setting value
//        myState.value = mNewValue;
//        return myState;
//    }
//
//    @Override
//    protected void onRestoreInstanceState(Parcelable state) {
//        // Check whether we saved the state in onSaveInstanceState
//        if (state == null || !state.getClass().equals(SavedState.class)) {
//            // Didn't save the state, so call superclass
//            super.onRestoreInstanceState(state);
//            return;
//        }
//
//        // Cast state to custom BaseSavedState and pass to superclass
//        SavedState myState = (SavedState) state;
//        super.onRestoreInstanceState(myState.getSuperState());
//
//        // Set this Preference's widget to reflect the restored state
//        mNumberPicker.setValue(myState.value);
//    }
//
//    Newsletter Blog Support

}
