package com.konkerlabs.platform.registry.business.services;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.DataEnrichmentExtension;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.repositories.DataEnrichmentExtensionRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.DataEnrichmentExtensionService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.*;

@Service
public class DataEnrichmentExtensionServiceImpl implements DataEnrichmentExtensionService {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private DataEnrichmentExtensionRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

//    private boolean isContainerKeyRegisteredFor(Tenant tenant, URI incoming, String containerKey) {
//        return mongoTemplate.exists(
//            new Query(
//                Criteria.where("tenant.id").is(tenant.getId())
//                .andOperator(
//                    Criteria.where("incoming").is(incoming),
//                    Criteria.where("containerKey").is(containerKey)
//                )
//            ), DataEnrichmentExtension.class
//        );
//    }

    @Override
    public ServiceResponse<DataEnrichmentExtension> register(Tenant tenant, DataEnrichmentExtension dee) {
        try {
            Optional.ofNullable(dee)
                    .orElseThrow(() -> new BusinessException("Data Enrichment Extension cannot be null"));
            Optional.ofNullable(tenant).orElseThrow(() -> new BusinessException("Tenant cannot be null"));

            Tenant t = Optional.ofNullable(tenantRepository.findByName(tenant.getName()))
                    .orElseThrow(() -> new BusinessException("Tenant does not exist"));

            List<String> validationErrors = new ArrayList<>();

            if (repository.findByTenantIdAndName(t.getId(), dee.getName()) != null) {
                validationErrors.add("Data Enrichment Extension Name must be unique");
            }

            dee.setTenant(tenant);
            dee.setId(null);
            dee.setGuid(UUID.randomUUID().toString());

            validationErrors = Optional.ofNullable(dee.applyValidations()).filter(de -> !de.isEmpty()).orElse(validationErrors);

            if (!repository.findByTenantIdAndIncomingAndContainerKey(
                tenant.getId(),
                dee.getIncoming(),
                dee.getContainerKey()
                ).isEmpty())
                validationErrors.add("Container key already registered for incoming device");

            if (!validationErrors.isEmpty()) {
                return ServiceResponse.<DataEnrichmentExtension>builder().status(ServiceResponse.Status.ERROR)
                        .responseMessages(validationErrors).<DataEnrichmentExtension>build();
            }

            DataEnrichmentExtension saved = repository.save(dee);

            return ServiceResponse.<DataEnrichmentExtension>builder().status(ServiceResponse.Status.OK).result(saved)
                    .<DataEnrichmentExtension>build();

        } catch (BusinessException be) {
            return ServiceResponse.<DataEnrichmentExtension> builder().status(ServiceResponse.Status.ERROR)
                    .responseMessage(be.getMessage()).<DataEnrichmentExtension>build();
        }
    }

    @Override
    public ServiceResponse<DataEnrichmentExtension> update(Tenant tenant, String uuid, DataEnrichmentExtension dee) {

        try {
            Optional.ofNullable(dee)
                    .orElseThrow(() -> new BusinessException("Data Enrichment Extension cannot be null"));
            Optional.ofNullable(tenant).orElseThrow(() -> new BusinessException("Tenant cannot be null"));

            Tenant t = Optional.ofNullable(tenantRepository.findByName(tenant.getName()))
                    .orElseThrow(() -> new BusinessException("Tenant does not exist"));

            DataEnrichmentExtension oldDee = Optional
                    .ofNullable(repository.findByTenantIdAndGUID(t.getId(), uuid))
                    .orElseThrow(() -> new BusinessException("Data Enrichment Extension does not exist"));

            dee.setTenant(tenant);

            List<String> validationErrors = Optional.ofNullable(dee.applyValidations()).orElse(Collections.emptyList());

            DataEnrichmentExtension tenantIdAndName = repository.findByTenantIdAndName(t.getId(), dee.getName());

            if (tenantIdAndName != null && !tenantIdAndName.getGuid().equals(oldDee.getGuid()))
                validationErrors.add("Data Enrichment Extension Name must be unique");

            boolean isContainerKeyInUse = Optional.of(
                repository.findByTenantIdAndIncomingAndContainerKey(
                        tenant.getId(),
                        dee.getIncoming(),
                        dee.getContainerKey()
                )
            ).filter(dataEnrichmentExtensions -> !dataEnrichmentExtensions.isEmpty())
            .orElseGet(ArrayList<DataEnrichmentExtension>::new).stream()
                .anyMatch(currentDee -> !currentDee.getGuid().equals(oldDee.getGuid()));

            if (isContainerKeyInUse)
                validationErrors.add("Container key already registered for incoming device");

            if (!validationErrors.isEmpty()) {
                return ServiceResponse.<DataEnrichmentExtension>builder().status(ServiceResponse.Status.ERROR)
                        .responseMessages(validationErrors).<DataEnrichmentExtension>build();
            }

            dee.setId(oldDee.getId());
            dee.setGuid(oldDee.getGuid());
            DataEnrichmentExtension saved = repository.save(dee);

            return ServiceResponse.<DataEnrichmentExtension> builder().status(ServiceResponse.Status.OK)
                    .result(Optional.ofNullable(saved).orElseThrow(() -> new BusinessException("Could no save")))
                    .<DataEnrichmentExtension>build();
        } catch (BusinessException be) {
            return ServiceResponse.<DataEnrichmentExtension> builder().status(ServiceResponse.Status.ERROR)
                    .responseMessage(be.getMessage()).<DataEnrichmentExtension>build();
        }

    }

