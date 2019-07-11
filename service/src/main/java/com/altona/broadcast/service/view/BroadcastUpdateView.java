package com.altona.broadcast.service.view;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BroadcastUpdateView {

    private String oldBroadcast;
    private String newBroadcast;

}
