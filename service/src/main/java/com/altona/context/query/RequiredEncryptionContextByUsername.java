package com.altona.context.query;

import com.altona.context.EncryptionContext;
import com.altona.context.SqlContext;
import com.altona.context.TimeInfo;
import com.altona.user.query.RequiredUserContextByUsername;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.security.core.Authentication;

import java.util.TimeZone;

@AllArgsConstructor
public class RequiredEncryptionContextByUsername {

    @NonNull
    private SqlContext sqlContext;

    @NonNull
    private Authentication authentication;

    @NonNull
    private TimeZone timeZone;

    @NonNull
    private TimeInfo timeInfo;

    public EncryptionContext execute() {
        return EncryptionContext.of(sqlContext, new RequiredUserContextByUsername(sqlContext, authentication, timeZone, timeInfo).execute());
    }

}
