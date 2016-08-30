package com.konkerlabs.platform.registry.business.services;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.SmsDestination;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.SmsDestinationRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.NewServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.business.services.api.SmsDestinationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class SmsDestinationServiceImpl implements SmsDestinationService {

    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private SmsDestinationRepository smsDestinationRepository;

    @Override
    public NewServiceResponse<List<SmsDestination>> findAll(Tenant tenant) {
        try {
            Optional.ofNullable(tenant).orElseThrow(() -> new BusinessException(CommonValidations.TENANT_NULL.getCode()));

            Optional.ofNullable(tenantRepository.findByDomainName(tenant.getDomainName()))
                    .orElseThrow(() -> new BusinessException(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()));

            List<SmsDestination> smsList = smsDestinationRepository.findAllByTenant(tenant.getId());

            return ServiceResponseBuilder.<List<SmsDestination>>ok().withResult(smsList)
                    .<List<SmsDestination>>build();
        } catch (BusinessException be) {
            return ServiceResponseBuilder.<List<SmsDestination>>error().withMessage(be.getMessage())
                    .<List<SmsDestination>>build();
        }
    }

    @Override
    public NewServiceResponse<SmsDestination> register(Tenant tenant, SmsDestination destination) {
        try {
            Optional.ofNullable(tenant).orElseThrow(() -> new BusinessException(CommonValidations.TENANT_NULL.getCode()));
            Optional.ofNullable(destination)
                    .orElseThrow(() -> new BusinessException(CommonValidations.RECORD_NULL.getCode()));

            Tenant savedTenant = tenantRepository.findByDomainName(tenant.getDomainName());
            Optional.ofNullable(savedTenant).orElseThrow(() -> new BusinessException(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()));

            if (smsDestinationRepository.getByTenantAndName(savedTenant.getId(), destination.getName()) != null) {
                throw new BusinessException(Validations.SMSDEST_NAME_UNIQUE.getCode());
            }

            destination.setId(null);
            destination.setTenant(savedTenant);
            destination.setGuid(UUID.randomUUID().toString());

            Optional<Map<String, Object[]>> validations = destination.applyValidations();
            if (validations.isPresent()) {
                    return ServiceResponseBuilder.<SmsDestination>error().withMessages(validations.get()).<SmsDestination>build();
            }

            SmsDestination saved = smsDestinationRepository.save(destination);

            return ServiceResponseBuilder.<SmsDestination>ok().withResult(saved)
                    .<SmsDestination>build();
        } catch (BusinessException be) {
            return ServiceResponseBuilder.<SmsDestination>error().withMessage(be.getMessage())
                    .<SmsDestination>build();
        }
    }

    @Override
    public NewServiceResponse<SmsDestination> update(Tenant tenant, String guid, SmsDestination destination) {
        try {
            Optional.ofNullable(tenant).orElseThrow(() -> new BusinessException(CommonValidations.TENANT_NULL.getCode()));
            Optional.ofNullable(destination)
                    .orElseThrow(() -> new BusinessException(CommonValidations.RECORD_NULL.getCode()));
            Optional.ofNullable(guid).orElseThrow(() -> new BusinessException(Validations.SMSDEST_ID_NULL.getCode()));

            Tenant savedTenant = tenantRepository.findByDomainName(tenant.getDomainName());
            Optional.ofNullable(savedTenant).orElseThrow(() -> new BusinessException(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()));

            SmsDestination byName = smsDestinationRepository.getByTenantAndName(savedTenant.getId(), destination.getName());
            if (!guid.equals(Optional.ofNullable(byName).map(SmsDestination::getGuid).orElse(guid))) {
                throw new BusinessException(Validations.SMSDEST_NAME_UNIQUE.getCode());
            }

            SmsDestination old = smsDestinationRepository.getByTenantAndGUID(savedTenant.getId(), guid);
            Optional.ofNullable(old).orElseThrow(() -> new BusinessException(Validations.SMSDEST_NOT_FOUND.getCode()));

            destination.setId(old.getId());
            destination.setGuid(old.getGuid());
            destination.setTenant(tenant);

            Optional<Map<String, Object[]>> validations = destination.applyValidations();
            if (validations.isPresent()) {
                    return ServiceResponseBuilder.<SmsDestination>error().withMessages(validations.get()).<SmsDestination>build();
            }

            SmsDestination saved = smsDestinationRepository.save(destination);

            return ServiceResponseBuilder.<SmsDestination>ok().withResult(saved).<SmsDestination>build();
        } catch (BusinessException be) {
            return ServiceResponseBuilder.<SmsDestination>error().withMessage(be.getMessage()).<SmsDestination>build();
        }
    }

    @Override
    public NewServiceResponse<SmsDestination> getByGUID(Tenant tenant, String guid) {
        try {
            Optional.ofNullable(tenant).orElseThrow(() -> new BusinessException(CommonValidations.TENANT_NULL.getCode()));
            Optional.ofNullable(guid).orElseThrow(() -> new BusinessException(Validations.SMSDEST_ID_NULL.getCode()));

            SmsDestination smsDestination = Optional.ofNullable(smsDestinationRepository.getByTenantAndGUID(tenant.getId(), guid))
                    .orElseThrow(() -> new BusinessException(Validations.SMSDEST_NOT_FOUND.getCode()));

            return ServiceResponseBuilder.<SmsDestination>ok().withResult(smsDestination)
                    .<SmsDestination>build();
        } catch (BusinessException be) {
            return ServiceResponseBuilder.<SmsDestination>error().withMessage(be.getMessage())
                    .<SmsDestination>build();
        }
    }
}
