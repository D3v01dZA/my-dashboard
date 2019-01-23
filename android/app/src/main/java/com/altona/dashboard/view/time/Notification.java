package com.altona.dashboard.view.time;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.altona.dashboard.MainActivity;
import com.altona.dashboard.R;

public class Notification {

    private static final String TIME_CHANNEL_ID = "TIME";
    private static final int TIME_NOTIFICATION_ID = 0;

    public static void display(MainActivity mainActivity) {
        NotificationManager notificationManager = mainActivity.getSystemService(NotificationManager.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(TIME_CHANNEL_ID, "Name", importance);
            channel.setDescription("Time Channel");
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mainActivity, TIME_CHANNEL_ID)
                .setContentTitle("Test")
                .setContentText("Does This Work")
                .setSmallIcon(R.drawable.ic_menu_time)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        notificationManager.notify(TIME_NOTIFICATION_ID, builder.build());
    }

}
