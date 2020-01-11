package com.altona.project.time;

import com.altona.context.EncryptionContext;
import com.altona.project.Project;
import com.altona.project.time.query.NotStoppedAction;
import com.altona.project.time.query.TimeSelection;
import com.altona.project.time.query.TimeRounding;

import java.time.temporal.TemporalAdjusters;

public enum SummaryType {

    CURRENT_DAY {
        @Override
        public TimeSelection createSelection(EncryptionContext encryptionContext, Project project) {
            return new TimeSelection(
                    encryptionContext,
                    project,
                    encryptionContext.today(),
                    encryptionContext.today().plusDays(1),
                    TimeRounding.NONE,
                    NotStoppedAction.INCLUDE,
                    true
            );
        }
    },
    CURRENT_WEEK {
        @Override
        public TimeSelection createSelection(EncryptionContext encryptionContext, Project project) {
            return new TimeSelection(
                    encryptionContext,
                    project,
                    encryptionContext.firstDayOfWeek(),
                    encryptionContext.lastDayOfWeek().plusDays(1),
                    TimeRounding.NONE,
                    NotStoppedAction.INCLUDE,
                    true
            );
        }
    },
    CURRENT_MONTH {
        @Override
        public TimeSelection createSelection(EncryptionContext encryptionContext, Project project) {
            return new TimeSelection(
                    encryptionContext,
                    project,
                    encryptionContext.firstDayOfMonth(),
                    encryptionContext.lastDayOfMonth().plusDays(1),
                    TimeRounding.NONE,
                    NotStoppedAction.INCLUDE,
                    true
            );
        }
    },
    PREVIOUS_MONTH {
        @Override
        public TimeSelection createSelection(EncryptionContext encryptionContext, Project project) {
            return new TimeSelection(
                    encryptionContext,
                    project,
                    encryptionContext.firstDayOfMonth().minusMonths(1).with(TemporalAdjusters.firstDayOfMonth()),
                    encryptionContext.lastDayOfMonth().minusMonths(1).with(TemporalAdjusters.lastDayOfMonth()).plusDays(1),
                    TimeRounding.NONE,
                    NotStoppedAction.INCLUDE,
                    true
            );
        }
    };

    public abstract TimeSelection createSelection(EncryptionContext encryptionContext, Project project);
}
