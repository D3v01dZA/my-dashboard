package com.altona.dashboard.service.time;

import android.os.Parcel;
import android.os.Parcelable;

import com.altona.dashboard.view.time.TimeActivity;
import com.altona.dashboard.view.time.TimeNotificationService;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TimeStatus implements Parcelable {

    private static final LocalTime NO_TIME = LocalTime.of(0, 0);

    @Getter
    private TimeActivity.Status status;

    private LocalDateTime lastUpdate;

    @Getter
    private LocalTime runningWorkTotal;
    @Getter
    private LocalTime runningBreakTotal;

    @JsonCreator
    TimeStatus(
            @JsonProperty(value = "status", required = true) TimeActivity.Status status,
            @JsonProperty(value = "runningWorkTotal") LocalTime runningWorkTotal,
            @JsonProperty(value = "runningBreakTotal") LocalTime runningBreakTotal
    ) {
        this(
                status,
                LocalDateTime.now(),
                runningWorkTotal == null ? NO_TIME : runningWorkTotal,
                runningBreakTotal == null ? NO_TIME : runningBreakTotal
        );
    }

    public boolean requiresNotification() {
        return status.requiresNotification();
    }

    public void update(TimeActivity timeActivity) {
        LocalDateTime now = LocalDateTime.now();
        if (status.updateWork()) {
            runningWorkTotal = runningWorkTotal.plus(ChronoUnit.NANOS.between(lastUpdate, now), ChronoUnit.NANOS);
        }
        if (status.updateBreak()) {
            runningBreakTotal = runningBreakTotal.plus(ChronoUnit.NANOS.between(lastUpdate, now), ChronoUnit.NANOS);
        }
        lastUpdate = now;
        status.setButtons(timeActivity);
    }

    public void startWork(TimeActivity timeActivity) {
        status = TimeActivity.Status.WORK;
        if (runningWorkTotal == null) {
            runningWorkTotal = NO_TIME;
        }
        if (runningBreakTotal == null) {
            runningBreakTotal = NO_TIME;
        }
        updateUiAndNotification(timeActivity);
    }

    public void startBreak(TimeActivity timeActivity) {
        status = TimeActivity.Status.BREAK;
        updateUiAndNotification(timeActivity);
    }

    public void stopBreak(TimeActivity timeActivity) {
        status = TimeActivity.Status.WORK;
        updateUiAndNotification(timeActivity);
    }

    public void stopWork(TimeActivity timeActivity) {
        status = TimeActivity.Status.NONE;
        runningWorkTotal = NO_TIME;
        runningBreakTotal = NO_TIME;
        updateUiAndNotification(timeActivity);
    }

    private void updateUiAndNotification(TimeActivity timeActivity) {
        status.setButtons(timeActivity);
        TimeNotificationService.schedule(timeActivity, this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(status.name());
        dest.writeString(lastUpdate.toString());
        dest.writeString(runningWorkTotal.toString());
        dest.writeString(runningBreakTotal.toString());
    }

    public static final Parcelable.Creator<TimeStatus> CREATOR = new Parcelable.Creator<TimeStatus>() {
        @Override
        public TimeStatus createFromParcel(Parcel source) {
            return new TimeStatus(
                    TimeActivity.Status.valueOf(source.readString()),
                    LocalDateTime.parse(source.readString()),
                    LocalTime.parse(source.readString()),
                    LocalTime.parse(source.readString())
            );
        }

        @Override
        public TimeStatus[] newArray(int size) {
            return new TimeStatus[size];
        }
    };
}
