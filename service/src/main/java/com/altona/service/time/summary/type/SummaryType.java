package com.altona.service.time.summary.type;

import com.altona.service.time.TimeConfig;
import com.altona.service.time.summary.SummaryConfiguration;
import com.altona.service.time.summary.TimeRounding;

public enum SummaryType {

    CURRENT_DAY {
        @Override
        public SummaryConfiguration getConfiguration(TimeConfig timeConfig) {
            return new SummaryConfiguration(timeConfig.today(), timeConfig.today(), TimeRounding.NONE);
        }
    },
    CURRENT_WEEK {
        @Override
        public SummaryConfiguration getConfiguration(TimeConfig timeConfig) {
            return new SummaryConfiguration(timeConfig.firstDayOfWeek(), timeConfig.today(), TimeRounding.NONE);
        }
    };

    public abstract SummaryConfiguration getConfiguration(TimeConfig timeConfig);
}
