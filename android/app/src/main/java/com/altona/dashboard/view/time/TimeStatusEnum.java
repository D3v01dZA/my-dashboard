package com.altona.dashboard.view.time;

import android.view.View;

import com.altona.dashboard.R;
import com.altona.dashboard.service.time.NotificationData;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;

public enum TimeStatusEnum {

    WORK {
        @Override
        public void setButtons(TimeActivity timeActivity, TimeStatus timeStatus) {
            timeActivity.startButton().setVisibility(View.GONE);
            timeActivity.secondaryButtonContainer().setVisibility(View.VISIBLE);
            timeActivity.pauseButton().setText("Pause");
            timeActivity.runningWorked().setText("Worked: " + timeStatus.getRunningWorkTotal(LONG_TIME_FORMATTER));
            timeActivity.runningBreaks().setText("Paused: " + timeStatus.getRunningBreakTotal(LONG_TIME_FORMATTER));
        }

        @Override
        public boolean updateWork() {
            return true;
        }

        @Override
        public boolean updateBreak() {
            return false;
        }

        @Override
        public boolean requiresNotification() {
            return true;
        }

        @Override
        public NotificationData notificationData(TimeStatus timeStatus) {
            return new NotificationData(R.drawable.ic_play, "Work", timeStatus.getRunningWorkTotal(SHORT_TIME_FORMATTER));
        }
    },
    BREAK {
        @Override
        public void setButtons(TimeActivity timeActivity, TimeStatus timeStatus) {
            timeActivity.startButton().setVisibility(View.GONE);
            timeActivity.secondaryButtonContainer().setVisibility(View.VISIBLE);
            timeActivity.pauseButton().setText("Resume");
            timeActivity.runningWorked().setText("Worked: " + timeStatus.getRunningWorkTotal(LONG_TIME_FORMATTER));
            timeActivity.runningBreaks().setText("Paused: " + timeStatus.getRunningBreakTotal(LONG_TIME_FORMATTER));
        }

        @Override
        public boolean updateWork() {
            return false;
        }

        @Override
        public boolean updateBreak() {
            return true;
        }

        @Override
        public boolean requiresNotification() {
            return true;
        }

        @Override
        public NotificationData notificationData(TimeStatus timeStatus) {
            return new NotificationData(R.drawable.ic_pause, "Break", timeStatus.getRunningBreakTotal(SHORT_TIME_FORMATTER));
        }
    },
    NONE {
        @Override
        public void setButtons(TimeActivity timeActivity, TimeStatus timeStatus) {
            timeActivity.startButton().setVisibility(View.VISIBLE);
            timeActivity.secondaryButtonContainer().setVisibility(View.GONE);
            timeActivity.runningWorked().setText("Worked: " + timeStatus.getRunningWorkTotal(LONG_TIME_FORMATTER));
            timeActivity.runningBreaks().setText("Paused: " + timeStatus.getRunningBreakTotal(LONG_TIME_FORMATTER));
        }

        @Override
        public boolean updateWork() {
            return false;
        }

        @Override
        public boolean updateBreak() {
            return false;
        }

        @Override
        public boolean requiresNotification() {
            return false;
        }

        @Override
        public NotificationData notificationData(TimeStatus timeStatus) {
            throw new IllegalStateException("No Notification For You");
        }
    };

    private static final DateTimeFormatter LONG_TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(MINUTE_OF_HOUR, 2)
            .appendLiteral(':')
            .appendValue(SECOND_OF_MINUTE, 2)
            .toFormatter();

    private static final DateTimeFormatter SHORT_TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(MINUTE_OF_HOUR, 2)
            .toFormatter();

    public abstract void setButtons(TimeActivity timeActivity, TimeStatus timeStatus);

    public abstract boolean updateWork();

    public abstract boolean updateBreak();

    public abstract boolean requiresNotification();
    
    public abstract NotificationData notificationData(TimeStatus timeStatus);

}
