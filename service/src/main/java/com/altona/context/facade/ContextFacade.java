package com.altona.context.facade;

import com.altona.context.Context;
import com.altona.context.SqlContext;
import com.altona.context.query.ContextByUsername;
import com.altona.service.time.util.TimeInfo;
import com.altona.user.service.User;
import com.altona.user.service.UserContext;
import com.altona.user.service.query.UserByUsername;
import com.altona.user.service.query.UserContextByUsername;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;

import java.util.TimeZone;

@AllArgsConstructor
public abstract class ContextFacade {

    private SqlContext sqlContext;
    private TimeInfo timeInfo;

    protected Context authenticate(Authentication authentication) {
        return new ContextByUsername(sqlContext, authentication).execute();
    }

    protected User legacyAuthenticate(Authentication authentication) {
        return new UserByUsername(sqlContext, authentication).execute();
    }

    protected UserContext legacyAuthenticate(Authentication authentication, TimeZone timeZone) {
        return new UserContextByUsername(sqlContext, authentication, timeZone, timeInfo).execute();
    }

}
