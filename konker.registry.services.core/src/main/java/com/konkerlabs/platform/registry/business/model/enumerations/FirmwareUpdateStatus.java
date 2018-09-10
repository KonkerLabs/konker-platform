package com.konkerlabs.platform.registry.business.model.enumerations;

public enum FirmwareUpdateStatus {

    UNKNOWN("firmware.unknown", "UNKNOWN"),
	PENDING("firmware.pending", "PENDING"),
	UPDATED("firmware.updated", "UPDATED");

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
