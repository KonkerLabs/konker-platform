package com.konkerlabs.platform.registry.business.services;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.ApplicationRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
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
    
    private ServiceResponse<Application> basicValidate(Tenant tenant, Application application) {
		if (!Optional.ofNullable(tenant).isPresent()) {
			Application app = Application.builder()
					.name("NULL")
					.tenant(Tenant.builder().domainName("unknow_domain").build())
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

	@Override
	public ServiceResponse<Application> register(Tenant tenant, Application application) {
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
		application.setQualifier(tenant.getName());
		Application save = applicationRepository.save(application);
		LOGGER.info("Application created. Name: {}", save.getName(), tenant.toURI(), tenant.getLogLevel());
		
		return ServiceResponseBuilder.<Application>ok().withResult(save).build();
	}

	@Override
	public ServiceResponse<Application> update(Tenant tenant, String name, Application updatingApplication) {
		ServiceResponse<Application> response = basicValidate(tenant, updatingApplication);
		
		if (Optional.ofNullable(response).isPresent())
			return response;
		
		if (!Optional.ofNullable(name).isPresent())
            return ServiceResponseBuilder.<Application>error()
                    .withMessage(Validations.APPLICATION_NAME_IS_NULL.getCode())
                    .build();
		
		Application appFromDB = getByApplicationName(tenant, name).getResult();
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
		
		Application application = applicationRepository.findByTenantAndName(tenant.getId(), name);
		
		if (!Optional.ofNullable(application).isPresent()) {
			return ServiceResponseBuilder.<Application>error()
                    .withMessage(Validations.APPLICATION_DOES_NOT_EXIST.getCode())
                    .build();
		}
		
		//TODO validar se tem algum device/rota/transformacao/rest atrelado a aplicacao
		
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
		
		Tenant tenantFromDB = tenantRepository.findByName(tenant.getName());
		
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
