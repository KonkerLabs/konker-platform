package com.konkerlabs.platform.registry.business.services;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.konkerlabs.platform.registry.business.model.LoginAudit;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.model.LoginAudit.LoginAuditEvent;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.LoginAuditRepository;
import com.konkerlabs.platform.registry.business.services.api.LoginAuditService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.business.services.api.UserService.Validations;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class LoginAuditServiceImpl implements LoginAuditService {

	@Autowired
	private LoginAuditRepository loginAuditRepository;

	@Override
	public ServiceResponse<LoginAudit> register(Tenant tenant, User user, LoginAuditEvent event) {

		if (!Optional.ofNullable(tenant).isPresent()) {
			return ServiceResponseBuilder.<LoginAudit>error()
					.withMessage(CommonValidations.TENANT_NULL.getCode())
					.build();
		}

        if (!Optional.ofNullable(user).isPresent()) {
            return ServiceResponseBuilder.<LoginAudit>error()
                    .withMessage(Validations.INVALID_USER_DETAILS.getCode())
                    .build();
        }

		try {
			LoginAudit loginAudit = LoginAudit.builder().time(new Date()).event(event.name()).user(user).tenant(tenant)
					.build();
			LoginAudit saved = loginAuditRepository.save(loginAudit);

			return ServiceResponseBuilder.<LoginAudit>ok().withResult(saved).build();
		} catch (Exception e) {
			return ServiceResponseBuilder.<LoginAudit>error().withMessage(e.getMessage()).build();
		}

	}

}
