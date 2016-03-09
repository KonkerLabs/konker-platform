package com.konkerlabs.platform.registry.business.services.api;

import com.konkerlabs.platform.registry.business.model.DataEnrichmentExtension;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.Transformation;

import java.util.List;

public interface TransformationService {

    ServiceResponse<List<Transformation>> getAll(Tenant tenant);

    ServiceResponse<Transformation> register(Tenant tenant, Transformation transformation);

    ServiceResponse<Transformation> get(Tenant tenant, String id);

    ServiceResponse<DataEnrichmentExtension> update(Tenant tenant, String id, Transformation transformation);
}
