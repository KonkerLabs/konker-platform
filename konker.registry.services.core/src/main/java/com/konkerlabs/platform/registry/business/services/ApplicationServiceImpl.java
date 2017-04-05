package com.konkerlabs.platform.registry.business.services;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

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
    
    private Pattern regex = Pattern.compile("[$&+,:;=?@#|'<>.-^*()%!\\s{2,}]");

    @Autowired
    private ApplicationRepository applicationRepository;
    
    @Autowired
    private TenantRepository tenantRepository;

	@Override
	public ServiceResponse<Application> register(Tenant tenant, Application application) {
		if (!Optional.ofNullable(tenant).isPresent()) {
			Application app = Application.builder()
					.guid("NULL")
					.tenant(Tenant.builder().domainName("unknow_domain").build())
					.build();
			
			LOGGER.debug(CommonValidations.TENANT_NULL.getCode(),
					app.toURI(),
					app.getTenant().getLogLevel());
			return ServiceResponseBuilder.<Application>error()
					.withMessage(CommonValidations.TENANT_NULL.getCode())
					.build();
		}
		
		if (!Optional.ofNullable(application).isPresent()) {
			Application app = Application.builder()
					.guid("NULL")
					.tenant(tenant)
					.build();
			
			LOGGER.debug(Validations.APPLICATION_NULL.getCode(),
					app.toURI(),
					app.getTenant().getLogLevel());
			
			return ServiceResponseBuilder.<Application>error()
					.withMessage(Validations.APPLICATION_NULL.getCode())
					.build();
		}
		
		if (regex.matcher(application.getName()).find()) {
			System.out.println("chegou");
		}
		
		return null;
	}

	@Override
	public ServiceResponse<Application> update(Tenant tenant, String guid, Application application) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServiceResponse<Application> remove(Tenant tenant, String guid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServiceResponse<List<Application>> findAll(Tenant tenant) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServiceResponse<Application> getByApplicationGuid(Tenant tenant, String guid) {
		// TODO Auto-generated method stub
		return null;
	}

}
