package com.altona.broadcast.operation;

import com.altona.broadcast.Broadcast;
import com.altona.broadcast.UnsavedBroadcast;
import com.altona.broadcast.query.BroadcastByBroadcast;
import com.altona.context.Context;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
public class BroadcastUpdate {

    private String oldBroadcast;

    @NonNull
    private String newBroadcast;

    public Broadcast execute(Context context) {
        if (oldBroadcast != null) {
            new BroadcastByBroadcast(context, oldBroadcast).execute()
                    .ifPresent(Broadcast::delete);
        }
        return new BroadcastByBroadcast(context, newBroadcast).execute()
                .orElseGet(() -> new UnsavedBroadcast(context, newBroadcast).save());
    }

}
