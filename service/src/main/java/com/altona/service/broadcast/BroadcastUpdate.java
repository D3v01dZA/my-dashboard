package com.altona.service.broadcast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.Optional;

@AllArgsConstructor
public class BroadcastUpdate {

    private String oldBroadcast;

    @Getter
    @NonNull
    private String newBroadcast;

    public Optional<String> getOldBroadcast() {
        return Optional.ofNullable(oldBroadcast);
    }
}
