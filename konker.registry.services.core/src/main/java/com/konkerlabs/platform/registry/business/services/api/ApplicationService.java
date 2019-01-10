package com.konkerlabs.platform.registry.business.services.api;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Tenant;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ApplicationService {

	String DEFAULT_APPLICATION_ALIAS = "default";
	
	
	enum Validations {
		APPLICATION_NULL("service.application.null"),
		APPLICATION_ALREADY_REGISTERED("service.application.already.registered"),
		APPLICATION_DOES_NOT_EXIST("service.application.does.not.exist"),
		APPLICATION_NAME_IS_NULL("service.application.name.null"),
		APPLICATION_NOT_FOUND("service.application.not_found"),
		APPLICATION_HAS_DEVICE("service.application.has.device"),
		APPLICATION_HAS_ROUTE("service.application.has.route"),
		APPLICATION_HAS_TRANSFORMATION("service.application.has.transformation"),
		APPLICATION_HAS_REST_DESTINATION("service.application.has.rest.destination"),
		APPLICATION_HAS_NO_PERMISSION("service.application.has.no.permission"),
		GATEWAY_NULL("service.gateway.null");

		public String getCode() {
			return code;
		}

		private String code;

		Validations(String code) {
			this.code = code;
		}
	}
	
	enum Messages {
		APPLICATION_REMOVED_SUCCESSFULLY("controller.application.removed.succesfully");

		public String getCode() {
			return code;
		}

		private String code;

		Messages(String code) {
			this.code = code;
		}
	}

	boolean isDefaultApplication(Application application, Tenant tenant);
	
   	/**
	 * Persists a new Application.
	 *
	 * If the application name already exists, an error is created. The
	 * tenant must exist.
	 *
	 * @param tenant
	 * @param application
	 * @return
	 */
	ServiceResponse<Application> register(Tenant tenant, Application application);

	/**
	 * Updates an already existent Tenant.
	 *
	 * If the applicationName does not exist in this tenant, an error is created. The
	 * tenant must exist.
	 *
	 * @param tenant
	 * @param application
	 * @return
	 */
	ServiceResponse<Application> update(Tenant tenant, String name, Application application);

	/**
	 * Removes the application that exists in this tenant.
	 *
	 * @param name
	 * @return ServiceResponse<Application>
	 */
	ServiceResponse<Application> remove(Tenant tenant, String name);

	/**
	 * Returns all applications owned by the provided tenant.
	 *
	 *
	 * @param tenant
	 * @return
	 */
	ServiceResponse<List<Application>> findAll(Tenant tenant);

	/**
	 * Returns all applications owned by the provided tenant paginated.
	 *
	 *
	 * @param tenant
	 * @param page
	 * @param size
	 * @return paginated list of application
	 */
	ServiceResponse<Page<Application>> findAll(Tenant tenant, int page, int size);


	/**
	 * Returns a application by its applicationName and tenant.
	 *
	 * If the application does not exist, returns an error
	 *
	 * @param tenant
	 * @param name
	 * @return
	 */
	ServiceResponse<Application> getByApplicationName(Tenant tenant, String name);


}