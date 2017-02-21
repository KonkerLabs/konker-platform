package com.konkerlabs.platform.registry.business.model.enumerations;

public enum LogLevel {
	
	ALL("loglevels.all", "ALL"),
	WARNING("loglevels.warning", "WARN"),
	INFO("loglevels.info", "INFO"),
	DISABLED("loglevels.disabled", "DISABLED");

    private String code;
    private String id;

    LogLevel(String code, String id) {
        this.code = code;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

}
