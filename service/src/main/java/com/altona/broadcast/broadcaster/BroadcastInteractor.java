package com.altona.broadcast.broadcaster;

import com.altona.security.User;

import java.util.List;

public interface BroadcastInteractor {

    void send(User user, List<BroadcastToken> tokens, BroadcastMessage<?> data);

}
