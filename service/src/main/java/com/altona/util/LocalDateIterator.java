package com.altona.util;

import org.springframework.util.Assert;

import java.time.LocalDate;
import java.util.Iterator;

public class LocalDateIterator implements Iterator<LocalDate> {

    public static Iterable<LocalDate> inclusive(LocalDate from, LocalDate to) {
        return () -> new LocalDateIterator(from, to);
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
        LocalDate current = this.current;
        this.current = this.current.plusDays(1);
        return current;
    }
}
