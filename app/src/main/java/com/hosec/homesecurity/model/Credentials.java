package com.hosec.homesecurity.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.Serializable;

/**
 * Encapsulates saving credentials in a shared preferences storage location
 */
public class Credentials implements Serializable {

    public static final String SYSTEM_PREF_KEY = "system_pref_key";
    public static final String USERNAME_PREF_KEY = "username_pref_key";
    public static final String PASSWORD_PREF_KEY = "password_pref_key";
    public static final String DEFAULT_VALUE = "";

    private String username, password, hostname;

    public static Credentials getStoredCredentials(Context context) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String hostname = sharedPreferences.getString(SYSTEM_PREF_KEY, DEFAULT_VALUE);
        String username = sharedPreferences.getString(USERNAME_PREF_KEY, DEFAULT_VALUE);
        String password = sharedPreferences.getString(PASSWORD_PREF_KEY, DEFAULT_VALUE);
        return new Credentials(username, password, hostname);
    }

    public static void setStoredCredentials(Context context, Credentials credentials) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(SYSTEM_PREF_KEY, credentials.getHostname());
        editor.putString(USERNAME_PREF_KEY, credentials.getUsername());
        editor.putString(PASSWORD_PREF_KEY, credentials.getPassword());
        editor.commit();
    }

    public Credentials(String username, String password, String hostname) {
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
