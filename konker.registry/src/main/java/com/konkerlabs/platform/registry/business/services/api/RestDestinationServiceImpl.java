package com.konkerlabs.platform.registry.business.services.api;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.RestDestination;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.repositories.RestDestinationRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RestDestinationServiceImpl implements RestDestinationService {

    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private RestDestinationRepository restRepository;

    @Override
    public ServiceResponse<List<RestDestination>> findAll(Tenant tenant) {
        try {
            Optional.ofNullable(tenant).orElseThrow(() -> new BusinessException("Tenant cannot be null"));

            Optional.ofNullable(tenantRepository.findByDomainName(tenant.getDomainName()))
                    .orElseThrow(() -> new BusinessException("Tenant does not exist"));

            List<RestDestination> RestList = restRepository.findAllByTenant(tenant.getId());

            return ServiceResponse.<List<RestDestination>> builder().result(RestList).status(ServiceResponse.Status.OK)
                    .build();
        } catch (BusinessException be) {
            return ServiceResponse.<List<RestDestination>> builder().responseMessage(be.getMessage())
                    .status(ServiceResponse.Status.ERROR).build();
        }
    }

    @Override
    public ServiceResponse<RestDestination> getByID(Tenant tenant, String id) {
        try {
            Optional.ofNullable(tenant).orElseThrow(() -> new BusinessException("Tenant cannot be null"));
            Optional.ofNullable(id).orElseThrow(() -> new BusinessException("REST Destination ID cannot be null"));

            RestDestination restDestination = Optional.ofNullable(restRepository.getByTenantAndID(tenant.getId(), id))
                    .orElseThrow(() -> new BusinessException("REST Destination does not exist"));

            return ServiceResponse.<RestDestination> builder().result(restDestination).status(ServiceResponse.Status.OK)
                    .build();
        } catch (BusinessException be) {
            return ServiceResponse.<RestDestination> builder().responseMessage(be.getMessage())
                    .status(ServiceResponse.Status.ERROR).build();
        }
    }

    @Override
    public ServiceResponse<RestDestination> register(final Tenant tenant, RestDestination destination) {
        try {
            Optional.ofNullable(tenant).orElseThrow(() -> new BusinessException("Tenant cannot be null"));
            Optional.ofNullable(destination)
                    .orElseThrow(() -> new BusinessException("REST Destination cannot be null"));

            Tenant savedTenant = tenantRepository.findByDomainName(tenant.getDomainName());
            Optional.ofNullable(savedTenant).orElseThrow(() -> new BusinessException("Tenant does not exist"));

            if (restRepository.getByTenantAndName(savedTenant.getId(), destination.getName()) != null) {
                throw new BusinessException("Name already exists");
            }

            destination.setId(null);
            destination.setTenant(savedTenant);

            List<String> validations = Optional.ofNullable(destination.applyValidations())
                    .orElse(Collections.emptyList());
            if (!validations.isEmpty()) {
                return ServiceResponse.<RestDestination> builder().responseMessages(validations)
                        .status(ServiceResponse.Status.ERROR).build();
            }

            RestDestination saved = restRepository.save(destination);

            return ServiceResponse.<RestDestination> builder().result(saved).status(ServiceResponse.Status.OK).build();
        } catch (BusinessException be) {
            return ServiceResponse.<RestDestination> builder().responseMessage(be.getMessage())
                    .status(ServiceResponse.Status.ERROR).build();
        }
    }

    @Override
    public ServiceResponse<RestDestination> update(Tenant tenant, String id, RestDestination destination) {
        try {
            Optional.ofNullable(tenant).orElseThrow(() -> new BusinessException("Tenant cannot be null"));
            Optional.ofNullable(destination)
                    .orElseThrow(() -> new BusinessException("REST Destination cannot be null"));
            Optional.ofNullable(id).orElseThrow(() -> new BusinessException("REST Destination ID cannot be null"));

            Tenant savedTenant = tenantRepository.findByDomainName(tenant.getDomainName());
            Optional.ofNullable(savedTenant).orElseThrow(() -> new BusinessException("Tenant does not exist"));

            RestDestination byName = restRepository.getByTenantAndName(savedTenant.getId(), destination.getName());
            if (!id.equals(Optional.ofNullable(byName).map(RestDestination::getId).orElse(id))) {
                throw new BusinessException("REST Destination Name already exists");
            }

            RestDestination old = restRepository.getByTenantAndID(savedTenant.getId(), id);
            Optional.ofNullable(old).orElseThrow(() -> new BusinessException("REST Destination does not exist"));

            destination.setId(old.getId());
            destination.setTenant(tenant);

            List<String> validations = Optional.ofNullable(destination.applyValidations())
                    .orElse(Collections.emptyList());
            if (!validations.isEmpty()) {
                return ServiceResponse.<RestDestination> builder().responseMessages(validations)
                        .status(ServiceResponse.Status.ERROR).build();
            }

            RestDestination saved = restRepository.save(destination);

            return ServiceResponse.<RestDestination> builder().result(saved).status(ServiceResponse.Status.OK).build();
        } catch (BusinessException be) {
            return ServiceResponse.<RestDestination> builder().responseMessage(be.getMessage())
                    .status(ServiceResponse.Status.ERROR).build();
        }
    }

}
