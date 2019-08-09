package com.altona.broadcast.service.query;

import com.altona.broadcast.service.Broadcast;
import com.altona.context.Context;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

@AllArgsConstructor
public class BroadcastRowMapper implements RowMapper<Broadcast> {

    @NonNull
    private Context context;

    @Override
    public Broadcast mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Broadcast(context, rs.getInt("id"), rs.getString("broadcast"));
    }

}
