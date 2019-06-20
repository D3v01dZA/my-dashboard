package com.altona.dashboard.service.firebase;

import com.altona.dashboard.service.Settings;
import com.altona.dashboard.service.login.LoginService;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class Firebase extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
    }

    @Override
    public void onNewToken(String newId) {
        super.onNewToken(newId);
        FirebaseUpdate firebaseUpdate = settings().getFirebaseId()
                .map(oldId -> new FirebaseUpdate(oldId, newId))
                .orElseGet(() -> new FirebaseUpdate(null, newId));
        loginService().updateFirebaseToken(firebaseUpdate);
    }

    private Settings settings() {
        return new Settings(this);
    }

    private LoginService loginService() {
        return new LoginService(this);
    }

}
