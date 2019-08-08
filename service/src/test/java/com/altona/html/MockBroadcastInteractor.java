package com.altona.html;

import com.altona.broadcast.broadcaster.BroadcastInteractor;
import com.altona.broadcast.broadcaster.BroadcastMessage;
import com.altona.broadcast.broadcaster.BroadcastToken;
import com.altona.context.Context;
import com.altona.util.Result;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MockBroadcastInteractor implements BroadcastInteractor {

    private static final int TIMEOUT = 30000;

    private volatile MockBroadcast mockBroadcast;
    private volatile RuntimeException exception;

    @Override
    public void send(Context context, List<BroadcastToken> tokens, BroadcastMessage<?> broadcastMessage) {
        if (mockBroadcast != null) {
            exception = new RuntimeException("Received multiple broadcasts");
        }
        mockBroadcast = new MockBroadcast(context, tokens, broadcastMessage);
    }

    public Result<MockBroadcast, RuntimeException> result() {
        long timeMillis = System.currentTimeMillis();
        while (System.currentTimeMillis() - timeMillis < TIMEOUT) {
            if (mockBroadcast != null) {
                Result<MockBroadcast, RuntimeException> success = Result.success(mockBroadcast);
                clear();
                return success;
            }
            if (exception != null) {
                Result<MockBroadcast, RuntimeException> failure = Result.failure(exception);
                clear();
                return failure;
            }
        }
        clear();
        return Result.failure(new RuntimeException("No broadcast received after " + TIMEOUT + " ms"));
    }

    private void clear() {
        mockBroadcast = null;
        exception = null;
    }

}
