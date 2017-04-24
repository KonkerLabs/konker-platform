package com.konkerlabs.platform.registry.business.services.api;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.RestDestination;
import com.konkerlabs.platform.registry.business.model.Tenant;

import java.util.List;

public interface RestDestinationService {

	enum Validations {
		GUID_NULL("service.rest_destination.guid.not_null"),
		DESTINATION_NOT_FOUND("service.rest_destination.not_found"),
		NAME_IN_USE("service.rest_destination.name.in_use"),
		REST_DESTINATION_IN_USE_ROUTE("service.rest_destination.in_use_route"),
		METHOD_INVALID("service.rest_destination.method.invalid");

		private String code;

		public String getCode() {
			return code;
		}

		Validations(String code) {
			this.code = code;
		}
	}

    enum Messages {
    	REST_DESTINATION_REMOVED_SUCCESSFULLY("service.rest_destination.removed_succesfully"),
    	REST_DESTINATION_REMOVED_UNSUCCESSFULLY("service.rest_destination.removed_unsuccesfully");

        public String getCode() {
            return code;
        }

        private String code;

        Messages(String code) {
            this.code = code;
        }
    }

	ServiceResponse<List<RestDestination>> findAll(Tenant tenant, Application application);
	ServiceResponse<RestDestination> register(Tenant tenant, Application application, RestDestination destination);
	ServiceResponse<RestDestination> update(Tenant tenant, Application application, String guid, RestDestination destination);
	ServiceResponse<RestDestination> getByGUID(Tenant tenant, Application application, String guid);
	ServiceResponse<RestDestination> remove(Tenant tenant, Application application, String guid);

}
