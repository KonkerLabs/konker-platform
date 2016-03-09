package com.konkerlabs.platform.registry.business.services;

import com.konkerlabs.platform.registry.business.model.DataEnrichmentExtension;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.Transformation;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.repositories.TransformationRepository;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.TransformationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class TransformationServiceImpl implements TransformationService {

    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private TransformationRepository transformationRepository;

    @Override
    public ServiceResponse<List<Transformation>> getAll(Tenant tenant) {
        return ServiceResponse.<List<Transformation>>builder()
            .status(ServiceResponse.Status.OK)
            .result(transformationRepository.findAllByTenantId(tenant.getId())).<List<Transformation>>build();
    }

    @Override
    public ServiceResponse<Transformation> register(Tenant tenant, Transformation transformation) {
        if (!Optional.ofNullable(tenant).isPresent())
            return ServiceResponse.<Transformation>builder()
                    .status(ServiceResponse.Status.ERROR)
                    .responseMessage("Tenant cannot be null").<Transformation>build();

        if (!Optional.ofNullable(transformation).isPresent())
            return ServiceResponse.<Transformation>builder()
                    .status(ServiceResponse.Status.ERROR)
                    .responseMessage("Transformation cannot be null").<Transformation>build();

        if (!tenantRepository.exists(tenant.getId()))
            return ServiceResponse.<Transformation>builder()
                    .status(ServiceResponse.Status.ERROR)
                    .responseMessage("Tenant does not exist").<Transformation>build();

        transformation.setTenant(tenant);

        Set<String> validations = transformation.applyValidation();

        if (Optional.ofNullable(transformationRepository.findByName(transformation.getName()))
                .filter(transformations -> !transformations.isEmpty()).isPresent())
            validations.add("Transformation name is already in use");

        if (Optional.of(validations).filter(set -> !set.isEmpty()).isPresent())
            return ServiceResponse.<Transformation>builder()
                    .status(ServiceResponse.Status.ERROR)
                    .responseMessages(validations.stream().collect(Collectors.toList())).<Transformation>build();

        Transformation saved = transformationRepository.save(transformation);

        return ServiceResponse.<Transformation>builder()
                .status(ServiceResponse.Status.OK)
                .result(saved).<Transformation>build();
    }

    @Override
    public ServiceResponse<Transformation> get(Tenant tenant, String id) {
        return ServiceResponse.<Transformation>builder()
                .status(ServiceResponse.Status.OK)
                .result(transformationRepository.findByTenantIdAndTransformationId(tenant.getId(),id))
                .<Transformation>build();
    }

    @Override
    public ServiceResponse<Transformation> update(Tenant tenant, String id, Transformation transformation) {
        if (!Optional.ofNullable(tenant).isPresent())
            return ServiceResponse.<Transformation>builder()
                    .status(ServiceResponse.Status.ERROR)
                    .responseMessage("Tenant cannot be null").<Transformation>build();

        if (!Optional.ofNullable(transformation).isPresent())
            return ServiceResponse.<Transformation>builder()
                    .status(ServiceResponse.Status.ERROR)
                    .responseMessage("Transformation cannot be null").<Transformation>build();

        if (!tenantRepository.exists(tenant.getId()))
            return ServiceResponse.<Transformation>builder()
                    .status(ServiceResponse.Status.ERROR)
                    .responseMessage("Tenant does not exist").<Transformation>build();

        Transformation fromDb = get(tenant, id).getResult();

        if (!Optional.ofNullable(fromDb).isPresent())
            return ServiceResponse.<Transformation>builder()
                    .status(ServiceResponse.Status.ERROR)
                    .responseMessage("Transformation not found").<Transformation>build();

        fromDb.setName(transformation.getName());
        fromDb.setDescription(transformation.getDescription());
        fromDb.setSteps(transformation.getSteps());

        Set<String> validations = fromDb.applyValidation();

        if (Optional.ofNullable(transformationRepository.findByName(fromDb.getName()))
                .filter(transformations -> !transformations.isEmpty())
                .orElseGet(ArrayList<Transformation>::new)
                .stream().anyMatch(transformation1 -> !transformation1.getId().equals(fromDb.getId())))
            validations.add("Transformation name is already in use");

        if (Optional.of(validations).filter(set -> !set.isEmpty()).isPresent())
            return ServiceResponse.<Transformation>builder()
                    .status(ServiceResponse.Status.ERROR)
                    .responseMessages(validations.stream().collect(Collectors.toList())).<Transformation>build();

        Transformation saved = transformationRepository.save(fromDb);

        return ServiceResponse.<Transformation>builder()
                .status(ServiceResponse.Status.OK)
                .result(saved).<Transformation>build();
    }
}