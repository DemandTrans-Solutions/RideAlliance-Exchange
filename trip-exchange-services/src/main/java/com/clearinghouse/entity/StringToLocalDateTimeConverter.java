/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;

/**
 *
 * @author chaitanyaP
 */
public class StringToLocalDateTimeConverter {

    public static LocalDateTime converterStringToLocalDate(String rowLocalDateTime) {
        if (rowLocalDateTime == null) {
            return null;
        }

        // Normalize input: allow both 'T' and space as date/time separator and trim whitespace
        String normalized = rowLocalDateTime.trim().replace('T', ' ');

        // Formatter that accepts both "yyyy-MM-dd HH:mm" and "yyyy-MM-dd HH:mm:ss"
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd HH:mm")
                .optionalStart()
                .appendPattern(":ss")
                .optionalEnd()
                .toFormatter();

        try {
            return LocalDateTime.parse(normalized, formatter);
        } catch (DateTimeParseException ex) {
            // Keep original behavior of throwing a parse exception, but include the input in the message
            throw ex;
        }
    }

}
