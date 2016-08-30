package com.konkerlabs.platform.registry.business.services;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.RestDestination;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.RestDestinationRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.NewServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.RestDestinationService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
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
    public NewServiceResponse<List<RestDestination>> findAll(Tenant tenant) {
        if (!Optional.ofNullable(tenant).isPresent())
            return ServiceResponseBuilder.<List<RestDestination>>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode()).build();

        Tenant existingTenant = tenantRepository.findByDomainName(tenant.getDomainName());

        if (!Optional.ofNullable(existingTenant).isPresent())
            return ServiceResponseBuilder.<List<RestDestination>>error()
                    .withMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()).build();

        List<RestDestination> RestList = restRepository.findAllByTenant(existingTenant.getId());

        return ServiceResponseBuilder.<List<RestDestination>>ok().withResult(RestList).build();
    }

    @Override
    public NewServiceResponse<RestDestination> getByGUID(Tenant tenant, String guid) {
        if (!Optional.ofNullable(tenant).isPresent())
            return ServiceResponseBuilder.<RestDestination>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode()).build();

        if (!Optional.ofNullable(guid).filter(s -> !s.isEmpty()).isPresent())
            return ServiceResponseBuilder.<RestDestination>error()
                    .withMessage(Validations.GUID_NULL.getCode()).build();

        Tenant existingTenant = tenantRepository.findByDomainName(tenant.getDomainName());

        if (!Optional.ofNullable(existingTenant).isPresent())
            return ServiceResponseBuilder.<RestDestination>error()
                    .withMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()).build();

        RestDestination restDestination = restRepository.getByTenantAndGUID(existingTenant.getId(),guid);

        if (!Optional.ofNullable(restDestination).isPresent())
            return ServiceResponseBuilder.<RestDestination>error()
                    .withMessage(Validations.DESTINATION_NOT_FOUND.getCode()).build();

        return ServiceResponseBuilder.<RestDestination>ok().withResult(restDestination).build();
    }

    @Override
    public NewServiceResponse<RestDestination> register(final Tenant tenant, RestDestination destination) {
        if (!Optional.ofNullable(tenant).isPresent())
            return ServiceResponseBuilder.<RestDestination>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode()).build();

        if (!Optional.ofNullable(destination).isPresent())
            return ServiceResponseBuilder.<RestDestination>error()
                    .withMessage(CommonValidations.RECORD_NULL.getCode()).build();

        Tenant existingTenant = tenantRepository.findByDomainName(tenant.getDomainName());

        if (!Optional.ofNullable(existingTenant).isPresent())
            return ServiceResponseBuilder.<RestDestination>error()
                    .withMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()).build();

        if (Optional.ofNullable(restRepository.getByTenantAndName(existingTenant.getId(),destination.getName())).isPresent())
            return ServiceResponseBuilder.<RestDestination>error()
                    .withMessage(Validations.NAME_IN_USE.getCode()).build();

        destination.setId(null);
        destination.setTenant(existingTenant);
        destination.setGuid(UUID.randomUUID().toString());

        Optional<Map<String,Object[]>> validations = destination.applyValidations();

        if (validations.isPresent()) {
            return ServiceResponseBuilder.<RestDestination>error()
                    .withMessages(validations.get()).build();
        }

        RestDestination saved = restRepository.save(destination);

        return ServiceResponseBuilder.<RestDestination>ok()
                .withResult(saved).build();
    }

    @Override
    public NewServiceResponse<RestDestination> update(Tenant tenant, String guid, RestDestination destination) {
        if (!Optional.ofNullable(tenant).isPresent())
            return ServiceResponseBuilder.<RestDestination>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode()).build();

        if (!Optional.ofNullable(destination).isPresent())
            return ServiceResponseBuilder.<RestDestination>error()
                    .withMessage(CommonValidations.RECORD_NULL.getCode()).build();

        if (!Optional.ofNullable(guid).filter(s -> !s.isEmpty()).isPresent())
            return ServiceResponseBuilder.<RestDestination>error()
                    .withMessage(Validations.GUID_NULL.getCode()).build();

        Tenant existingTenant = tenantRepository.findByDomainName(tenant.getDomainName());

        if (!Optional.ofNullable(existingTenant).isPresent())
            return ServiceResponseBuilder.<RestDestination>error()
                    .withMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()).build();

        RestDestination existingDestination = restRepository.getByTenantAndGUID(existingTenant.getId(),guid);

        if (!Optional.ofNullable(existingDestination).isPresent())
            return ServiceResponseBuilder.<RestDestination>error()
                    .withMessage(Validations.DESTINATION_NOT_FOUND.getCode()).build();

//        RestDestination byName = restRepository.getByTenantAndName(savedTenant.getId(), destination.getName());
//        if (!guid.equals(Optional.ofNullable(byName).map(RestDestination::getGuid).orElse(guid))) {
//            throw new BusinessException("REST Destination Name already exists");
//        }

        if (Optional.ofNullable(restRepository.getByTenantAndName(existingTenant.getId(),destination.getName()))
                .filter(restDestination -> !restDestination.getGuid().equals(guid))
                .isPresent())
            return ServiceResponseBuilder.<RestDestination>error()
                    .withMessage(Validations.NAME_IN_USE.getCode()).build();

        destination.setId(existingDestination.getId());
        destination.setGuid(existingDestination.getGuid());
        destination.setTenant(existingTenant);

        Optional<Map<String,Object[]>> validations = destination.applyValidations();

        if (validations.isPresent()) {
            return ServiceResponseBuilder.<RestDestination>error()
                    .withMessages(validations.get()).build();
        }

        RestDestination saved = restRepository.save(destination);

        return ServiceResponseBuilder.<RestDestination>ok()
                .withResult(saved).build();
    }

}
