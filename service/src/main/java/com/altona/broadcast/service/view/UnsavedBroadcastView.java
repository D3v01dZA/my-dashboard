package com.altona.broadcast.service.view;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public class UnsavedBroadcastView {

    @NonNull
    private String broadcast;

}
