package com.altona.dashboard.view.time;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.altona.dashboard.R;
import com.altona.dashboard.service.time.TimeStatus;

public class TimeNotificationService extends JobService {

    private static final String TIME_CHANNEL_ID = "timeChannel";
    private static final int TIME_NOTIFICATION_ID = 879878979;

    private static final int TIME_NOTIFICATION_JOB_ID = 218391830;
    private static final String TIME_STATUS = "timeStatus";

    public static void schedule(Context context, TimeStatus timeStatus) {
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        if (!timeStatus.requiresNotification()) {
            jobScheduler.cancel(TIME_NOTIFICATION_JOB_ID);
            cancelNotification(context);
        } else {
            ComponentName componentName = new ComponentName(context, TimeNotificationService.class);
            Bundle extras = new Bundle();
            extras.putParcelable(TIME_STATUS, timeStatus);
            jobScheduler.schedule(
                    new JobInfo.Builder(TIME_NOTIFICATION_JOB_ID, componentName)
                            .setMinimumLatency(0)
                            .setPeriodic(1000)
                            .setTransientExtras(extras)
                            .build()
            );
        }
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Bundle extras = params.getTransientExtras();
        extras.setClassLoader(getClassLoader());
        TimeStatus timeStatus = extras.getParcelable(TIME_STATUS);
        showNotification(getApplicationContext(), timeStatus);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        cancelNotification(getApplicationContext());
        return true;
    }


    private static void cancelNotification(Context context) {
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.cancel(TIME_NOTIFICATION_ID);
    }


    private static void showNotification(Context context, TimeStatus timeStatus) {
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
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, TIME_CHANNEL_ID)
                .setContentTitle("Test")
                .setContentText("Does This Work")
                .setSmallIcon(R.drawable.ic_menu_time)
                .setOnlyAlertOnce(true)
                .setSound(null)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        notificationManager.notify(TIME_NOTIFICATION_ID, builder.build());
    }

}
