package com.konkerlabs.platform.registry.business.services.api;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.Transformation;

import java.util.List;

public interface TransformationService {

    enum Validations {
        TRANSFORMATION_NAME_IN_USE("service.transformation.name.in_use"),
        TRANSFORMATION_NOT_FOUND("service.transformation.not_found"),
        TRANSFORMATION_HAS_ROUTE("service.transformation.has.route"),
        TRANSFORMATION_BELONG_ANOTHER_TENANT("service.transformation.belong.another.tenant"),
        TRANSFORMATION_BELONG_ANOTHER_APPLICATION("service.transformation.belong.another.application");

        private String code;

        public String getCode() {
            return code;
        }

        Validations(String code) {
            this.code = code;
        }
    }

    ServiceResponse<List<Transformation>> getAll(Tenant tenant, Application application);

    ServiceResponse<Transformation> register(Tenant tenant, Application application, Transformation transformation);

    ServiceResponse<Transformation> get(Tenant tenant, Application application, String id);

    ServiceResponse<Transformation> update(Tenant tenant, Application application, String id, Transformation transformation);

	ServiceResponse<Transformation> remove(Tenant tenant, Application application, String id);
}
