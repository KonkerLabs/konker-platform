package com.konkerlabs.platform.registry.business.services.api;

import com.konkerlabs.platform.registry.business.model.RestDestination;
import com.konkerlabs.platform.registry.business.model.Tenant;

import java.util.List;

public interface RestDestinationService {

    ServiceResponse<List<RestDestination>> findAll(Tenant tenant);

    ServiceResponse<RestDestination> register(Tenant tenant, RestDestination destination);

    ServiceResponse<RestDestination> update(Tenant tenant, String guid, RestDestination destination);

    ServiceResponse<RestDestination> getByGUID(Tenant tenant, String guid);
    // TODO: will we need this? URI encapsulation for destinations should have taken care of this by now
    // ServiceResponse<RestDestination> getByUri(Tenant tenant, URI restUri);
}
