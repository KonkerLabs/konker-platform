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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

            if (repository.findByTenantIdAndName(t.getId(), dee.getName()) != null) {
                throw new BusinessException("Data Enrichment Extension Name must be unique");
            }

            dee.setTenant(tenant);
            dee.setId(null);

            List<String> validationErrors = Optional.ofNullable(dee.applyValidations()).orElse(Collections.emptyList());

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

            dee.setTenant(tenant);
            dee.setId(null);
            DataEnrichmentExtension saved = repository.save(dee);

            return ServiceResponse.<DataEnrichmentExtension>builder().status(ServiceResponse.Status.OK).result(saved)
                    .<DataEnrichmentExtension>build();

        } catch (BusinessException be) {
            return ServiceResponse.<DataEnrichmentExtension> builder().status(ServiceResponse.Status.ERROR)
                    .responseMessage(be.getMessage()).<DataEnrichmentExtension>build();
        }
    }

    @Override
    public ServiceResponse<DataEnrichmentExtension> update(Tenant tenant, DataEnrichmentExtension dee) {

        try {
            Optional.ofNullable(dee)
                    .orElseThrow(() -> new BusinessException("Data Enrichment Extension cannot be null"));
            Optional.ofNullable(tenant).orElseThrow(() -> new BusinessException("Tenant cannot be null"));

            Tenant t = Optional.ofNullable(tenantRepository.findByName(tenant.getName()))
                    .orElseThrow(() -> new BusinessException("Tenant does not exist"));

            DataEnrichmentExtension oldDee = Optional
                    .ofNullable(repository.findByTenantIdAndName(t.getId(), dee.getName()))
                    .orElseThrow(() -> new BusinessException("Data Enrichment Extension does not exist"));

            dee.setTenant(tenant);

            List<String> validationErrors = Optional.ofNullable(dee.applyValidations()).orElse(Collections.emptyList());

            boolean isContainerKeyInUse = Optional.of(
                repository.findByTenantIdAndIncomingAndContainerKey(
                        tenant.getId(),
                        dee.getIncoming(),
                        dee.getContainerKey()
                )
            ).filter(dataEnrichmentExtensions -> !dataEnrichmentExtensions.isEmpty())
            .orElseGet(ArrayList<DataEnrichmentExtension>::new).stream()
                .anyMatch(currentDee -> !currentDee.getId().equals(oldDee.getId()));

            if (isContainerKeyInUse)
                validationErrors.add("Container key already registered for incoming device");

            if (!validationErrors.isEmpty()) {
                return ServiceResponse.<DataEnrichmentExtension>builder().status(ServiceResponse.Status.ERROR)
                        .responseMessages(validationErrors).<DataEnrichmentExtension>build();
            }

            dee.setId(oldDee.getId());
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
    public ServiceResponse<DataEnrichmentExtension>getByName(Tenant tenant, String name) {
        try {
            Optional.ofNullable(name).orElseThrow(() -> new BusinessException("Name cannot be null"));
            Optional.ofNullable(tenant).orElseThrow(() -> new BusinessException("Tenant cannot be null"));

            Tenant t = Optional.ofNullable(tenantRepository.findByName(tenant.getName()))
                    .orElseThrow(() -> new BusinessException("Tenant does not exist"));

            DataEnrichmentExtension dee = Optional.ofNullable(repository.findByTenantIdAndName(t.getId(), name))
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

}
