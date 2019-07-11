package com.altona.dashboard.service.time.synchronization;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.altona.dashboard.R;
import com.altona.dashboard.service.time.SynchronizationAttemptNotificationData;

public class SynchronizationNotification {

    private static final String SYNCHRONIZATION_ATTEMPT_CHANNEL_ID = "synchronizationAttemptChannel";

    public static void success(Context context, SynchronizationAttemptBroadcast synchronizationAttemptBroadcast, String location) {
        showNotification(
                context,
                new SynchronizationAttemptNotificationData(
                        R.drawable.ic_success,
                        String.format("Synchronization %s complete", synchronizationAttemptBroadcast.getId()),
                        String.format("Saved to %s", location),
                        location
                ),
                synchronizationAttemptBroadcast
        );
    }

    public static void success(Context context, SynchronizationAttemptBroadcast synchronizationAttemptBroadcast) {
        showNotification(
                context,
                new SynchronizationAttemptNotificationData(
                        R.drawable.ic_success,
                        String.format("Synchronization %s complete", synchronizationAttemptBroadcast.getId()),
                        "Screenshot not saved",
                        null
                ),
                synchronizationAttemptBroadcast
        );
    }

    public static void failure(Context context, SynchronizationAttemptBroadcast synchronizationAttemptBroadcast, String failure) {
        showNotification(
                context,
                new SynchronizationAttemptNotificationData(
                        R.drawable.ic_failure,
                        String.format("Synchronization %s failed", synchronizationAttemptBroadcast.getId()),
                        "Reason: " + failure,
                        null
                ),
                synchronizationAttemptBroadcast
        );
    }

    private static void showNotification(Context context, SynchronizationAttemptNotificationData notificationData, SynchronizationAttemptBroadcast synchronizationAttemptBroadcast) {
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(SYNCHRONIZATION_ATTEMPT_CHANNEL_ID, "Name", importance);
            channel.setDescription("Time Channel");
            channel.setSound(null, null);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder notification = new NotificationCompat.Builder(context, SYNCHRONIZATION_ATTEMPT_CHANNEL_ID)
                .setContentTitle(notificationData.getTitle())
                .setContentText(notificationData.getSubTitle())
                .setSmallIcon(notificationData.getIcon())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
        notificationData.getLocation().ifPresent(location -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(location), "image/*");
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            notification.setContentIntent(pendingIntent);
        });
        notificationManager.notify(synchronizationAttemptBroadcast.getId(), notification.build());
    }

}
