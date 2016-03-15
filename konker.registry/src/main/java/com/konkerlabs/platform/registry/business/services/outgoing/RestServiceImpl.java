package com.konkerlabs.platform.registry.business.services.outgoing;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.outgoing.Rest;
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
    public ServiceResponse<List<Rest>> getAll(Tenant tenant) {
        try {
            Optional.ofNullable(tenant).orElseThrow(() -> new BusinessException("Tenant cannot be null"));

            Optional.ofNullable(tenantRepository.findOne(tenant.getId()))
                    .orElseThrow(() -> new BusinessException("Tenant does not exist"));

            List<Rest> RestList = Optional.ofNullable(restRepository.getAllByTenant(tenant.getId()))
                    .orElseThrow(() -> new BusinessException("Rest outgoing list does not exist"));

            return ServiceResponse.<List<Rest>>builder()
                    .result(RestList)
                    .status(ServiceResponse.Status.OK)
                    .<List<Rest>>build();
        } catch (BusinessException be) {
            return ServiceResponse.<List<Rest>>builder()
                    .responseMessage(be.getMessage())
                    .status(ServiceResponse.Status.ERROR)
                    .<List<Rest>>build();
        }
    }

    @Override
    public ServiceResponse<Rest> get(Tenant tenant, String RestId) {
        try {
            Optional.ofNullable(tenant).orElseThrow(() -> new BusinessException("Tenant cannot be null"));
            Optional.ofNullable(RestId).orElseThrow(() -> new BusinessException("Rest outgoing ID cannot be null"));

            Optional.ofNullable(tenantRepository.findOne(tenant.getId()))
                    .orElseThrow(() -> new BusinessException("Tenant does not exist"));

            Rest RestList = Optional.ofNullable(restRepository.findOne(RestId))
                    .orElseThrow(() -> new BusinessException("Rest outgoing does not exist"));

            return ServiceResponse.<Rest>builder()
                    .result(RestList)
                    .status(ServiceResponse.Status.OK)
                    .<Rest>build();
        } catch (BusinessException be) {
            return ServiceResponse.<Rest>builder()
                    .responseMessage(be.getMessage())
                    .status(ServiceResponse.Status.ERROR)
                    .<Rest>build();
        }
    }

    @Override
    public ServiceResponse<Rest> getByUri(Tenant tenant, URI RestUri) {
        try {
            Optional.ofNullable(tenant).orElseThrow(() -> new BusinessException("Tenant cannot be null"));
            Optional.ofNullable(RestUri).orElseThrow(() -> new BusinessException("Rest outgoing URI cannot be null"));

            Optional.ofNullable(tenantRepository.findOne(tenant.getId()))
                    .orElseThrow(() -> new BusinessException("Tenant does not exist"));

            Rest RestList = Optional.ofNullable(restRepository.findByURI(RestUri))
                    .orElseThrow(() -> new BusinessException("Rest outgoing does not exist"));

            return ServiceResponse.<Rest>builder()
                    .result(RestList)
                    .status(ServiceResponse.Status.OK)
                    .<Rest>build();
        } catch (BusinessException be) {
            return ServiceResponse.<Rest>builder()
                    .responseMessage(be.getMessage())
                    .status(ServiceResponse.Status.ERROR)
                    .<Rest>build();
        }
    }

    @Override
    public ServiceResponse<Rest> save(Tenant tenant, Rest Rest) {
        try {
            Optional.ofNullable(tenant).orElseThrow(() -> new BusinessException("Tenant cannot be null"));
            Optional.ofNullable(Rest).orElseThrow(() -> new BusinessException("Rest outgoing cannot be null"));

            Optional.ofNullable(tenantRepository.findOne(tenant.getId()))
                    .orElseThrow(() -> new BusinessException("Tenant does not exist"));

            Rest.setTenant(tenant);

            Rest RestList = Optional.ofNullable(restRepository.findOne(tenant.getId()))
                    .orElseThrow(() -> new BusinessException("Rest outgoing does not exist"));

            return ServiceResponse.<Rest>builder()
                    .result(RestList)
                    .status(ServiceResponse.Status.OK)
                    .<Rest>build();
        } catch (BusinessException be) {
            return ServiceResponse.<Rest>builder()
                    .responseMessage(be.getMessage())
                    .status(ServiceResponse.Status.ERROR)
                    .<Rest>build();
        }
    }
}
