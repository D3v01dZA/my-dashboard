package com.altona.dashboard.service.boot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.altona.dashboard.service.login.LoginService;
import com.altona.dashboard.service.time.TimeService;
import com.altona.dashboard.view.time.TimeNotification;

import java.util.logging.Logger;

public class BootReceiver extends BroadcastReceiver {

    private static final Logger LOGGER = Logger.getLogger(BootReceiver.class.getName());

    @Override
    public void onReceive(Context context, Intent intent) {
        LOGGER.info(() -> "Starting intent " + intent.getAction());
        new TimeService(new LoginService(context)).queryStatus(
                timeStatus -> TimeNotification.notify(context, timeStatus),
                string -> LOGGER.severe(() -> "Failed on boot " + string),
                () -> LOGGER.warning("User is not authenticated")
        );
    }

}
