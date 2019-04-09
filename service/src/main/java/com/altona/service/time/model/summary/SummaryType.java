package com.altona.service.time.model.summary;

import com.altona.security.UserContext;

import java.util.Date;

public enum SummaryType {

    CURRENT_DAY {
        @Override
        public SummaryConfiguration getConfiguration(UserContext userContext) {
            return new SummaryConfiguration(
                    userContext.localize(new Date()),
                    userContext.today(),
                    userContext.today().plusDays(1),
                    TimeRounding.NONE,
                    NotStoppedAction.INCLUDE,
                    true
            );
        }
    },
    CURRENT_WEEK {
        @Override
        public SummaryConfiguration getConfiguration(UserContext userContext) {
            return new SummaryConfiguration(
                    userContext.localize(new Date()),
                    userContext.firstDayOfWeek(),
                    userContext.lastDayOfWeek().plusDays(1),
                    TimeRounding.NONE,
                    NotStoppedAction.INCLUDE,
                    true
            );
        }
    },
    CURRENT_MONTH {
        @Override
        public SummaryConfiguration getConfiguration(UserContext userContext) {
            return new SummaryConfiguration(
                    userContext.localize(new Date()),
                    userContext.firstDayOfMonth(),
                    userContext.lastDayOfMonth().plusDays(1),
                    TimeRounding.NONE,
                    NotStoppedAction.INCLUDE,
                    true
            );
        }
    };

    public abstract SummaryConfiguration getConfiguration(UserContext userContext);
}
