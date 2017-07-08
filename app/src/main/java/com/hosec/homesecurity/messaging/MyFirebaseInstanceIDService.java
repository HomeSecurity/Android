package com.hosec.homesecurity.messaging;


import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.hosec.homesecurity.remote.RemoteAlarmSystem;

/**
 * Created by D062572 on 30.06.2017.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    public static String msToken = null;

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.i("HomeSecurity", refreshedToken);
        msToken = refreshedToken;

    }

}
