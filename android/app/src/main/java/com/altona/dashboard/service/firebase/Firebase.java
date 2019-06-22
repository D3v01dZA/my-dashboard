package com.altona.dashboard.service.firebase;

import com.altona.dashboard.Static;
import com.altona.dashboard.service.login.LoginService;
import com.altona.dashboard.service.time.TimeStatus;
import com.altona.dashboard.view.time.TimeNotification;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.util.logging.Logger;

public class Firebase extends FirebaseMessagingService {

    private static final Logger LOGGER = Logger.getLogger(Firebase.class.getName());

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        try {
            String type = remoteMessage.getData().get("type");
            LOGGER.info(() -> "Received background message of type " + type);
            if ("TIME".equals(type)) {
                TimeStatus timeStatus = Static.OBJECT_MAPPER.readValue(remoteMessage.getData().get("message"), TimeStatus.class);
                TimeNotification.notify(this, timeStatus);
            } else {
                LOGGER.info(() -> "Cannot handle message type " + type);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onNewToken(String newId) {
        loginService().updateFirebaseToken(newId);
    }

    private LoginService loginService() {
        return new LoginService(this);
    }

}
