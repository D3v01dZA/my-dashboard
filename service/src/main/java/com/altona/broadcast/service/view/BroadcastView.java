package com.altona.broadcast.service.view;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public class BroadcastView {

    private int id;

    @NonNull
    private String broadcast;

}
