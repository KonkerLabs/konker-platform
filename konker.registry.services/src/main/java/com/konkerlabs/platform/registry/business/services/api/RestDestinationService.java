package com.konkerlabs.platform.registry.business.services.api;

import com.konkerlabs.platform.registry.business.model.RestDestination;
import com.konkerlabs.platform.registry.business.model.Tenant;

import java.util.List;

public interface RestDestinationService {

	enum Validations {
		GUID_NULL("service.rest_destination.guid.not_null"), 
		DESTINATION_NOT_FOUND("service.rest_destination.not_found"), 
		NAME_IN_USE("service.rest_destination.name.in_use"),
		REST_DESTINATION_IN_USE_ROUTE("service.rest_destination.in_use_route");

		private String code;

		public String getCode() {
			return code;
		}

		Validations(String code) {
			this.code = code;
		}
	}
	
    enum Messages {
    	REST_DESTINATION_REMOVED_SUCCESSFULLY("service.rest_destination.removed_succesfully");

        public String getCode() {
            return code;
        }

        private String code;

        Messages(String code) {
            this.code = code;
        }
    }


	ServiceResponse<List<RestDestination>> findAll(Tenant tenant);

	ServiceResponse<RestDestination> register(Tenant tenant, RestDestination destination);

	ServiceResponse<RestDestination> update(Tenant tenant, String guid, RestDestination destination);

	ServiceResponse<RestDestination> getByGUID(Tenant tenant, String guid);
	// TODO: will we need this? URI encapsulation for destinations should have
	// taken care of this by now
	// ServiceResponse<RestDestination> getByUri(Tenant tenant, URI restUri);

	ServiceResponse<RestDestination> remove(Tenant tenant, String guid);

}
