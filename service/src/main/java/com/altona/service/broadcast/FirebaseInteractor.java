package com.altona.service.broadcast;

import com.altona.security.User;

import java.util.List;

public interface FirebaseInteractor {

    void send(User user, List<String> tokens, BroadcastMessage<?> data);

}
