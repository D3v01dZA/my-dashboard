package com.altona.context.facade;

import com.altona.context.Context;
import com.altona.context.EncryptionContext;
import com.altona.context.SqlContext;
import com.altona.context.query.RequiredContextByUsername;
import com.altona.context.query.RequiredEncryptionContextByUsername;
import com.altona.context.TimeInfo;
import com.altona.user.User;
import com.altona.user.UserContext;
import com.altona.user.query.RequiredUserByUsername;
import com.altona.user.query.RequiredUserContextByUsername;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;

import java.util.TimeZone;

@AllArgsConstructor
public abstract class ContextFacade {

    private SqlContext sqlContext;
    private TimeInfo timeInfo;

    protected Context authenticate(Authentication authentication) {
        return new RequiredContextByUsername(sqlContext, authentication).execute();
    }

    protected EncryptionContext authenticate(Authentication authentication, TimeZone timeZone) {
        return new RequiredEncryptionContextByUsername(sqlContext, authentication, timeZone, timeInfo).execute();
    }

    protected User legacyAuthenticate(Authentication authentication) {
        return new RequiredUserByUsername(sqlContext, authentication).execute();
    }

    protected UserContext legacyAuthenticate(Authentication authentication, TimeZone timeZone) {
        return new RequiredUserContextByUsername(sqlContext, authentication, timeZone, timeInfo).execute();
    }

}
