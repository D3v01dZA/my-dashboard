package com.altona.project.time;

import com.altona.project.time.TimeUtil;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class LocalDateIteratorTest {

    @Test
    public void testInvalid() {
        assertThrows(IllegalArgumentException.class, () -> TimeUtil.LocalDateIterator.inclusive(LocalDate.of(2018, 1, 1), LocalDate.of(2017, 1, 1)).iterator());
    }

    @Test
    public void testInclusive() {
        Iterable<LocalDate> iterable = TimeUtil.LocalDateIterator.inclusive(LocalDate.of(2018, 1, 1), LocalDate.of(2018, 1, 3));

        Iterator<LocalDate> firstIterator = iterable.iterator();
        assertTrue(firstIterator.hasNext());
        assertEquals(LocalDate.of(2018, 1, 1), firstIterator.next());
        assertTrue(firstIterator.hasNext());
        assertEquals(LocalDate.of(2018, 1, 2), firstIterator.next());
        assertTrue(firstIterator.hasNext());
        assertEquals(LocalDate.of(2018, 1, 3), firstIterator.next());
        assertFalse(firstIterator.hasNext());
        assertThrows(NoSuchElementException.class, firstIterator::next);

        Iterator<LocalDate> secondIterator = iterable.iterator();
        assertTrue(secondIterator.hasNext());
        assertEquals(LocalDate.of(2018, 1, 1), secondIterator.next());
        assertTrue(secondIterator.hasNext());
        assertEquals(LocalDate.of(2018, 1, 2), secondIterator.next());
        assertTrue(secondIterator.hasNext());
        assertEquals(LocalDate.of(2018, 1, 3), secondIterator.next());
        assertFalse(secondIterator.hasNext());
        assertThrows(NoSuchElementException.class, secondIterator::next);
    }

}