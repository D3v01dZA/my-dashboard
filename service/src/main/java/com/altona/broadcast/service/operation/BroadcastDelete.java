package com.altona.broadcast.service.operation;

import com.altona.broadcast.service.Broadcast;
import com.altona.broadcast.service.UnsavedBroadcast;
import com.altona.broadcast.service.query.BroadcastByBroadcast;
import com.altona.context.Context;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.util.Optional;

@AllArgsConstructor
public class BroadcastDelete {

    @NonNull
    private String broadcast;

    public Optional<UnsavedBroadcast> execute(Context context) {
        return new BroadcastByBroadcast(broadcast, context).execute()
                .map(Broadcast::delete);
    }

}
