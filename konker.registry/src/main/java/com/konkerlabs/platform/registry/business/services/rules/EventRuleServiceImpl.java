package com.konkerlabs.platform.registry.business.services.rules;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.EventRule;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.repositories.EventRuleRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.rules.api.EventRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class EventRuleServiceImpl implements EventRuleService {

    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private EventRuleRepository eventRuleRepository;


    @Override
    public ServiceResponse<EventRule> save(Tenant tenant, EventRule rule) throws BusinessException {
        Optional.ofNullable(tenant)
            .orElseThrow(() -> new BusinessException("Tenant cannot be null"));
        Optional.ofNullable(rule)
            .orElseThrow(() -> new BusinessException("Record cannot be null"));
        Optional.ofNullable(tenantRepository.findOne(tenant.getId()))
            .orElseThrow(() -> new BusinessException("Tenant does not exist"));

        rule.setTenant(tenant);

        List<String> validations = rule.applyValidations();

        if (validations != null) {
            return ServiceResponse.<EventRule>builder()
                    .responseMessages(validations)
                    .status(ServiceResponse.Status.ERROR).<EventRule>build();
        }

//        String incomingChannel = rule.getIncoming().getData().get("channel");
//        String outgoingChannel = rule.getOutgoing().getData().get("channel");
//
//        if (incomingChannel != null && outgoingChannel != null && incomingChannel.equals(outgoingChannel)) {
//            return ServiceResponse.<EventRule>builder()
//                    .responseMessages(Arrays.asList(new String[] { "Incoming and outgoing device channels cannot be the same" }))
//                    .status(ServiceResponse.Status.ERROR).<EventRule>build();
//        }

        //TODO Validate rule's filter expression language.

        EventRule saved = eventRuleRepository.save(rule);

        return ServiceResponse.<EventRule>builder().status(ServiceResponse.Status.OK).result(saved).<EventRule>build();
    }

    @Override
    public List<EventRule> getAll(Tenant tenant) {
        return eventRuleRepository.findAllByTenant(tenant.getId());
    }

    @Override
    public ServiceResponse<EventRule> getById(Tenant tenant, String id) {
        try {
            Optional.ofNullable(id).orElseThrow(() -> new BusinessException("Id cannot be null"));
            Optional.ofNullable(tenant).orElseThrow(() -> new BusinessException("Tenant cannot be null"));

            Tenant t = Optional.ofNullable(tenantRepository.findByName(tenant.getName()))
                    .orElseThrow(() -> new BusinessException("Tenant does not exist"));

            EventRule rule = Optional.ofNullable(eventRuleRepository.findByTenantIdAndRuleId(t.getId(), id))
                    .orElseThrow(() -> new BusinessException("Event Rule does not exist"));

            return ServiceResponse.<EventRule> builder().status(ServiceResponse.Status.OK).result(rule)
                    .<EventRule>build();
        } catch (BusinessException be) {
            return ServiceResponse.<EventRule> builder().status(ServiceResponse.Status.ERROR)
                    .responseMessage(be.getMessage()).<EventRule>build();
        }
    }

    @Override
    public List<EventRule> findByIncomingUri(URI uri) {
        return eventRuleRepository.findByIncomingURI(uri);
    }
}
