package com.altona.service.time.model.summary;

import java.time.LocalTime;

public interface TimeRounding {

    TimeRounding NONE = Implementations.NONE;

    TimeRounding NEAREST_FIFTEEN = Implementations.NEAREST_FIFTEEN;

    LocalTime round(LocalTime time);

    enum Implementations implements TimeRounding {
        NONE {
            @Override
            public LocalTime round(LocalTime time) {
                return time;
            }
        },
        NEAREST_FIFTEEN {
            @Override
            public LocalTime round(LocalTime time) {
                // I wasn't really awake while doing this but I think this works even though its not some cool math formula
                int minute = time.getMinute() % 15;
                int nearest;
                if (minute < 7) {
                    nearest = 0;
                } else if (minute == 7) {
                    int seconds = time.getSecond();
                    if (seconds < 30) {
                        nearest = 0;
                    } else {
                        nearest = 15;
                    }
                } else {
                    nearest = 15;
                }
                int nearestMinute = time.getMinute() - minute + nearest;
                if (nearestMinute == 60) {
                    return LocalTime.of(time.getHour() + 1, 0, 0, 0);
                }
                return LocalTime.of(time.getHour(), nearestMinute, 0, 0);
            }
        }
    }

}
