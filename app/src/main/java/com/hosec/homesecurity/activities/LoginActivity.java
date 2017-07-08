package com.hosec.homesecurity.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hosec.homesecurity.R;
import com.hosec.homesecurity.model.Credentials;
import com.hosec.homesecurity.remote.NetworkFragment;
import com.hosec.homesecurity.remote.RemoteAlarmSystem;
import com.hosec.homesecurity.remote.RequestCallback;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements RemoteAlarmSystem.ResultListener{

    private RemoteAlarmSystem mRemoteAlarmSystem;
    private String mPwd;
    private String mUser;
    private String mHost;
    public static final String UNABLE_TO_CONNECT_WITH_KNOWN_CREDS_KEY = "UNABLE_TO_CONNECT_WITH_KNOWN_CREDS_KEY";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Intent intent = getIntent();
        boolean unableToConnect = intent.getBooleanExtra(UNABLE_TO_CONNECT_WITH_KNOWN_CREDS_KEY, false);

        final EditText hostname = (EditText) findViewById(R.id.etSystemHostname);
        final EditText username = (EditText) findViewById(R.id.etUsername);
        final EditText password = (EditText) findViewById(R.id.etPassword);

        if(unableToConnect){
            Credentials creds = Credentials.getStoredCredentials(this);
            hostname.setText(creds.getHostname());
            password.setText(creds.getPassword());
            username.setText(creds.getUsername());
        }

        final Button button  = (Button) findViewById(R.id.btSignIn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPwd = password.getText().toString();
                mUser = username.getText().toString();
                mHost = hostname.getText().toString();

                mRemoteAlarmSystem = RemoteAlarmSystem.getInstance(LoginActivity.this);
                mRemoteAlarmSystem.logOnToSystem(mHost,mUser,mPwd, LoginActivity.this);
            }
        });

    }


    @Override
    public void onResult(RemoteAlarmSystem.Result result) {
        if(result.success) {

            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this).edit();
            editor.putString(ChangeSystemDialog.SYSTEM_PREF_KEY, mHost);
            editor.putString(ChangeUsernameDialog.KEY, mUser);
            editor.putString(ChangePasswordDialog.KEY, mPwd);
            editor.commit();
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            LoginActivity.this.startActivity(intent);
            LoginActivity.this.finish();

        }else{
            EditText password = (EditText) findViewById(R.id.etPassword);

            Button button  = (Button) findViewById(R.id.btSignIn);
            password.setText("");
            button.startAnimation(AnimationUtils.loadAnimation(LoginActivity.this,R.anim.shake_on_wrong_input));
            Toast.makeText(this,result.message,Toast.LENGTH_SHORT).show();
        }
    }
}

