package com.altona.dashboard.view.time;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.altona.dashboard.service.time.NotificationData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import java.util.Objects;

public class TimeNotification extends Service {

    private static final String TIME_CHANNEL_ID = "timeChannel";
    private static final int TIME_NOTIFICATION_ID = 879878979;

    private static final String TIME_STATUS = "timeStatus";

    private static final int TIME_INTENT_CODE = 123981290;

    public static void notify(Context context, TimeStatus timeStatus) {
        if (!timeStatus.requiresNotification()) {
            context.stopService(new Intent(context, TimeNotification.class));
        } else {
            Intent intent = new Intent(context, TimeNotification.class);
            intent.putExtra(TIME_STATUS, timeStatus);
            context.startForegroundService(intent);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        TimeStatus timeStatus = Objects.requireNonNull(Objects.requireNonNull(intent.getExtras()).getParcelable(TIME_STATUS));
        startForeground(TIME_NOTIFICATION_ID, showNotification(getApplicationContext(), timeStatus));
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private static Notification showNotification(Context context, TimeStatus timeStatus) {
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(TIME_CHANNEL_ID, "Name", importance);
            channel.setDescription("Time Channel");
            channel.setSound(null, null);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager.createNotificationChannel(channel);
        }
        NotificationData notificationData = timeStatus.notificationData();
        long current = new Date().getTime();
        long back = ChronoUnit.MILLIS.between(LocalTime.of(0, 0), notificationData.getTime());
        PendingIntent time = PendingIntent.getActivity(context, TIME_INTENT_CODE, new Intent(context, TimeActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        return new NotificationCompat.Builder(context, TIME_CHANNEL_ID)
                .setContentTitle(notificationData.getTitle())
                .setContentText(notificationData.getSubTitle())
                .setSmallIcon(notificationData.getIcon())
                .setOnlyAlertOnce(true)
                .setSound(null)
                .setContentIntent(time)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setShowWhen(true)
                .setWhen(current - back)
                .build();
    }
}
