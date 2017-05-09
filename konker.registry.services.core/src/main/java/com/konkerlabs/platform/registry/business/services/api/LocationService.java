package com.konkerlabs.platform.registry.business.services.api;

import java.util.List;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Location;
import com.konkerlabs.platform.registry.business.model.Tenant;

public interface LocationService {

	enum Validations {
	    LOCATION_GUID_NULL("service.location.guid_null"),
	    LOCATION_GUID_DOES_NOT_EXIST("service.location.guid_does_not_exist"),
		LOCATION_NAME_ALREADY_REGISTERED("service.location.name_already_registered"),
		LOCATION_ID_DOES_NOT_EXIST("service.location.name_does_not_exist"),
		LOCATION_HAVE_DEVICES("service.location.have_devices")
		;

		public String getCode() {
			return code;
		}

		private String code;

		Validations(String code) {
			this.code = code;
		}
	}

    enum Messages {
        LOCATION_REGISTERED_SUCCESSFULLY("service.location.registered_success"),
        LOCATION_REMOVED_SUCCESSFULLY("service.location.removed_succesfully"),
        LOCATION_REMOVED_UNSUCCESSFULLY("service.location.removed_unsuccesfully"),
        LOCATION_NOT_FOUND("service.location.not_found"),
        LOCATION_DEFAULT_NOT_FOUND("service.location.default_not_found"),
        LOCATION_ROOT_NOT_FOUND("service.location.root_not_found")
        ;

        public String getCode() {
            return code;
        }

        private String code;

        Messages(String code) {
            this.code = code;
        }

    }

    ServiceResponse<Location> findDefault(Tenant tenant, Application application);
    ServiceResponse<Location> findRoot(Tenant tenant, Application application);
    ServiceResponse<Location> findByName(Tenant tenant, Application application, String locationName);
	ServiceResponse<Location> save(Tenant tenant, Application application, Location location);
	ServiceResponse<Location> update(Tenant tenant, Application application, String guid, Location location);
	ServiceResponse<Location> remove(Tenant tenant, Application application, String guid);
	ServiceResponse<List<Location>> findAll(Tenant tenant, Application application);
    ServiceResponse<Location> findTree(Tenant tenant, Application application);

}