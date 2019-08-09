package com.altona.user.service.query;

import com.altona.context.SqlContext;
import com.altona.service.time.util.TimeInfo;
import com.altona.user.service.User;
import com.altona.user.service.UserContext;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.security.core.Authentication;

import java.util.TimeZone;

@AllArgsConstructor
public class UserContextByUsername {

    @NonNull
    private SqlContext sqlContext;

    @NonNull
    private Authentication authentication;

    @NonNull
    private TimeZone timeZone;

    @NonNull
    private TimeInfo timeInfo;

    public UserContext execute() {
        User user = new UserByUsername(sqlContext, authentication.getName()).execute();
        return new UserContext(user, authentication, timeZone, timeInfo);
    }


}
