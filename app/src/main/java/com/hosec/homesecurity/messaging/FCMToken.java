package com.hosec.homesecurity.messaging;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Encapsulates storing of local fcm token
 */
public class FCMToken {

    private static final String TOKEN_KEY = "TOKEN_KEY";
    public static final String DEFAULT_VALUE = "";

    public static void setStoredToken(Context context, String token){
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(TOKEN_KEY,token);
        editor.commit();
    }

    public static String getStoredToken(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getString(TOKEN_KEY,DEFAULT_VALUE);
    }


}
