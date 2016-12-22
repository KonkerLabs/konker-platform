package com.konkerlabs.platform.registry.business.model.enumerations;

public enum TimeZone {
    AMERICA_SAO_PAULO("timezones.america_sao_paulo", "America/Sao_Paulo"),
    AMERICA_LOS_ANGELES("timezones.america_los_angeles", "America/Los_Angeles");

    private String code;
    private String id;

    TimeZone(String code, String id){
        this.code = code;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    TimeZone(String code) {
        this.code = code;
    }

    public String getCode(){
        return code;
    }
}
