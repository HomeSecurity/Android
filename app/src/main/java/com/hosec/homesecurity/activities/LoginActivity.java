package com.hosec.homesecurity.activities;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hosec.homesecurity.R;
import com.hosec.homesecurity.messaging.FCMToken;
import com.hosec.homesecurity.model.Credentials;
import com.hosec.homesecurity.remote.RemoteAlarmSystem;
import com.hosec.homesecurity.remote.RemoteAlarmSystem.ResultListener;

/**
 * A login screen that offers login to alarm system by entering the IP or hostname of the system
 * and username and password. There is only one pre-configured user for each alarm system so far
 * which has username DefaultUser and password DefaultPassword. Multiple users are not supported by
 * current implementations of HomeSecurity's alarm system.
 * <p>
 * The login credentials will be remembered and used for subsequent logins. Currently, all credentials
 * are stored in a private shared preference storage which is not the recommended way to store authentication
 * information. TODO: implement token auth strategy
 */
public class LoginActivity extends RemoteAPIActivity {

    private String mPwd;
    private String mUser;
    private String mHost;
    private Button mSubmit;

    private ResultListener mTokenListener = new ResultListener() {
        @Override
        public void onResult(RemoteAlarmSystem.Result result) {
            //start home activity
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            LoginActivity.this.startActivity(intent);
            LoginActivity.this.finish();
        }
    };

    //ResultListener for authentication requests sent to the alarm system
    private ResultListener mLoginListener = new ResultListener() {
        @Override
        public void onResult(RemoteAlarmSystem.Result result) {
            Toast.makeText(LoginActivity.this, result.message, Toast.LENGTH_SHORT).show();
            if (result.success) {
                //save valid credentials
                Credentials creds = new Credentials(mUser, mPwd, mHost);
                Credentials.setStoredCredentials(LoginActivity.this, creds);

                //update cloud messaging token
                String token = FCMToken.getStoredToken(LoginActivity.this);
                mRemoteAlarmSystem.setMessagingToken(token, mTokenListener);

            } else {
                EditText password = (EditText) findViewById(R.id.etPassword);
                Button button = (Button) findViewById(R.id.btSignIn);
                password.setText("");
                button.startAnimation(AnimationUtils.loadAnimation(LoginActivity.this, R.anim.shake_on_wrong_input));
            }

            mSubmit.setEnabled(true);
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText hostname = (EditText) findViewById(R.id.etSystemHostname);
        final EditText username = (EditText) findViewById(R.id.etUsername);
        final EditText password = (EditText) findViewById(R.id.etPassword);

        Credentials creds = Credentials.getStoredCredentials(this);
        hostname.setText(creds.getHostname());
        password.setText(creds.getPassword());
        username.setText(creds.getUsername());


        mSubmit = (Button) findViewById(R.id.btSignIn);
        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPwd = password.getText().toString();
                mUser = username.getText().toString();
                mHost = hostname.getText().toString();
                mSubmit.setEnabled(false); //prevent simultaneous requests
                mRemoteAlarmSystem.logOnToSystem(mHost, mUser, mPwd, mLoginListener);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSubmit.callOnClick();
    }
}

