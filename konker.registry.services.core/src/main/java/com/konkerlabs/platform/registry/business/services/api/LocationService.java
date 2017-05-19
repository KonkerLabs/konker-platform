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
		LOCATION_PARENT_NULL("service.location.parent_null"),
		LOCATION_PARENT_NOT_FOUND("service.location.parent_not_found"),
		LOCATION_HAVE_DEVICES("service.location.have_devices"),
		LOCATION_IS_ROOT("service.location.is_root"),
		LOCATION_HAVE_DEVICE_CONFIGS("service.location.have_device_configs"),
		LOCATION_MULTIPLE_DEFAULTS("service.location.multiple_defaults")
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
        LOCATION_NOT_FOUND("service.location.not_found"),
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

    ServiceResponse<Location> save(Tenant tenant, Application application, Location location);
	ServiceResponse<Location> update(Tenant tenant, Application application, String guid, Location location);
    ServiceResponse<Location> updateSubtree(Tenant tenant, Application application, String guid, List<Location> sublocations);
    ServiceResponse<Location> remove(Tenant tenant, Application application, String guid);

}