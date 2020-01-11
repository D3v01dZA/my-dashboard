package com.altona.user.query;

import com.altona.context.SqlContext;
import com.altona.context.TimeInfo;
import com.altona.user.User;
import com.altona.user.UserContext;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.security.core.Authentication;

import java.util.TimeZone;

@AllArgsConstructor
public class RequiredUserContextByUsername {

    @NonNull
    private SqlContext sqlContext;

    @NonNull
    private Authentication authentication;

    @NonNull
    private TimeZone timeZone;

    @NonNull
    private TimeInfo timeInfo;

    public UserContext execute() {
        User user = new RequiredUserByUsername(sqlContext, authentication.getName()).execute();
        return new UserContext(user, authentication, timeZone, timeInfo);
    }


}
