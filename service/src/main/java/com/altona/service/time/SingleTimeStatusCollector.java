package com.altona.service.time;

import com.altona.service.time.control.TimeStatus;

import java.util.Optional;
import java.util.stream.Collector;

public interface SingleTimeStatusCollector extends Collector<TimeStatus, SingleTimeStatusCollector.Holder, Optional<TimeStatus>> {

    Collector<TimeStatus, SingleTimeStatusCollector.Holder, Optional<TimeStatus>> INSTANCE = Collector.of(
            Holder::new,
            (holder, status) -> {
                if (status.isTimeRunning()) {
                    if (holder.timeStatus != null) {
                        throw new IllegalStateException("Can't run time on multiple projects simultaneously");
                    }
                    holder.timeStatus = status;
                }
            },
            (optionalOne, optionalTwo) -> {
                throw new IllegalStateException("Don't parallel collect me bro");
            },
            holder -> Optional.ofNullable(holder.timeStatus)
    );

    class Holder {

        TimeStatus timeStatus;

    }

}
