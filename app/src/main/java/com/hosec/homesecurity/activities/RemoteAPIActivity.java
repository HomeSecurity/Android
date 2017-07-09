package com.hosec.homesecurity.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.hosec.homesecurity.remote.RemoteAlarmSystem;

/**
 * Base Activity for all activity implementations using the RemoteAlarmSystem API wrapper.
 * This activity provides a RemoteAlarmSystem instance after onResume() was called.
 * It also guarantees that all pending request will be canceled if an activity is stopped.
 */

public abstract class RemoteAPIActivity extends AppCompatActivity {

    protected RemoteAlarmSystem mRemoteAlarmSystem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRemoteAlarmSystem = RemoteAlarmSystem.getInstance(this);
    }


    @Override
    protected void onStop() {
        super.onStop();
        mRemoteAlarmSystem.cancelAll();
    }
}
