package com.konkerlabs.platform.registry.business.model.enumerations;

public enum DateFormat {
    DDMMYYYY("dateformats.ddmmyyyy", "dd/MM/yyyy"),
    YYYYMMDD("dateformats.yyyymmdd", "yyyy/MM/dd"),
    MMDDYYYY("dateformats.mmddyyyy", "MM/dd/yyyy");

    private String code;
    private String id;

    DateFormat(String code, String id) {
        this.code = code;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getCode(){
        return code;
    }
}
