package com.konkerlabs.platform.registry.business.model.enumerations;

import java.time.format.DateTimeFormatter;

public enum DateFormat {
    DDMMYYYY("dateformats.ddmmyyyy", "dd/MM/yyyy"), YYYYMMDD("dateformats.yyyymmdd",
            "yyyy/MM/dd"), MMDDYYYY("dateformats.mmddyyyy", "MM/dd/yyyy");

    private static final String DEFAULT_HOUR_FORMAT = "HH:mm:ss";

    private String code;
    private String id;

    DateFormat(String code, String id) {
        this.code = code;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public DateTimeFormatter getDateFormatter() {
        return DateTimeFormatter.ofPattern(this.id);
    }

    public DateTimeFormatter getDateTimeFormatter() {
        return DateTimeFormatter.ofPattern(this.id + " " + DEFAULT_HOUR_FORMAT);
    }

}
