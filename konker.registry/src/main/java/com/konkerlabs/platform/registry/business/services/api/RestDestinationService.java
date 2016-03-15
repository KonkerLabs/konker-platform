package com.konkerlabs.platform.registry.business.services.api;

import com.konkerlabs.platform.registry.business.model.RestDestination;
import com.konkerlabs.platform.registry.business.model.Tenant;

import java.util.List;

public interface RestDestinationService {

    ServiceResponse<List<RestDestination>> findAll(Tenant tenant);

    ServiceResponse<RestDestination> register(Tenant tenant, RestDestination destination);

    ServiceResponse<RestDestination> update(Tenant tenant, String id, RestDestination destination);

    ServiceResponse<RestDestination> getByID(Tenant tenant, String restId);
    // TODO: will we need this?
    // ServiceResponse<RestDestination> getByUri(Tenant tenant, URI restUri);
}
