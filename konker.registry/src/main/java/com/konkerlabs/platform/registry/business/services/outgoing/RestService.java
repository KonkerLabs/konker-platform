package com.konkerlabs.platform.registry.business.services.outgoing;


import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.destinations.RestDestination;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;

import java.net.URI;
import java.util.List;

public interface RestService {

    ServiceResponse<List<RestDestination>> getAll(Tenant tenant);
    ServiceResponse<RestDestination> get(Tenant tenant, String restId);
    ServiceResponse<RestDestination> getByUri(Tenant tenant, URI restUri);
    ServiceResponse<RestDestination> save(Tenant tenant, RestDestination restDestination);
}
