package com.konkerlabs.platform.registry.business.model.enumerations;

public enum FirmwareUpdateStatus {

    UNKNOWN("firmware.unknown", "UNKNOWN"),
	PENDING("firmware.pending", "PENDING"),
    UPDATING("firmware.updating", "UPDATING"),
	UPDATED("firmware.updated", "UPDATED"),
    SUSPENDED("firmware.suspended", "SUSPENDED");

    private String code;
    private String id;

    FirmwareUpdateStatus(String code, String id) {
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
