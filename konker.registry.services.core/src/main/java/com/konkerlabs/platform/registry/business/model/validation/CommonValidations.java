package com.konkerlabs.platform.registry.business.model.validation;

public enum CommonValidations {

    GENERIC_ERROR("common.generic_message"),
    TENANT_NULL("model.tenant.not_null"),
    TENANT_DOES_NOT_EXIST("service.tenant.does_not_exist"),
    RECORD_NULL("service.device.record.not_null"),
    URL_MATCHES_BLACKLIST("service.url.matches_blacklist"),
    SIZE_ELEMENT_PAGE_INVALID("service.size.element.page.invalid"),
    SIZE_ELEMENT_PAGE_OVERPASS("service.size.element.page.ovepass"),
    SORT_INVALID("service.search.sort.invalid");

    private String code;

    CommonValidations(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
