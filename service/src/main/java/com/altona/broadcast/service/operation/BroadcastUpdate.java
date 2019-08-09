package com.altona.broadcast.service.operation;

import com.altona.broadcast.service.Broadcast;
import com.altona.broadcast.service.UnsavedBroadcast;
import com.altona.broadcast.service.query.BroadcastByBroadcast;
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
