package com.konkerlabs.platform.registry.business.model.validation;

public enum CommonValidations {

    GENERIC_ERROR("common.generic_message"),
    TENANT_NULL("model.tenant.not_null"),
    TENANT_DOES_NOT_EXIST("service.tenant.does_not_exist"),
    RECORD_NULL("service.device.record.not_null"), 
    URL_MATCHES_BLACKLIST("service.url.matches_blacklist");

    private String code;

    CommonValidations(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
