package com.altona.dashboard.service.firebase;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public class FirebaseUpdate {

    private String oldBroadcast;
    @NonNull
    private String newBroadcast;

}
