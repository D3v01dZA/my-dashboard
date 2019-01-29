package com.altona.dashboard.view.time;

import android.view.View;

import com.altona.dashboard.R;
import com.altona.dashboard.service.time.NotificationData;
import com.altona.dashboard.service.time.Project;
import com.altona.dashboard.service.time.TimeStatus;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;

import static com.altona.dashboard.service.time.TimeService.LONG_TIME_FORMATTER;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;

public enum TimeStatusEnum {

    WORK {
        @Override
        public void setButtons(TimeActivity timeActivity, TimeStatus timeStatus) {
            timeActivity.primaryButtonContainer().setVisibility(View.GONE);
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
            return new NotificationData(R.drawable.ic_play, "Time Tracker", "Work", timeStatus.getRunningWorkTotal());
        }
    },
    BREAK {
        @Override
        public void setButtons(TimeActivity timeActivity, TimeStatus timeStatus) {
            timeActivity.primaryButtonContainer().setVisibility(View.GONE);
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
            return new NotificationData(R.drawable.ic_pause, "Time Tracker", "Break", timeStatus.getRunningBreakTotal());
        }
    },
    NONE {
        @Override
        public void setButtons(TimeActivity timeActivity, TimeStatus timeStatus) {
            timeActivity.primaryButtonContainer().setVisibility(View.VISIBLE);
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

    public abstract void setButtons(TimeActivity timeActivity, TimeStatus timeStatus);

    public abstract boolean updateWork();

    public abstract boolean updateBreak();

    public abstract boolean requiresNotification();
    
    public abstract NotificationData notificationData(TimeStatus timeStatus);

    public void setCurrentProject(TimeActivity timeActivity, int projectId) {
        List<Project> projects = timeActivity.currentProjects();
        for (int i = 0; i < projects.size(); i++) {
            if (projects.get(i).getId() == projectId) {
                timeActivity.projectSpinner().setSelection(i);
            }
        }
        throw new IllegalStateException("Couldn't find project id " + projectId + " + in " + projects);
    }

}
