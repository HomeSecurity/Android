package com.hosec.homesecurity.messaging;


import android.content.Intent;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.hosec.homesecurity.remote.RemoteAlarmSystem;

/**
 * This service is responsible for obtaining a valid token which can be used for FCM
 */
public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        // Get updated FCM token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("HomeSecurity", refreshedToken);
        FCMToken.setStoredToken(this,refreshedToken);
        sendTokenToAlarmSystem(refreshedToken);

    }

    private void sendTokenToAlarmSystem(String token){
        RemoteAlarmSystem.getInstance(this).setMessagingToken(token,null);
    }

}
