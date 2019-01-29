package com.altona.service.time.summary.type;

import com.altona.security.UserContext;
import com.altona.service.time.summary.SummaryConfiguration;
import com.altona.service.time.summary.TimeRounding;
import com.altona.service.time.summary.NotStoppedAction;

import java.util.Date;

public enum SummaryType {

    CURRENT_DAY {
        @Override
        public SummaryConfiguration getConfiguration(UserContext userContext) {
            return new SummaryConfiguration(userContext.localize(new Date()), userContext.today(), userContext.today().plusDays(1), TimeRounding.NONE, NotStoppedAction.EXCLUDE);
        }
    },
    CURRENT_WEEK {
        @Override
        public SummaryConfiguration getConfiguration(UserContext userContext) {
            return new SummaryConfiguration(userContext.localize(new Date()), userContext.firstDayOfWeek(), userContext.today().plusDays(1), TimeRounding.NONE, NotStoppedAction.EXCLUDE);
        }
    },
    DAY {
        @Override
        public SummaryConfiguration getConfiguration(UserContext userContext) {
            return new SummaryConfiguration(userContext.localize(new Date()), userContext.today(), userContext.today().plusDays(1), TimeRounding.NONE, NotStoppedAction.EXCLUDE);
        }
    },
    WEEK {
        @Override
        public SummaryConfiguration getConfiguration(UserContext userContext) {
            return new SummaryConfiguration(userContext.localize(new Date()), userContext.today().minusDays(6), userContext.today().plusDays(1), TimeRounding.NONE, NotStoppedAction.EXCLUDE);
        }
    };

    public abstract SummaryConfiguration getConfiguration(UserContext userContext);
}
