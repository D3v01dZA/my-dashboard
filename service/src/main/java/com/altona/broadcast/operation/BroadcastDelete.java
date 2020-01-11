package com.altona.broadcast.operation;

import com.altona.broadcast.Broadcast;
import com.altona.broadcast.UnsavedBroadcast;
import com.altona.broadcast.query.BroadcastByBroadcast;
import com.altona.context.Context;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.util.Optional;

@AllArgsConstructor
public class BroadcastDelete {

    @NonNull
    private String broadcast;

    public Optional<UnsavedBroadcast> execute(Context context) {
        return new BroadcastByBroadcast(context, broadcast).execute()
                .map(Broadcast::delete);
    }

}
