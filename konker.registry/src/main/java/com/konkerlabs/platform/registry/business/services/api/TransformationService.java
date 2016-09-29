package com.konkerlabs.platform.registry.business.services.api;

import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.Transformation;

import java.util.List;

public interface TransformationService {

    enum Validations {
        TRANSFORMATION_NAME_IN_USE("service.transformation.name.in_use"),
        TRANSFORMATION_NOT_FOUND("service.transformation.not_found"),
        TRANSFORMATION_HAS_ROUTE("service.transformation.has.route"),
        TRANSFORMATION_BELONG_ANOTHER_TENANT("service.transformation.belong.another.tenant");

        private String code;

        public String getCode() {
            return code;
        }

        Validations(String code) {
            this.code = code;
        }
    }

    ServiceResponse<List<Transformation>> getAll(Tenant tenant);

    ServiceResponse<Transformation> register(Tenant tenant, Transformation transformation);

    ServiceResponse<Transformation> get(Tenant tenant, String id);

    ServiceResponse<Transformation> update(Tenant tenant, String id, Transformation transformation);

	ServiceResponse<Transformation> remove(Tenant tenant, String id);
}
