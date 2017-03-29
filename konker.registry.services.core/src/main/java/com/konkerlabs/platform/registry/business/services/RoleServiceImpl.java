package com.konkerlabs.platform.registry.business.services;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.konkerlabs.platform.registry.business.model.Role;
import com.konkerlabs.platform.registry.business.repositories.RoleRepository;
import com.konkerlabs.platform.registry.business.services.api.RoleService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;

@Service
public class RoleServiceImpl implements RoleService {
	private static final Logger LOGGER = LoggerFactory.getLogger(RoleServiceImpl.class);
	
    @Autowired
    private RoleRepository roleRepository;

    public RoleServiceImpl() {
    }

	@Override
	public ServiceResponse<Role> findByName(String name) {
		Role role = roleRepository.findByName(name);
		
		if (!Optional.ofNullable(role).isPresent()) {
			return ServiceResponseBuilder.<Role> error()
					.withMessage(RoleService.Validations.ROLE_NOT_EXIST.getCode())
					.build();
		}
		
		return ServiceResponseBuilder.<Role> ok()
				.withResult(role)
				.build();
	}

}
