package com.konkerlabs.platform.registry.business.services.api;

import com.konkerlabs.platform.registry.business.model.RestDestination;
import com.konkerlabs.platform.registry.business.model.Tenant;

import java.util.List;

public interface RestDestinationService {

    enum Validations {
        GUID_NULL("service.rest_destination.guid.not_null"),
        DESTINATION_NOT_FOUND("service.rest_destination.not_found"),
        NAME_IN_USE("service.rest_destination.name.in_use");

        private String code;

        public String getCode() {
            return code;
        }

        Validations(String code) {
            this.code = code;
        }
    }

    NewServiceResponse<List<RestDestination>> findAll(Tenant tenant);

    NewServiceResponse<RestDestination> register(Tenant tenant, RestDestination destination);

    NewServiceResponse<RestDestination> update(Tenant tenant, String guid, RestDestination destination);

    NewServiceResponse<RestDestination> getByGUID(Tenant tenant, String guid);
    // TODO: will we need this? URI encapsulation for destinations should have taken care of this by now
    // ServiceResponse<RestDestination> getByUri(Tenant tenant, URI restUri);
}
