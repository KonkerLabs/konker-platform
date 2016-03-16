package com.konkerlabs.platform.registry.business.services;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.RestDestination;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.repositories.RestDestinationRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;

import com.konkerlabs.platform.registry.business.services.api.RestDestinationService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
                    .<List<RestDestination>>build();
        } catch (BusinessException be) {
            return ServiceResponse.<List<RestDestination>> builder().responseMessage(be.getMessage())
                    .status(ServiceResponse.Status.ERROR).<List<RestDestination>>build();
        }
    }

    @Override
    public ServiceResponse<RestDestination> getByGUID(Tenant tenant, String guid) {
        try {
            Optional.ofNullable(tenant).orElseThrow(() -> new BusinessException("Tenant cannot be null"));
            Optional.ofNullable(guid).orElseThrow(() -> new BusinessException("REST Destination ID cannot be null"));

            RestDestination restDestination = Optional.ofNullable(restRepository.getByTenantAndGUID(tenant.getId(), guid))
                    .orElseThrow(() -> new BusinessException("REST Destination does not exist"));

            return ServiceResponse.<RestDestination> builder().result(restDestination).status(ServiceResponse.Status.OK)
                    .<RestDestination>build();
        } catch (BusinessException be) {
            return ServiceResponse.<RestDestination> builder().responseMessage(be.getMessage())
                    .status(ServiceResponse.Status.ERROR).<RestDestination>build();
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
            destination.setGuid(UUID.randomUUID().toString());

            List<String> validations = Optional.ofNullable(destination.applyValidations())
                    .orElse(Collections.emptyList());
            if (!validations.isEmpty()) {
                return ServiceResponse.<RestDestination> builder().responseMessages(validations)
                        .status(ServiceResponse.Status.ERROR).<RestDestination>build();
            }

            RestDestination saved = restRepository.save(destination);

            return ServiceResponse.<RestDestination> builder().result(saved).status(ServiceResponse.Status.OK)
                    .<RestDestination>build();
        } catch (BusinessException be) {
            return ServiceResponse.<RestDestination> builder().responseMessage(be.getMessage())
                    .status(ServiceResponse.Status.ERROR).<RestDestination>build();
        }
    }

    @Override
    public ServiceResponse<RestDestination> update(Tenant tenant, String uuid, RestDestination destination) {
        try {
            Optional.ofNullable(tenant).orElseThrow(() -> new BusinessException("Tenant cannot be null"));
            Optional.ofNullable(destination)
                    .orElseThrow(() -> new BusinessException("REST Destination cannot be null"));
            Optional.ofNullable(uuid).orElseThrow(() -> new BusinessException("REST Destination ID cannot be null"));

            Tenant savedTenant = tenantRepository.findByDomainName(tenant.getDomainName());
            Optional.ofNullable(savedTenant).orElseThrow(() -> new BusinessException("Tenant does not exist"));

            RestDestination byName = restRepository.getByTenantAndName(savedTenant.getId(), destination.getName());
            if (!uuid.equals(Optional.ofNullable(byName).map(RestDestination::getGuid).orElse(uuid))) {
                throw new BusinessException("REST Destination Name already exists");
            }

            RestDestination old = restRepository.getByTenantAndGUID(savedTenant.getId(), uuid);
            Optional.ofNullable(old).orElseThrow(() -> new BusinessException("REST Destination does not exist"));

            destination.setId(old.getId());
            destination.setGuid(old.getGuid());
            destination.setTenant(tenant);

            List<String> validations = Optional.ofNullable(destination.applyValidations())
                    .orElse(Collections.emptyList());
            if (!validations.isEmpty()) {
                return ServiceResponse.<RestDestination> builder().responseMessages(validations)
                        .status(ServiceResponse.Status.ERROR).<RestDestination>build();
            }

            RestDestination saved = restRepository.save(destination);

            return ServiceResponse.<RestDestination> builder().result(saved).status(ServiceResponse.Status.OK)
                    .<RestDestination>build();
        } catch (BusinessException be) {
            return ServiceResponse.<RestDestination> builder().responseMessage(be.getMessage())
                    .status(ServiceResponse.Status.ERROR).<RestDestination>build();
        }
    }

}
