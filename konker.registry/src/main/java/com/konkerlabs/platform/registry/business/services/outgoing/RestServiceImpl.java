package com.konkerlabs.platform.registry.business.services.outgoing;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.destinations.RestDestination;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.repositories.outgoing.RestRepository;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RestServiceImpl implements RestService {

    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private RestRepository restRepository;

    @Override
    public ServiceResponse<List<RestDestination>> getAll(Tenant tenant) {
        try {
            Optional.ofNullable(tenant).orElseThrow(() -> new BusinessException("Tenant cannot be null"));

            Optional.ofNullable(tenantRepository.findOne(tenant.getId()))
                    .orElseThrow(() -> new BusinessException("Tenant does not exist"));

            List<RestDestination> restDestinationList = Optional.ofNullable(restRepository.getAllByTenant(tenant.getId()))
                    .orElseThrow(() -> new BusinessException("RestDestination outgoing list does not exist"));

            return ServiceResponse.<List<RestDestination>>builder()
                    .result(restDestinationList)
                    .status(ServiceResponse.Status.OK)
                    .<List<RestDestination>>build();
        } catch (BusinessException be) {
            return ServiceResponse.<List<RestDestination>>builder()
                    .responseMessage(be.getMessage())
                    .status(ServiceResponse.Status.ERROR)
                    .<List<RestDestination>>build();
        }
    }

    @Override
    public ServiceResponse<RestDestination> get(Tenant tenant, String RestId) {
        try {
            Optional.ofNullable(tenant).orElseThrow(() -> new BusinessException("Tenant cannot be null"));
            Optional.ofNullable(RestId).orElseThrow(() -> new BusinessException("RestDestination outgoing ID cannot be null"));

            Optional.ofNullable(tenantRepository.findOne(tenant.getId()))
                    .orElseThrow(() -> new BusinessException("Tenant does not exist"));

            RestDestination restDestinationList = Optional.ofNullable(restRepository.findOne(RestId))
                    .orElseThrow(() -> new BusinessException("RestDestination outgoing does not exist"));

            return ServiceResponse.<RestDestination>builder()
                    .result(restDestinationList)
                    .status(ServiceResponse.Status.OK)
                    .<RestDestination>build();
        } catch (BusinessException be) {
            return ServiceResponse.<RestDestination>builder()
                    .responseMessage(be.getMessage())
                    .status(ServiceResponse.Status.ERROR)
                    .<RestDestination>build();
        }
    }

    @Override
    public ServiceResponse<RestDestination> getByUri(Tenant tenant, URI RestUri) {
        try {
            Optional.ofNullable(tenant).orElseThrow(() -> new BusinessException("Tenant cannot be null"));
            Optional.ofNullable(RestUri).orElseThrow(() -> new BusinessException("RestDestination outgoing URI cannot be null"));

            Optional.ofNullable(tenantRepository.findOne(tenant.getId()))
                    .orElseThrow(() -> new BusinessException("Tenant does not exist"));

            RestDestination restDestinationList = Optional.ofNullable(restRepository.findByURI(RestUri))
                    .orElseThrow(() -> new BusinessException("RestDestination outgoing does not exist"));

            return ServiceResponse.<RestDestination>builder()
                    .result(restDestinationList)
                    .status(ServiceResponse.Status.OK)
                    .<RestDestination>build();
        } catch (BusinessException be) {
            return ServiceResponse.<RestDestination>builder()
                    .responseMessage(be.getMessage())
                    .status(ServiceResponse.Status.ERROR)
                    .<RestDestination>build();
        }
    }

    @Override
    public ServiceResponse<RestDestination> save(Tenant tenant, RestDestination RestDestination) {
        try {
            Optional.ofNullable(tenant).orElseThrow(() -> new BusinessException("Tenant cannot be null"));
            Optional.ofNullable(RestDestination).orElseThrow(() -> new BusinessException("RestDestination outgoing cannot be null"));

            Optional.ofNullable(tenantRepository.findOne(tenant.getId()))
                    .orElseThrow(() -> new BusinessException("Tenant does not exist"));

            RestDestination.setTenant(tenant);

            RestDestination restDestinationList = Optional.ofNullable(restRepository.findOne(tenant.getId()))
                    .orElseThrow(() -> new BusinessException("RestDestination outgoing does not exist"));

            return ServiceResponse.<RestDestination>builder()
                    .result(restDestinationList)
                    .status(ServiceResponse.Status.OK)
                    .<RestDestination>build();
        } catch (BusinessException be) {
            return ServiceResponse.<RestDestination>builder()
                    .responseMessage(be.getMessage())
                    .status(ServiceResponse.Status.ERROR)
                    .<RestDestination>build();
        }
    }
}
