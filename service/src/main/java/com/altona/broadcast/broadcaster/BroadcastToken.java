package com.altona.broadcast.broadcaster;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public class BroadcastToken {

    @NonNull
    private String token;

}