    @Override
    public ServiceResponse<List<DataEnrichmentExtension>> getAll(Tenant tenant) {
        try {

            Optional.ofNullable(tenant).orElseThrow(() -> new BusinessException("Tenant cannot be null"));

            Tenant t = Optional.ofNullable(tenantRepository.findByName(tenant.getName()))
                    .orElseThrow(() -> new BusinessException("Tenant does not exist"));

            List<DataEnrichmentExtension> l = repository.findAllByTenantId(t.getId());

            return ServiceResponse.<List<DataEnrichmentExtension>>builder().status(ServiceResponse.Status.OK).result(l)
                    .<List<DataEnrichmentExtension>>build();

        } catch (BusinessException be) {
            return ServiceResponse.<List<DataEnrichmentExtension>> builder().status(ServiceResponse.Status.ERROR)
                    .responseMessage(be.getMessage()).<List<DataEnrichmentExtension>>build();
        }
    }

    @Override
    public ServiceResponse<DataEnrichmentExtension> getByGUID(Tenant tenant, String guid) {
        try {
            Optional.ofNullable(guid).orElseThrow(() -> new BusinessException("ID cannot be null"));
            Optional.ofNullable(tenant).orElseThrow(() -> new BusinessException("Tenant cannot be null"));

            Tenant t = Optional.ofNullable(tenantRepository.findByName(tenant.getName()))
                    .orElseThrow(() -> new BusinessException("Tenant does not exist"));

            DataEnrichmentExtension dee = Optional.ofNullable(repository.findByTenantIdAndGUID(t.getId(), guid))
                    .orElseThrow(() -> new BusinessException("Data Enrichment Extension does not exist"));

            return ServiceResponse.<DataEnrichmentExtension> builder().status(ServiceResponse.Status.OK).result(dee)
                    .<DataEnrichmentExtension>build();
        } catch (BusinessException be) {
            return ServiceResponse.<DataEnrichmentExtension> builder().status(ServiceResponse.Status.ERROR)
                    .responseMessage(be.getMessage()).<DataEnrichmentExtension>build();
        }
    }

    @Override
    public ServiceResponse<List<DataEnrichmentExtension>> getByTenantAndByIncomingURI(Tenant tenant, URI incomingUri) {
        try {
            Optional.ofNullable(incomingUri).orElseThrow(() -> new BusinessException("Incoming URI cannot be null"));
            Optional.ofNullable(tenant).orElseThrow(() -> new BusinessException("Tenant cannot be null"));

            Tenant t = Optional.ofNullable(tenantRepository.findByName(tenant.getName()))
                    .orElseThrow(() -> new BusinessException("Tenant does not exist"));

            List<DataEnrichmentExtension> l = repository.findByTenantIdAndIncoming(t.getId(), incomingUri);

            return ServiceResponse.<List<DataEnrichmentExtension>> builder().status(ServiceResponse.Status.OK).result(l)
                    .<List<DataEnrichmentExtension>>build();

        } catch (BusinessException be) {
            return ServiceResponse.<List<DataEnrichmentExtension>> builder().status(ServiceResponse.Status.ERROR)
                    .responseMessage(be.getMessage()).<List<DataEnrichmentExtension>>build();
        }
    }

    @Override
    public ServiceResponse<DataEnrichmentExtension> remove(Tenant tenant, String guid) {
        if (!Optional.ofNullable(tenant).isPresent())
            return invalidServiceResponse("Tenant cannot be null").<DataEnrichmentExtension>build();
        if (!Optional.ofNullable(tenantRepository.findByName(tenant.getName())).isPresent())
            return invalidServiceResponse("Tenant does not exist").<DataEnrichmentExtension>build();
        if (!Optional.ofNullable(guid).filter(s -> !s.isEmpty()).isPresent())
            return invalidServiceResponse("GUID cannot be null or empty").<DataEnrichmentExtension>build();

        DataEnrichmentExtension route = repository.findByTenantIdAndGUID(tenant.getId(), guid);

        if (!Optional.ofNullable(route).isPresent())
            return invalidServiceResponse("Enrichment does not exist").<DataEnrichmentExtension>build();

        repository.delete(route);

        return ServiceResponse.<DataEnrichmentExtension>builder()
                .status(ServiceResponse.Status.OK)
                .result(route)
                .<DataEnrichmentExtension>build();
    }

    private ServiceResponse.ServiceResponseBuilder invalidServiceResponse(String... errors) {
        ServiceResponse.ServiceResponseBuilder invalidBuilder = ServiceResponse.builder()
                .status(ServiceResponse.Status.ERROR);
        for (String error : errors)
            invalidBuilder.responseMessage(error);

        invalidBuilder.responseMessages(Arrays.asList(errors));

        return invalidBuilder;
    }
}
