package com.altona.dashboard.service.time;

import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public class SynchronizationAttemptNotificationData {

    @NonNull
    private int icon;

    @NonNull
    private String title;

    @NonNull
    private String subTitle;

    private String location;

    public Optional<String> getLocation() {
        return Optional.ofNullable(location);
    }

}
