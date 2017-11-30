package com.konkerlabs.platform.registry.business.services;

import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.ApplicationRepository;
import com.konkerlabs.platform.registry.business.repositories.GatewayRepository;
import com.konkerlabs.platform.registry.business.repositories.LocationRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.GatewayService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class GatewayServiceImpl implements GatewayService {

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private GatewayRepository gatewayRepository;
    @Autowired
    private LocationRepository locationRepository;
    
    @Override
    public ServiceResponse<Gateway> save(Tenant tenant, Application application, Gateway route) {

        ServiceResponse<Gateway> validationResponse = validate(tenant, application);
        if (!validationResponse.isOk()) {
            return validationResponse;
        }

        route.setId(null);
        route.setTenant(tenant);
        route.setApplication(application);
        route.setGuid(UUID.randomUUID().toString());

        Optional<Map<String,Object[]>> validations = route.applyValidations();

        if (validations.isPresent()) {
            return ServiceResponseBuilder.<Gateway>error()
                .withMessages(validations.get()).build();
        }

        if (Optional.ofNullable(gatewayRepository.findByName(tenant.getId(),
                                                                     application.getName(),
                                                                     route.getName())).isPresent()) {
            return ServiceResponseBuilder.<Gateway>error()
                    .withMessage(Validations.NAME_IN_USE.getCode()).build();
        }

        Gateway saved = gatewayRepository.save(route);

        LOGGER.info("Gateway created. Name: {}", route.getName(), tenant.toURI(), tenant.getLogLevel());

        return ServiceResponseBuilder.<Gateway>ok().withResult(saved).build();
    }

    @Override
    public ServiceResponse<Gateway> update(Tenant tenant, Application application, String guid, Gateway updatingGateway) {

        ServiceResponse<Gateway> validationResponse = validate(tenant, application);
        if (!validationResponse.isOk()) {
            return validationResponse;
        }

        if (!Optional.ofNullable(updatingGateway).isPresent())
            return ServiceResponseBuilder.<Gateway>error()
                    .withMessage(CommonValidations.RECORD_NULL.getCode())
                    .build();

        if (!Optional.ofNullable(guid).filter(s -> !s.isEmpty()).isPresent()) {
            return ServiceResponseBuilder.<Gateway>error()
                    .withMessage(Validations.GUID_NULL.getCode())
                    .build();
        }

        Gateway current = gatewayRepository.findByGuid(
            tenant.getId(),
            application.getName(),
            guid
        );

        if (!Optional.ofNullable(current).isPresent())
            return ServiceResponseBuilder.<Gateway>error()
                    .withMessage(Validations.GATEWAY_NOT_FOUND.getCode())
                    .build();

        current.setName(updatingGateway.getName());
        current.setDescription(updatingGateway.getDescription());
        current.setLocation(updatingGateway.getLocation());
        current.setActive(updatingGateway.isActive());

        Optional<Map<String,Object[]>> validations = current.applyValidations();

        if (validations.isPresent()) {
            return ServiceResponseBuilder.<Gateway>error()
                    .withMessages(validations.get())
                    .build();
        }

        if (Optional.ofNullable(gatewayRepository.findByName(tenant.getId(),
                                                                     application.getName(),
                                                                     current.getName()))
                .filter(gateway1 -> !gateway1.getGuid().equals(current.getGuid()))
                .isPresent()) {
            return ServiceResponseBuilder.<Gateway>error()
                    .withMessage(Validations.NAME_IN_USE.getCode())
                    .build();
        }

        Gateway saved = gatewayRepository.save(current);

        LOGGER.info("Gateway updated. Name: {}", saved.getName(), tenant.toURI(), tenant.getLogLevel());

        return ServiceResponseBuilder.<Gateway>ok().withResult(saved).build();
    }

    @Override
    public ServiceResponse<List<Gateway>> getAll(Tenant tenant, Application application) {

        ServiceResponse<List<Gateway>> validationResponse = validate(tenant, application);
        if (!validationResponse.isOk()) {
            return validationResponse;
        }

        return ServiceResponseBuilder.<List<Gateway>>ok()
            .withResult(gatewayRepository.findAll(tenant.getId(), application.getName()))
            .build();
    }

    @Override
    public ServiceResponse<Gateway> getByGUID(Tenant tenant, Application application, String guid) {

        ServiceResponse<Gateway> validationResponse = validate(tenant, application);
        if (!validationResponse.isOk()) {
            return validationResponse;
        }

        if (!Optional.ofNullable(guid).isPresent())
            return ServiceResponseBuilder.<Gateway>error()
                    .withMessage(Validations.GUID_NULL.getCode())
                    .build();

        Gateway route = gatewayRepository.findByGuid(tenant.getId(), application.getName(), guid);

        if (!Optional.ofNullable(route).isPresent())
            return ServiceResponseBuilder.<Gateway>error()
                    .withMessage(Validations.GATEWAY_NOT_FOUND.getCode())
                    .build();

        return ServiceResponseBuilder.<Gateway>ok().withResult(route)
                .build();

    }

    @Override
    public ServiceResponse<Gateway> remove(Tenant tenant, Application application, String guid) {

        ServiceResponse<Gateway> validationResponse = validate(tenant, application);
        if (!validationResponse.isOk()) {
            return validationResponse;
        }

        if (!Optional.ofNullable(guid).filter(s -> !s.isEmpty()).isPresent()) {
            return ServiceResponseBuilder.<Gateway>error()
                    .withMessage(Validations.GUID_NULL.getCode())
                    .build();
        }

        Gateway current = gatewayRepository.findByGuid(
                tenant.getId(),
                application.getName(),
                guid
        );

        if (!Optional.ofNullable(current).isPresent())
            return ServiceResponseBuilder.<Gateway>error()
                    .withMessage(Validations.GATEWAY_NOT_FOUND.getCode())
                    .build();

        gatewayRepository.delete(current);

        LOGGER.info("Gateway removed. Name: {}", current.getName(), tenant.toURI(), tenant.getLogLevel());

        return ServiceResponseBuilder.<Gateway>ok().withResult(current).build();

    }

    @Override
    public ServiceResponse<Boolean> validateGatewayAuthorization(
            Gateway source,
            Location locationToAuthorize) {

        if(source == null || locationToAuthorize == null || source.getLocation() == null){
            return ServiceResponseBuilder
                    .<Boolean> error()
                    .withMessage(Validations.INVALID_GATEWAY_LOCATION.getCode())
                    .build();
        }

        if(locationToAuthorize.getTenant() == null ||
                locationToAuthorize.getApplication() == null){
            locationToAuthorize = locationRepository.
                    findByTenantAndApplicationAndName(
                            source.getTenant().getId(),
                            source.getApplication().getName(),
                            locationToAuthorize.getName());
        }

        if(source.getLocation().getChildren() == null ||
                source.getLocation().getChildren().size() == 0){
            List<Location> sourceChildrens =
                    locationRepository.findChildrensByParentId(
                            source.getTenant().getId(),
                            source.getApplication().getName(),
                            source.getLocation().getId());
            source.getLocation().setChildren(sourceChildrens);

        }

        return ServiceResponseBuilder
                .<Boolean> ok()
                .withResult(LocationTreeUtils.isSublocationOf(source.getLocation(), locationToAuthorize))
                .build();
    }

    private <T> ServiceResponse<T> validate(Tenant tenant, Application application) {

        if (!Optional.ofNullable(tenant).isPresent()) {
            Device noDevice = Device.builder().guid("NULL").tenant(
                    Tenant.builder().domainName("unknow_domain").build()).build();
            LOGGER.debug(CommonValidations.TENANT_NULL.getCode(),
                    noDevice.toURI(),
                    noDevice.getTenant().getLogLevel());
            return ServiceResponseBuilder.<T>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode())
                    .build();
        }

        if (!Optional.ofNullable(application).isPresent()) {
            Device noDevice = Device.builder()
                    .guid("NULL")
                    .tenant(tenant)
                    .application(Application.builder().name("unknowapp").tenant(tenant).build())
                    .build();
            LOGGER.debug(ApplicationService.Validations.APPLICATION_NULL.getCode(),
                    noDevice.toURI(),
                    noDevice.getTenant().getLogLevel());
            return ServiceResponseBuilder.<T>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_NULL.getCode())
                    .build();
        }

        if (!tenantRepository.exists(tenant.getId())) {
            return ServiceResponseBuilder.<T>error()
                    .withMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode())
                    .build();
        }

        if (!applicationRepository.exists(application.getName())) {
            return ServiceResponseBuilder.<T>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode())
                    .build();
        }

        return ServiceResponseBuilder.<T>ok().build();

    }

}
