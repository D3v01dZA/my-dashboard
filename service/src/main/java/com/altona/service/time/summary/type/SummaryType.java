package com.altona.service.time.summary.type;

import com.altona.service.time.summary.SummaryConfiguration;
import com.altona.service.time.summary.TimeRounding;

import java.time.LocalDate;

public enum SummaryType {
    DAY {
        @Override
        public SummaryConfiguration getConfiguration() {
            LocalDate now = LocalDate.now();
            return new SummaryConfiguration(now, now, TimeRounding.NONE);
        }
    },
    WEEK {
        @Override
        public SummaryConfiguration getConfiguration() {
            LocalDate now = LocalDate.now();
            return new SummaryConfiguration(now.minusDays(7), now, TimeRounding.NONE);
        }
    };

    public abstract SummaryConfiguration getConfiguration();
}
