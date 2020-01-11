package com.altona.project.time;

import org.springframework.util.Assert;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.NoSuchElementException;

public interface TimeUtil {

    LocalTime ZERO = LocalTime.of(0, 0);

    static LocalTime difference(Instant from, Instant to) {
        Assert.isTrue(from.isBefore(to), () -> String.format("%s was before %s", to, from));
        return LocalTime.ofNanoOfDay(ChronoUnit.NANOS.between(from, to));
    }

    class LocalDateIterator implements Iterator<LocalDate> {

        public static Iterable<LocalDate> inclusive(LocalDate from, LocalDate to) {
            return () -> new LocalDateIterator(from, to);
        }

        public static Iterable<LocalDate> exclusive(LocalDate from, LocalDate to) {
            return () -> new LocalDateIterator(from, to.minusDays(1));
        }

        private LocalDate current;
        private LocalDate to;

        private LocalDateIterator(LocalDate from, LocalDate to) {
            Assert.isTrue(from.isBefore(to) || from.isEqual(to), "Date " + from + " was not before " + to);
            this.current = from;
            this.to = to;
        }

        @Override
        public boolean hasNext() {
            return !current.isAfter(to);
        }

        @Override
        public LocalDate next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            LocalDate current = this.current;
            this.current = this.current.plusDays(1);
            return current;
        }
    }
}
