package com.konkerlabs.platform.registry.business.services.api;

import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.Transformation;

import java.util.List;

public interface TransformationService {


    ServiceResponse<List<Transformation>> getAll(Tenant tenant);

    ServiceResponse<Transformation> save(Tenant tenant, Transformation transformation);
}
