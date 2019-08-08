package com.altona.broadcast;

import com.altona.broadcast.service.Broadcast;
import com.altona.broadcast.service.UnsavedBroadcast;
import com.altona.broadcast.service.operation.BroadcastDelete;
import com.altona.broadcast.service.operation.BroadcastUpdate;
import com.altona.broadcast.service.query.BroadcastById;
import com.altona.broadcast.service.query.BroadcastsByUser;
import com.altona.context.Context;
import com.altona.security.User;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class BroadcastFacade {

    private NamedParameterJdbcTemplate jdbcTemplate;

    @Transactional
    public Broadcast update(User user, BroadcastUpdate broadcastUpdate) {
        return broadcastUpdate.execute(Context.of(user, jdbcTemplate));
    }

    @Transactional
    public Optional<UnsavedBroadcast> delete(User user, BroadcastDelete broadcastDelete) {
        return broadcastDelete.execute(Context.of(user, jdbcTemplate));
    }

    @Transactional(readOnly = true)
    public Optional<Broadcast> broadcast(User user, int id) {
        return new BroadcastById(id, Context.of(user, jdbcTemplate)).execute();
    }

    @Transactional(readOnly = true)
    public List<Broadcast> broadcasts(User user) {
        return new BroadcastsByUser(Context.of(user, jdbcTemplate)).execute();
    }

}
