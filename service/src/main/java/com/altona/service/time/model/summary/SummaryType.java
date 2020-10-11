package com.altona.service.time.model.summary;

import com.altona.user.service.UserContext;

import java.time.temporal.TemporalAdjusters;

public enum SummaryType {

    CURRENT_DAY {
        @Override
        public SummaryConfiguration getConfiguration(UserContext userContext) {
            return new SummaryConfiguration(
                    userContext.localizedNow(),
                    userContext.today(),
                    userContext.today().plusDays(1),
                    TimeRounding.NONE,
                    NotStoppedAction.INCLUDE,
                    true
            );
        }

        @Override
        public TimeRetrievalConfiguration getTimeRetrievalConfiguration(UserContext userContext) {
            return new TimeRetrievalConfiguration(
                    userContext.today(),
                    userContext.today().plusDays(1)
            );
        }
    },
    CURRENT_WEEK {
        @Override
        public SummaryConfiguration getConfiguration(UserContext userContext) {
            return new SummaryConfiguration(
                    userContext.localizedNow(),
                    userContext.firstDayOfWeek(),
                    userContext.lastDayOfWeek().plusDays(1),
                    TimeRounding.NONE,
                    NotStoppedAction.INCLUDE,
                    true
            );
        }

        @Override
        public TimeRetrievalConfiguration getTimeRetrievalConfiguration(UserContext userContext) {
            return new TimeRetrievalConfiguration(
                    userContext.firstDayOfWeek(),
                    userContext.lastDayOfWeek().plusDays(1)
            );
        }
    },
    CURRENT_MONTH {
        @Override
        public SummaryConfiguration getConfiguration(UserContext userContext) {
            return new SummaryConfiguration(
                    userContext.localizedNow(),
                    userContext.firstDayOfMonth(),
                    userContext.lastDayOfMonth().plusDays(1),
                    TimeRounding.NONE,
                    NotStoppedAction.INCLUDE,
                    true
            );
        }

        @Override
        public TimeRetrievalConfiguration getTimeRetrievalConfiguration(UserContext userContext) {
            return new TimeRetrievalConfiguration(
                    userContext.firstDayOfMonth(),
                    userContext.lastDayOfMonth().plusDays(1)
            );
        }
    },
    PREVIOUS_MONTH {
        @Override
        public SummaryConfiguration getConfiguration(UserContext userContext) {
            return new SummaryConfiguration(
                    userContext.localizedNow(),
                    userContext.firstDayOfMonth().minusMonths(1).with(TemporalAdjusters.firstDayOfMonth()),
                    userContext.lastDayOfMonth().minusMonths(1).with(TemporalAdjusters.lastDayOfMonth()).plusDays(1),
                    TimeRounding.NONE,
                    NotStoppedAction.INCLUDE,
                    true
            );
        }

        @Override
        public TimeRetrievalConfiguration getTimeRetrievalConfiguration(UserContext userContext) {
            return new TimeRetrievalConfiguration(
                    userContext.firstDayOfMonth().minusMonths(1).with(TemporalAdjusters.firstDayOfMonth()),
                    userContext.lastDayOfMonth().minusMonths(1).with(TemporalAdjusters.lastDayOfMonth()).plusDays(1)
            );
        }
    };

    public abstract SummaryConfiguration getConfiguration(UserContext userContext);

    public abstract TimeRetrievalConfiguration getTimeRetrievalConfiguration(UserContext userContext);

}
