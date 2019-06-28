package com.altona.service.broadcast;

import com.altona.security.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class MockBroadcast {

    private User user;
    private List<String> tokens;
    private BroadcastMessage<?> broadcastMessage;

}
