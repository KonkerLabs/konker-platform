package com.konkerlabs.platform.registry.api.model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.konkerlabs.platform.registry.api.exceptions.BadRequestResponseException;

import lombok.Data;

@Data
public class EventsFilter {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private String deviceGuid;
    private String locationGuid;
    private String channel;
    private Instant startingTimestamp;
    private Instant endTimestamp;

    public void parse(String query) throws BadRequestResponseException {

        this.deviceGuid = null;
        this.locationGuid = null;
        this.channel = null;
        this.startingTimestamp = null;
        this.endTimestamp = null;

        if (!StringUtils.hasText(query)) {
            return;
        }

        String filters[] = query.split(" ");

        for (String filter : filters) {
            parseToken(filter);
        }

    }

    private void parseToken(String filter) throws BadRequestResponseException {

        String tokens[] = filter.split(":", 2);

        if (tokens.length != 2) {
            throw new BadRequestResponseException("Invalid filter: " + filter);
        }

        if (tokens[0].equalsIgnoreCase("device")) {
            this.deviceGuid = tokens[1];
        } else if (tokens[0].equalsIgnoreCase("location")) {
            this.locationGuid = tokens[1];
        } else if (tokens[0].equalsIgnoreCase("channel")) {
            this.channel = tokens[1];
        } else if (tokens[0].equalsIgnoreCase("timestamp")) {
            if (tokens[1].contains(">")) {
                startingTimestamp = parseIntant(removeInvalidChars(tokens[1]));
            } else if (tokens[1].contains("<")) {
                endTimestamp = parseIntant(removeInvalidChars(tokens[1]));
            }
        } else {
            throw new BadRequestResponseException("Not supported filter: " + tokens[0]);
        }

    }

    private String removeInvalidChars(String text) {
        return text.replaceAll("[<>\\\"']", "").trim();
    }

    private Instant parseIntant(String text) throws BadRequestResponseException {

        // 2007-12-03T10:15:30+01:00
        Instant instant = parseISOOffsetDateTime(text);
        if (instant != null) {
            return instant;
        }

        // 2007-12-03T10:15:30
        instant = parseISODateTime(text);
        if (instant != null) {
            return instant;
        }

        // 2007-12-03+01:00
        instant = parseISOOffsetDate(text);
        if (instant != null) {
            return instant;
        }

        // 2007-12-03
        instant = parseISODate(text);
        if (instant != null) {
            return instant;
        }

        throw new BadRequestResponseException("Not ISO Date: " + text);
    }

    private Instant parseISOOffsetDateTime(String text) {

        try {
            return OffsetDateTime.parse(text).toInstant();
        } catch (DateTimeParseException e) {
            return null;
        }

    }

    private Instant parseISODateTime(String text) {

        try {

            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            LocalDateTime datetime = LocalDateTime.parse(text, formatter);
            ZonedDateTime dt = datetime.atZone(ZoneId.of("UTC"));

            return dt.toInstant();

        } catch (DateTimeParseException e) {
            return null;
        }

    }

    private Instant parseISOOffsetDate(String text) {

        try {

            DateTimeFormatter fmt = new DateTimeFormatterBuilder().append(DateTimeFormatter.ISO_OFFSET_DATE)
                    .parseDefaulting(ChronoField.HOUR_OF_DAY, 0L).toFormatter();
            return OffsetDateTime.parse(text, fmt).toInstant();

        } catch (DateTimeParseException e) {
            return null;
        }

    }

    private Instant parseISODate(String text) {

        try {

            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;
            LocalDateTime datetime = LocalDate.parse(text, formatter).atStartOfDay();
            ZonedDateTime dt = datetime.atZone(ZoneId.of("UTC"));

            return dt.toInstant();

        } catch (DateTimeParseException e) {
            return null;
        }

    }

}
