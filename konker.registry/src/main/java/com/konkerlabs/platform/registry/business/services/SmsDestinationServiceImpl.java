package com.konkerlabs.platform.registry.business.services;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.SmsDestination;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.repositories.SmsDestinationRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.SmsDestinationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SmsDestinationServiceImpl implements SmsDestinationService {

    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private SmsDestinationRepository smsDestinationRepository;

    @Override
    public ServiceResponse<List<SmsDestination>> findAll(Tenant tenant) {
        try {
            Optional.ofNullable(tenant).orElseThrow(() -> new BusinessException("Tenant cannot be null"));

            Optional.ofNullable(tenantRepository.findByDomainName(tenant.getDomainName()))
                    .orElseThrow(() -> new BusinessException("Tenant does not exist"));

            List<SmsDestination> smsList = smsDestinationRepository.findAllByTenant(tenant.getId());

            return ServiceResponse.<List<SmsDestination>>builder().result(smsList).status(ServiceResponse.Status.OK)
                    .<List<SmsDestination>>build();
        } catch (BusinessException be) {
            return ServiceResponse.<List<SmsDestination>>builder().responseMessage(be.getMessage())
                    .status(ServiceResponse.Status.ERROR).<List<SmsDestination>>build();
        }
    }

    @Override
    public ServiceResponse<SmsDestination> register(Tenant tenant, SmsDestination destination) {
        try {
            Optional.ofNullable(tenant).orElseThrow(() -> new BusinessException("Tenant cannot be null"));
            Optional.ofNullable(destination)
                    .orElseThrow(() -> new BusinessException("SMS Destination cannot be null"));

            Tenant savedTenant = tenantRepository.findByDomainName(tenant.getDomainName());
            Optional.ofNullable(savedTenant).orElseThrow(() -> new BusinessException("Tenant does not exist"));

            if (smsDestinationRepository.getByTenantAndName(savedTenant.getId(), destination.getName()) != null) {
                throw new BusinessException("Name already exists");
            }

            destination.setId(null);
            destination.setTenant(savedTenant);
            destination.setGuid(UUID.randomUUID().toString());

            List<String> validations = Optional.ofNullable(destination.applyValidations())
                    .orElse(Collections.emptyList());
            if (!validations.isEmpty()) {
                return ServiceResponse.<SmsDestination>builder().responseMessages(validations)
                        .status(ServiceResponse.Status.ERROR).<SmsDestination>build();
            }

            SmsDestination saved = smsDestinationRepository.save(destination);

            return ServiceResponse.<SmsDestination>builder().result(saved).status(ServiceResponse.Status.OK)
                    .<SmsDestination>build();
        } catch (BusinessException be) {
            return ServiceResponse.<SmsDestination>builder().responseMessage(be.getMessage())
                    .status(ServiceResponse.Status.ERROR).<SmsDestination>build();
        }
    }

    @Override
    public ServiceResponse<SmsDestination> update(Tenant tenant, String guid, SmsDestination destination) {
        try {
            Optional.ofNullable(tenant).orElseThrow(() -> new BusinessException("Tenant cannot be null"));
            Optional.ofNullable(destination)
                    .orElseThrow(() -> new BusinessException("SMS Destination cannot be null"));
            Optional.ofNullable(guid).orElseThrow(() -> new BusinessException("SMS Destination ID cannot be null"));

            Tenant savedTenant = tenantRepository.findByDomainName(tenant.getDomainName());
            Optional.ofNullable(savedTenant).orElseThrow(() -> new BusinessException("Tenant does not exist"));

            SmsDestination byName = smsDestinationRepository.getByTenantAndName(savedTenant.getId(), destination.getName());
            if (!guid.equals(Optional.ofNullable(byName).map(SmsDestination::getGuid).orElse(guid))) {
                throw new BusinessException("SMS Destination Name already exists");
            }

            SmsDestination old = smsDestinationRepository.getByTenantAndGUID(savedTenant.getId(), guid);
            Optional.ofNullable(old).orElseThrow(() -> new BusinessException("SMS Destination does not exist"));

            destination.setId(old.getId());
            destination.setGuid(old.getGuid());
            destination.setTenant(tenant);

            List<String> validations = Optional.ofNullable(destination.applyValidations())
                    .orElse(Collections.emptyList());
            if (!validations.isEmpty()) {
                return ServiceResponse.<SmsDestination> builder().responseMessages(validations)
                        .status(ServiceResponse.Status.ERROR).<SmsDestination>build();
            }

            SmsDestination saved = smsDestinationRepository.save(destination);

            return ServiceResponse.<SmsDestination> builder().result(saved).status(ServiceResponse.Status.OK)
                    .<SmsDestination>build();
        } catch (BusinessException be) {
            return ServiceResponse.<SmsDestination> builder().responseMessage(be.getMessage())
                    .status(ServiceResponse.Status.ERROR).<SmsDestination>build();
        }
    }

    @Override
    public ServiceResponse<SmsDestination> getByGUID(Tenant tenant, String guid) {
        try {
            Optional.ofNullable(tenant).orElseThrow(() -> new BusinessException("Tenant cannot be null"));
            Optional.ofNullable(guid).orElseThrow(() -> new BusinessException("SMS Destination ID cannot be null"));

            SmsDestination smsDestination = Optional.ofNullable(smsDestinationRepository.getByTenantAndGUID(tenant.getId(), guid))
                    .orElseThrow(() -> new BusinessException("SMS Destination does not exist"));

            return ServiceResponse.<SmsDestination>builder().result(smsDestination).status(ServiceResponse.Status.OK)
                    .<SmsDestination>build();
        } catch (BusinessException be) {
            return ServiceResponse.<SmsDestination>builder().responseMessage(be.getMessage())
                    .status(ServiceResponse.Status.ERROR).<SmsDestination>build();
        }
    }
}
