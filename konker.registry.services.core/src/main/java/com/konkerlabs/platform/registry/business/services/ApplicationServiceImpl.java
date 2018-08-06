package com.konkerlabs.platform.registry.business.services;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.EventRoute;
import com.konkerlabs.platform.registry.business.model.Location;
import com.konkerlabs.platform.registry.business.model.RestDestination;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.Transformation;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.ApplicationRepository;
import com.konkerlabs.platform.registry.business.repositories.DeviceRepository;
import com.konkerlabs.platform.registry.business.repositories.EventRouteRepository;
import com.konkerlabs.platform.registry.business.repositories.RestDestinationRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.repositories.TransformationRepository;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.LocationService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ApplicationServiceImpl implements ApplicationService {

    private Logger LOGGER = LoggerFactory.getLogger(ApplicationServiceImpl.class);

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private TransformationRepository transformationRepository;

    @Autowired
    private RestDestinationRepository restDestinationRepository;

    @Autowired
    private EventRouteRepository eventRouteRepository;
    
    @Autowired
    private LocationService locationService;

    private ServiceResponse<Application> basicValidate(Tenant tenant, Application application) {
		if (!Optional.ofNullable(tenant).isPresent()) {
			Application app = Application.builder()
					.name("NULL")
					.tenant(Tenant.builder().domainName("unknown_domain").build())
					.build();

			if(LOGGER.isDebugEnabled()){
				LOGGER.debug(CommonValidations.TENANT_NULL.getCode(),
						app.toURI(),
						app.getTenant().getLogLevel());
			}

			return ServiceResponseBuilder.<Application>error()
					.withMessage(CommonValidations.TENANT_NULL.getCode())
					.build();
		}

		if (!tenantRepository.exists(tenant.getId())) {
			LOGGER.debug("device cannot exists",
					Application.builder().name("NULL").tenant(tenant).build().toURI(),
					tenant.getLogLevel());
			return ServiceResponseBuilder.<Application>error()
					.withMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode())
					.build();
		}

		if (!Optional.ofNullable(application).isPresent()) {
			Application app = Application.builder()
					.name("NULL")
					.tenant(tenant)
					.build();
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug(Validations.APPLICATION_NULL.getCode(),
						app.toURI(),
						app.getTenant().getLogLevel());
			}

			return ServiceResponseBuilder.<Application>error()
					.withMessage(Validations.APPLICATION_NULL.getCode())
					.build();
		}

		return null;
	}

    public boolean isDefaultApplication(Application application,Tenant tenant) {
    	if(!Optional.ofNullable(application).isPresent() || !Optional.ofNullable(tenant).isPresent()) {
    		return false;
    	}
    	
    	if(!Optional.ofNullable(application.getName()).isPresent() || !Optional.ofNullable(tenant.getDomainName()).isPresent()) {
    		return false;
    	}
    	
		if(application.getName().equals(tenant.getDomainName()) || 
				DEFAULT_APPLICATION_ALIAS.equals(application.getName())) {
			return true;
		}

		return false;
    }
    
	@Override
	public ServiceResponse<Application> register(Tenant tenant, Application application) {	
		if(Optional.ofNullable(application).isPresent()&& Optional.ofNullable(tenant).isPresent()) {
			if(isDefaultApplication(application, tenant)) {
				application.setName(tenant.getDomainName());
			}
		}
		
		ServiceResponse<Application> response = basicValidate(tenant, application);

		if (Optional.ofNullable(response).isPresent())
			return response;
	
		Optional<Map<String,Object[]>> validations = application.applyValidations();

		if (validations.isPresent()) {
			LOGGER.debug("error saving application",
					Application.builder().name("NULL").tenant(tenant).build().toURI(),
					tenant.getLogLevel());
			return ServiceResponseBuilder.<Application>error()
					.withMessages(validations.get())
					.build();
		}

		if (applicationRepository.findOne(application.getName()) != null) {
			LOGGER.debug("error saving application",
					Application.builder().name("NULL").tenant(tenant).build().toURI(),
					tenant.getLogLevel());
            return ServiceResponseBuilder.<Application>error()
                    .withMessage(Validations.APPLICATION_ALREADY_REGISTERED.getCode())
                    .build();
		}

		application.setTenant(tenant);
		application.setRegistrationDate(Instant.now());
		application.setQualifier(Application.DEFAULT_QUALIFIER);
		Application save = applicationRepository.save(application);
		LOGGER.info("Application created. Name: {}", save.getName(), tenant.toURI(), tenant.getLogLevel());
		
		createLocationDefaultToApplication(tenant, save);

		return ServiceResponseBuilder.<Application>ok().withResult(save).build();
	}

	private void createLocationDefaultToApplication(Tenant tenant, Application application) {
		Location location  = Location.builder()
                .tenant(tenant)
                .application(application)
                .guid(UUID.randomUUID().toString())
                .defaultLocation(true)
                .name(DEFAULT_APPLICATION_ALIAS)
                .build();
		locationService.save(tenant, application, location );
	}

	@Override
	public ServiceResponse<Application> update(Tenant tenant, String name, Application updatingApplication) {
		name = DEFAULT_APPLICATION_ALIAS.equals(name) ? tenant.getDomainName() : name;
		if(Optional.ofNullable(updatingApplication).isPresent()) {
			if(isDefaultApplication(updatingApplication, tenant)) {
				updatingApplication.setName(tenant.getDomainName());
			}
		}
		
		ServiceResponse<Application> response = basicValidate(tenant, updatingApplication);

		if (Optional.ofNullable(response).isPresent())
			return response;

		if (!Optional.ofNullable(name).isPresent())
            return ServiceResponseBuilder.<Application>error()
                    .withMessage(Validations.APPLICATION_NAME_IS_NULL.getCode())
                    .build();

		Application appFromDB = getByApplicationName(tenant, name).getResult();
		if(Optional.ofNullable(appFromDB).isPresent()) {
			if(isDefaultApplication(appFromDB, tenant)) {
				appFromDB.setName(tenant.getDomainName());
			}
		}
		
		if (!Optional.ofNullable(appFromDB).isPresent()) {
			return ServiceResponseBuilder.<Application>error()
                    .withMessage(Validations.APPLICATION_DOES_NOT_EXIST.getCode())
                    .build();
		}

		appFromDB.setFriendlyName(updatingApplication.getFriendlyName());
		appFromDB.setDescription(updatingApplication.getDescription());

		Optional<Map<String, Object[]>> validations = appFromDB.applyValidations();
		if (validations.isPresent()) {
			return ServiceResponseBuilder.<Application>error()
					.withMessages(validations.get())
					.build();
		}

		

		
		Application updated = applicationRepository.save(appFromDB);

		LOGGER.info("Application updated. Name: {}", appFromDB.getName(), tenant.toURI(), tenant.getLogLevel());

		return ServiceResponseBuilder.<Application>ok().withResult(updated).build();
	}

	@Override
	public ServiceResponse<Application> remove(Tenant tenant, String name) {
		if (!Optional.ofNullable(tenant).isPresent()) {
			return ServiceResponseBuilder.<Application>error()
					.withMessage(CommonValidations.TENANT_NULL.getCode())
					.build();
		}

		if (!Optional.ofNullable(name).isPresent()) {
			return ServiceResponseBuilder.<Application>error()
                    .withMessage(Validations.APPLICATION_NAME_IS_NULL.getCode())
                    .build();
		}

		name = DEFAULT_APPLICATION_ALIAS.equals(name) ? tenant.getDomainName() : name;
		
		Application application = applicationRepository.findByTenantAndName(tenant.getId(), name);

		if (!Optional.ofNullable(application).isPresent()) {
			return ServiceResponseBuilder.<Application>error()
                    .withMessage(Validations.APPLICATION_DOES_NOT_EXIST.getCode())
                    .build();
		}

		List<Device> devices = deviceRepository.findAllByTenantIdAndApplicationName(tenant.getId(), name);
		List<EventRoute> routes = eventRouteRepository.findAll(tenant.getId(), name);
		List<Transformation> transformations = transformationRepository.findAllByApplicationId(tenant.getId(), name);
		List<RestDestination> destinations = restDestinationRepository.findAllByTenant(tenant.getId(), name);

		if (!devices.isEmpty()) {
			return ServiceResponseBuilder.<Application>error()
                    .withMessage(Validations.APPLICATION_HAS_DEVICE.getCode())
                    .build();
		}
		if (!routes.isEmpty()) {
			return ServiceResponseBuilder.<Application>error()
                    .withMessage(Validations.APPLICATION_HAS_ROUTE.getCode())
                    .build();
		}
		if (!transformations.isEmpty()) {
			return ServiceResponseBuilder.<Application>error()
                    .withMessage(Validations.APPLICATION_HAS_TRANSFORMATION.getCode())
                    .build();
		}
		if (!destinations.isEmpty()) {
			return ServiceResponseBuilder.<Application>error()
                    .withMessage(Validations.APPLICATION_HAS_REST_DESTINATION.getCode())
                    .build();
		}

		applicationRepository.delete(application);

		return ServiceResponseBuilder.<Application>ok()
				.withMessage(Messages.APPLICATION_REMOVED_SUCCESSFULLY.getCode())
				.withResult(application)
				.build();
	}

	@Override
	public ServiceResponse<List<Application>> findAll(Tenant tenant) {
		List<Application> all = applicationRepository.findAllByTenant(tenant.getId());	
		return ServiceResponseBuilder.<List<Application>>ok().withResult(all).build();
	}

	@Override
	public ServiceResponse<Application> getByApplicationName(Tenant tenant, String name) {
		if (!Optional.ofNullable(tenant).isPresent()) {
			return ServiceResponseBuilder.<Application>error()
					.withMessage(CommonValidations.TENANT_NULL.getCode())
					.build();
		}
		if (!Optional.ofNullable(name).isPresent()) {
			return ServiceResponseBuilder.<Application>error()
					.withMessage(Validations.APPLICATION_NAME_IS_NULL.getCode())
					.build();
		}
		
		name = DEFAULT_APPLICATION_ALIAS.equals(name) ? tenant.getDomainName() : name;

		Tenant tenantFromDB = tenantRepository.findByDomainName(tenant.getDomainName());

		if (!Optional.ofNullable(tenantFromDB).isPresent())
			return ServiceResponseBuilder.<Application> error()
					.withMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()).build();

		Application application = applicationRepository.findByTenantAndName(tenantFromDB.getId(), name);
		if (!Optional.ofNullable(application).isPresent()) {
			return ServiceResponseBuilder.<Application> error()
					.withMessage(Validations.APPLICATION_DOES_NOT_EXIST.getCode()).build();
		}

		return ServiceResponseBuilder.<Application>ok().withResult(application).build();
	}

}
