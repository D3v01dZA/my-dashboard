package com.altona.broadcast.broadcaster;

import com.altona.context.Context;

import java.util.List;

public interface BroadcastInteractor {

    void send(Context context, List<BroadcastToken> tokens, BroadcastMessage<?> data);

}
