package com.altona.project.time.query;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;

interface PostgresTimeReader {

    /**
     * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     * Exists because dates and date formats are so bad
     * Also this will quite definitely break if the db changes
     * Reason this exists:
     *      App Inserts Date        2019-01-26 18:45:08.460-05
     *      DB Saves Date           2019-01-26 18:45:08.46-05
     *      App Retrieves Date      2019-01-26 18:45:08.046-05
     * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     */
    int MINIMUM_LENGTH = 22;
    int EXPECTED_LENGTH = 26;

    static Instant padMissingZeroMillis(String date) {
        if (date == null) {
            return null;
        }
        try {
            char padding = '0';
            if (date.length() < MINIMUM_LENGTH) {
                throw new IllegalStateException("Date " + date + " is too short");
            } else if (date.length() == MINIMUM_LENGTH) {
                padding = '.';
            }
            while (date.length() != EXPECTED_LENGTH) {
                // Offset by timezone
                int padLoc = (EXPECTED_LENGTH - (EXPECTED_LENGTH - date.length())) - 3;
                String lhs = date.substring(0, padLoc);
                String rhs = date.substring(padLoc);
                date = lhs + padding + rhs;
                padding = '0';
            }
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSX").parse(date).toInstant();
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }
    }

}
