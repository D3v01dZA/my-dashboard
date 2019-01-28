package com.altona.dashboard.view.time;

import android.os.Parcel;
import android.os.Parcelable;

import com.altona.dashboard.service.time.NotificationData;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TimeStatus implements Parcelable {

    private static final LocalTime NO_TIME = LocalTime.of(0, 0);

    private TimeStatusEnum status;
    private Integer projectId;
    private Integer timeId;
    private LocalDateTime lastUpdate;
    @Getter
    private LocalTime runningWorkTotal;
    @Getter
    private LocalTime runningBreakTotal;

    @JsonCreator
    TimeStatus(
            @JsonProperty(value = "status", required = true) TimeStatusEnum status,
            @JsonProperty(value = "projectId") Integer projectId,
            @JsonProperty(value = "timeId") Integer timeId,
            @JsonProperty(value = "runningWorkTotal") LocalTime runningWorkTotal,
            @JsonProperty(value = "runningBreakTotal") LocalTime runningBreakTotal
    ) {
        this(
                status,
                projectId,
                timeId,
                LocalDateTime.now(),
                runningWorkTotal == null ? NO_TIME : runningWorkTotal,
                runningBreakTotal == null ? NO_TIME : runningBreakTotal
        );
    }

    public boolean requiresNotification() {
        return status.requiresNotification();
    }

    public void update(TimeActivity timeActivity) {
        update();
        status.setButtons(timeActivity, this);
        if (projectId != null) {
            Project.setCurrentProject(timeActivity, projectId);
        }
    }

    public void update() {
        LocalDateTime now = LocalDateTime.now();
        if (status.updateWork()) {
            runningWorkTotal = runningWorkTotal.plus(ChronoUnit.NANOS.between(lastUpdate, now), ChronoUnit.NANOS);
        }
        if (status.updateBreak()) {
            runningBreakTotal = runningBreakTotal.plus(ChronoUnit.NANOS.between(lastUpdate, now), ChronoUnit.NANOS);
        }
        lastUpdate = now;
    }

    public void startWork(TimeActivity timeActivity) {
        status = TimeStatusEnum.WORK;
        if (runningWorkTotal == null) {
            runningWorkTotal = NO_TIME;
        }
        if (runningBreakTotal == null) {
            runningBreakTotal = NO_TIME;
        }
        updateUiAndNotification(timeActivity);
    }

    public void startBreak(TimeActivity timeActivity) {
        status = TimeStatusEnum.BREAK;
        updateUiAndNotification(timeActivity);
    }

    public void stopBreak(TimeActivity timeActivity) {
        status = TimeStatusEnum.WORK;
        updateUiAndNotification(timeActivity);
    }

    public void stopWork(TimeActivity timeActivity) {
        status = TimeStatusEnum.NONE;
        runningWorkTotal = NO_TIME;
        runningBreakTotal = NO_TIME;
        projectId = null;
        timeId = null;
        updateUiAndNotification(timeActivity);
    }

    public String getRunningWorkTotal(DateTimeFormatter formatter) {
        return runningWorkTotal.format(formatter);
    }

    public String getRunningBreakTotal(DateTimeFormatter formatter) {
        return runningBreakTotal.format(formatter);
    }

    public NotificationData notificationData() {
        update();
        return status.notificationData(this);
    }

    private void updateUiAndNotification(TimeActivity timeActivity) {
        status.setButtons(timeActivity, this);
        TimeNotification.notify(timeActivity, this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(status.name());
        if (projectId == null) {
            dest.writeInt(-1);
        } else {
            dest.writeInt(projectId);
        }
        if (timeId == null) {
            dest.writeInt(-1);
        } else {
            dest.writeInt(timeId);
        }
        dest.writeString(lastUpdate.toString());
        dest.writeString(runningWorkTotal.toString());
        dest.writeString(runningBreakTotal.toString());
    }

    public static final Parcelable.Creator<TimeStatus> CREATOR = new Parcelable.Creator<TimeStatus>() {
        @Override
        public TimeStatus createFromParcel(Parcel source) {
            TimeStatusEnum status = TimeStatusEnum.valueOf(source.readString());
            int projectId = source.readInt();
            int timeId = source.readInt();
            LocalDateTime lastUpdate = LocalDateTime.parse(source.readString());
            LocalTime runningWorkTotal = LocalTime.parse(source.readString());
            LocalTime runningBreakTotal = LocalTime.parse(source.readString());
            return new TimeStatus(
                    status,
                    projectId == -1 ? null : projectId,
                    timeId == -1 ? null : projectId,
                    lastUpdate,
                    runningWorkTotal,
                    runningBreakTotal
            );
        }

        @Override
        public TimeStatus[] newArray(int size) {
            return new TimeStatus[size];
        }
    };
}
