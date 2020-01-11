package com.altona.project.time.modify;

import com.altona.context.EncryptionContext;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.util.Assert;

import java.sql.Date;

@AllArgsConstructor
public class EndTime {

    @NonNull
    private EncryptionContext encryptionContext;

    private int id;

    public void execute() {
        int workUpdate = encryptionContext.update(
                "UPDATE time SET end_time = :endTime WHERE id = :id",
                new MapSqlParameterSource()
                        .addValue("endTime", Date.from(encryptionContext.now()))
                        .addValue("id", id)
        );
        Assert.isTrue(workUpdate == 1, "Expected one row to be updated");
    }

}
