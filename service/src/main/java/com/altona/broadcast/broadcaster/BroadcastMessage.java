package com.altona.broadcast.broadcaster;

import com.altona.project.time.view.TimeStatusView;
import com.altona.service.synchronization.model.SynchronizationAttemptBroadcast;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BroadcastMessage<T> {

    public static BroadcastMessage<TimeStatusView> timeStatus(TimeStatusView timeStatus) {
        return new BroadcastMessage<>(Type.TIME, timeStatus);
    }

    public static BroadcastMessage<SynchronizationAttemptBroadcast> synchronization(SynchronizationAttemptBroadcast attempt) {
        return new BroadcastMessage<>(Type.SYNCHRONIZE_ATTEMPT, attempt);
    }

    @NonNull
    private Type type;

    @NonNull
    private T message;

    public enum Type {

        TIME,
        SYNCHRONIZE_ATTEMPT

    }

}
