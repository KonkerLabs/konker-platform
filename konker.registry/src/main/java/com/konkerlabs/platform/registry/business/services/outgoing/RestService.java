package com.konkerlabs.platform.registry.business.services.outgoing;


import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.outgoing.Rest;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;

import java.net.URI;
import java.util.List;

public interface RestService {

    ServiceResponse<List<Rest>> getAll(Tenant tenant);
    ServiceResponse<Rest> get(Tenant tenant, String restId);
    ServiceResponse<Rest> getByUri(Tenant tenant, URI restUri);
    ServiceResponse<Rest> save(Tenant tenant, Rest rest);
}
