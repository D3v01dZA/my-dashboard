package com.altona.service.broadcast;

import com.altona.service.time.model.control.TimeStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BroadcastMessage<T> {

    public static BroadcastMessage<TimeStatus> timeStatus(TimeStatus timeStatus) {
        return new BroadcastMessage<>(Type.TIME, timeStatus);
    }

    @NonNull
    private Type type;

    @NonNull
    private T message;

    public enum Type {

        TIME

    }

}
