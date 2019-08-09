package com.altona.context.query;

import com.altona.context.EncryptionContext;
import com.altona.context.SqlContext;
import com.altona.service.time.util.TimeInfo;
import com.altona.user.service.query.UserContextByUsername;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.security.core.Authentication;

import java.util.TimeZone;

@AllArgsConstructor
public class EncryptionContextByUsername {

    @NonNull
    private SqlContext sqlContext;

    @NonNull
    private Authentication authentication;

    @NonNull
    private TimeZone timeZone;

    @NonNull
    private TimeInfo timeInfo;

    public EncryptionContext execute() {
        return EncryptionContext.of(sqlContext, new UserContextByUsername(sqlContext, authentication, timeZone, timeInfo).execute());
    }

}
