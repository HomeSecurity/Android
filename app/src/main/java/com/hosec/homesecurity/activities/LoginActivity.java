package com.hosec.homesecurity.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;

import com.hosec.homesecurity.R;
import com.hosec.homesecurity.remote.RemoteAlarmSystem;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText hostname = (EditText) findViewById(R.id.etSystemHostname);
        final EditText username = (EditText) findViewById(R.id.etUsername);
        final EditText password = (EditText) findViewById(R.id.etPassword);

        final Button button  = (Button) findViewById(R.id.btSignIn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pwd = password.getText().toString();
                String user = username.getText().toString();
                String host = hostname.getText().toString();

                if(RemoteAlarmSystem.checkSystem(host,user,pwd)){
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this).edit();
                    editor.putString(ChangeSystemDialog.SYSTEM_PREF_KEY,host);
                    editor.putString(ChangeUsernameDialog.KEY,user);
                    editor.putString(ChangePasswordDialog.KEY,pwd);
                    editor.commit();
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    LoginActivity.this.startActivity(intent);
                    LoginActivity.this.finish();
                }else{
                    password.setText("");
                    button.startAnimation(AnimationUtils.loadAnimation(LoginActivity.this,R.anim.shake_on_wrong_input));

                }

            }
        });

    }
}

