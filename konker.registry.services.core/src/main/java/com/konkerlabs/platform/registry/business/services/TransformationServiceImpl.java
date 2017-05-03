package com.konkerlabs.platform.registry.business.services;

import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.ApplicationRepository;
import com.konkerlabs.platform.registry.business.repositories.EventRouteRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.repositories.TransformationRepository;
import com.konkerlabs.platform.registry.business.services.api.AbstractURLBlacklistValidation;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.business.services.api.TransformationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class TransformationServiceImpl
        extends AbstractURLBlacklistValidation
        implements TransformationService {

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private TransformationRepository transformationRepository;
    @Autowired
    private EventRouteRepository eventRouteRepository;

    @Override
    public ServiceResponse<List<Transformation>> getAll(Tenant tenant, Application application) {

        if (!Optional.ofNullable(tenant).isPresent())
            return ServiceResponseBuilder.<List<Transformation>>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode()).build();

        if (!Optional.ofNullable(application).isPresent())
            return ServiceResponseBuilder.<List<Transformation>>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()).build();

        return ServiceResponseBuilder.<List<Transformation>>ok()
                .withResult(transformationRepository.findAllByApplicationId(tenant.getId(), application.getName())).build();
    }

    @Override
    public ServiceResponse<Transformation> register(Tenant tenant, Application application,
                                                    Transformation transformation) {
        if (!Optional.ofNullable(tenant).isPresent())
            return ServiceResponseBuilder.<Transformation>error().withMessage(CommonValidations.TENANT_NULL.getCode())
                    .build();

        if (!Optional.ofNullable(transformation).isPresent())
            return ServiceResponseBuilder.<Transformation>error().withMessage(CommonValidations.RECORD_NULL.getCode())
                    .build();

        if (!tenantRepository.exists(tenant.getId()))
            return ServiceResponseBuilder.<Transformation>error()
                    .withMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()).build();

        if (!Optional.ofNullable(application).isPresent())
            return ServiceResponseBuilder.<Transformation>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()).build();

        Application existingApplication = applicationRepository.findByTenantAndName(tenant.getId(), application.getName());

        if (!Optional.ofNullable(existingApplication).isPresent())
            return ServiceResponseBuilder.<Transformation>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_NOT_FOUND.getCode()).build();

        transformation.setTenant(tenant);
        transformation.setApplication(existingApplication);
        transformation.setGuid(UUID.randomUUID().toString());

        Optional<Map<String, Object[]>> validations = transformation.applyValidations();

        if (validations.isPresent())
            return ServiceResponseBuilder.<Transformation>error().withMessages(validations.get()).build();

        if (Optional.ofNullable(transformationRepository.findByName(
                tenant.getId(), application.getName(), transformation.getName()))
                .filter(transformations -> !transformations.isEmpty()).isPresent()) {
            return ServiceResponseBuilder.<Transformation>error()
                    .withMessage(Validations.TRANSFORMATION_NAME_IN_USE.getCode()).build();
        }

        Optional<Map<String, Object[]>> blacklistValidations =
                verifyIfUrlMatchesBlacklist(transformation.getSteps());

        if (blacklistValidations.isPresent())
            return ServiceResponseBuilder.<Transformation>error().withMessages(blacklistValidations.get()).build();

        Transformation saved = transformationRepository.save(transformation);

        LOGGER.info("Transformation created. Name: {}", saved.getName(), tenant.toURI(), tenant.getLogLevel());

        return ServiceResponseBuilder.<Transformation>ok().withResult(saved).build();
    }

    private Optional<Map<String, Object[]>> verifyIfUrlMatchesBlacklist(List<TransformationStep> uriList) {
        Optional<Map<String, Object[]>> result = null;
        for (TransformationStep uri : uriList) {

            result = verifyIfUrlMatchesBlacklist((String) uri.getAttributes().get("url"));

            if (result.isPresent())
                break;

        }

        return result;

    }

    @Override
    public ServiceResponse<Transformation> get(Tenant tenant, Application application, String guid) {

        if (!Optional.ofNullable(tenant).isPresent())
            return ServiceResponseBuilder.<Transformation>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode()).build();

        if (!Optional.ofNullable(application).isPresent())
            return ServiceResponseBuilder.<Transformation>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()).build();

        Transformation transformation =
                transformationRepository.findByTenantIdApplicationIdAndTransformationGuid(
                        tenant.getId(), guid, application.getName());

        if (transformation != null) {
            return ServiceResponseBuilder.<Transformation>ok()
                    .withResult(transformation).build();
        } else {
            return ServiceResponseBuilder.<Transformation>error()
                    .withMessage(Validations.TRANSFORMATION_NOT_FOUND.getCode()).build();
        }

    }

    @Override
    public ServiceResponse<Transformation> update(Tenant tenant, Application application,
                                                  String guid, Transformation transformation) {
        if (!Optional.ofNullable(tenant).isPresent())
            return ServiceResponseBuilder.<Transformation>error().withMessage(CommonValidations.TENANT_NULL.getCode())
                    .build();

        if (!Optional.ofNullable(transformation).isPresent())
            return ServiceResponseBuilder.<Transformation>error().withMessage(CommonValidations.RECORD_NULL.getCode())
                    .build();

        if (!tenantRepository.exists(tenant.getId()))
            return ServiceResponseBuilder.<Transformation>error()
                    .withMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()).build();

        if (!Optional.ofNullable(application).isPresent())
            return ServiceResponseBuilder.<Transformation>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()).build();

        Application existingApplication = applicationRepository.findByTenantAndName(tenant.getId(), application.getName());

        if (!Optional.ofNullable(existingApplication).isPresent())
            return ServiceResponseBuilder.<Transformation>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_NOT_FOUND.getCode()).build();

        Transformation fromDb = get(tenant, application, guid).getResult();

        if (!Optional.ofNullable(fromDb).isPresent())
            return ServiceResponseBuilder.<Transformation>error()
                    .withMessage(Validations.TRANSFORMATION_NOT_FOUND.getCode()).build();

        fromDb.setName(transformation.getName());
        fromDb.setDescription(transformation.getDescription());
        fromDb.setSteps(transformation.getSteps());

        Optional<Map<String, Object[]>> validations = fromDb.applyValidations();

        if (validations.isPresent())
            return ServiceResponseBuilder.<Transformation>error().withMessages(validations.get()).build();

        if (Optional.ofNullable(transformationRepository
                .findByName(fromDb.getTenant().getId(), application.getName(), fromDb.getName()))
                .filter(transformations -> !transformations.isEmpty()).orElseGet(ArrayList<Transformation>::new)
                .stream().anyMatch(transformation1 -> !transformation1.getId().equals(fromDb.getId()))) {
            return ServiceResponseBuilder.<Transformation>error()
                    .withMessage(Validations.TRANSFORMATION_NAME_IN_USE.getCode()).build();
        }

        Optional<Map<String, Object[]>> blacklistValidations = verifyIfUrlMatchesBlacklist(transformation.getSteps());

        if (blacklistValidations.isPresent())
            return ServiceResponseBuilder.<Transformation>error().withMessages(blacklistValidations.get()).build();

        Transformation saved = transformationRepository.save(fromDb);

        LOGGER.info("Transformation updated. Name: {}", saved.getName(), tenant.toURI(), tenant.getLogLevel());

        return ServiceResponseBuilder.<Transformation>ok().withResult(saved).build();
    }

    @Override
    public ServiceResponse<Transformation> remove(Tenant tenant, Application application, String transformationGuid) {

        if (!Optional.ofNullable(tenant).isPresent())
            return ServiceResponseBuilder.<Transformation>error().withMessage(CommonValidations.TENANT_NULL.getCode())
                    .build();

        if (!tenantRepository.exists(tenant.getId()))
            return ServiceResponseBuilder.<Transformation>error()
                    .withMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()).build();

        if (!Optional.ofNullable(application).isPresent())
            return ServiceResponseBuilder.<Transformation>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()).build();

        Application existingApplication = applicationRepository.findByTenantAndName(tenant.getId(), application.getName());

        if (!Optional.ofNullable(existingApplication).isPresent())
            return ServiceResponseBuilder.<Transformation>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_NOT_FOUND.getCode()).build();

        Transformation transformation = transformationRepository.findByGuid(transformationGuid);
        List<EventRoute> eventRoutes = Collections.emptyList();

        if(transformation != null) {
            eventRoutes = eventRouteRepository.findByTransformationId(tenant.getId(),
                    application.getName(),
                    transformation.getId());
        } else {
            return ServiceResponseBuilder.<Transformation>error()
                    .withMessage(Validations.TRANSFORMATION_NOT_FOUND.getCode()).build();
        }

        if (!eventRoutes.isEmpty()) {
            return ServiceResponseBuilder.<Transformation>error()
                    .withMessage(Validations.TRANSFORMATION_HAS_ROUTE.getCode()).build();
        }

        if (Optional.ofNullable(transformation).isPresent()
                && !transformation.getTenant().getId().equals(tenant.getId())) {
            return ServiceResponseBuilder.<Transformation>error()
                    .withMessage(Validations.TRANSFORMATION_BELONG_ANOTHER_TENANT.getCode()).build();
        }

        transformationRepository.delete(transformation);

        LOGGER.info("Transformation removed. Name: {}", transformation.getName(), tenant.toURI(), tenant.getLogLevel());

        return ServiceResponseBuilder.<Transformation>ok().build();
    }
}