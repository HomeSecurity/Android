package com.hosec.homesecurity.model;

import android.content.Context;
import android.preference.PreferenceManager;

import com.hosec.homesecurity.activities.ChangePasswordDialog;
import com.hosec.homesecurity.activities.ChangeSystemDialog;
import com.hosec.homesecurity.activities.ChangeUsernameDialog;

import java.io.Serializable;

/**
 * Created by D062572 on 19.06.2017.
 */

public class Credentials implements Serializable{

    private String username, password, hostname;


    public static Credentials getStoredCredentials(Context context){
        String hostname = PreferenceManager.getDefaultSharedPreferences(context).getString(
                ChangeSystemDialog.SYSTEM_PREF_KEY,
                ChangeSystemDialog.DEFAULT_VALUE);

        String username = PreferenceManager.getDefaultSharedPreferences(context).getString(
                ChangeUsernameDialog.KEY,
                ChangeUsernameDialog.DEFAULT_VALUE);

        String password = PreferenceManager.getDefaultSharedPreferences(context).getString(
                ChangePasswordDialog.KEY,
                ChangePasswordDialog.DEFAULT_VALUE);

        return new Credentials(username,password,hostname);

    }

    private Credentials(String username, String password, String hostname) {
        this.username = username;
        this.password = password;
        this.hostname = hostname;
    }



    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
}
