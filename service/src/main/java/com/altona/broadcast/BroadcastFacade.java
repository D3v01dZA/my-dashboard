package com.altona.broadcast;

import com.altona.broadcast.operation.BroadcastDelete;
import com.altona.broadcast.operation.BroadcastUpdate;
import com.altona.broadcast.query.BroadcastById;
import com.altona.broadcast.query.BroadcastsByUser;
import com.altona.context.Context;
import com.altona.context.facade.ContextFacade;
import com.altona.context.SqlContext;
import com.altona.context.TimeInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BroadcastFacade extends ContextFacade {

    @Autowired
    public BroadcastFacade(SqlContext sqlContext, TimeInfo timeInfo) {
        super(sqlContext, timeInfo);
    }

    @Transactional
    public Broadcast update(Authentication authentication, BroadcastUpdate broadcastUpdate) {
        Context context = authenticate(authentication);
        return broadcastUpdate.execute(context);
    }

    @Transactional
    public Optional<UnsavedBroadcast> delete(Authentication authentication, BroadcastDelete broadcastDelete) {
        Context context = authenticate(authentication);
        return broadcastDelete.execute(context);
    }

    @Transactional(readOnly = true)
    public Optional<Broadcast> broadcast(Authentication authentication, int id) {
        Context context = authenticate(authentication);
        return new BroadcastById(context, id).execute();
    }

    @Transactional(readOnly = true)
    public List<Broadcast> broadcasts(Authentication authentication) {
        Context context = authenticate(authentication);
        return new BroadcastsByUser(context).execute();
    }

}
