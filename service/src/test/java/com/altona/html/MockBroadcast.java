package com.altona.html;

import com.altona.broadcast.broadcaster.BroadcastMessage;
import com.altona.broadcast.broadcaster.BroadcastToken;
import com.altona.context.Context;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class MockBroadcast {

    private Context context;
    private List<BroadcastToken> tokens;
    private BroadcastMessage<?> broadcastMessage;

}
