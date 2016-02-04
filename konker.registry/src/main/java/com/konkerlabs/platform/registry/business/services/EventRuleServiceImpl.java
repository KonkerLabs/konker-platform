package com.konkerlabs.platform.registry.business.services;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.EventRule;
import com.konkerlabs.platform.registry.business.repositories.EventRuleRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.EventRuleService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class EventRuleServiceImpl implements EventRuleService {

    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private EventRuleRepository eventRuleRepository;

    @Override
    public ServiceResponse create(EventRule rule) throws BusinessException {
        if (rule == null)
            throw new BusinessException("Record cannot be null");

        rule.setTenant(tenantRepository.findByName("Konker"));

        if (rule.getTenant() == null) {
            return ServiceResponse.builder()
                    .responseMessages(Arrays.asList(new String[] { "Default tenant does not exist" }))
                    .status(ServiceResponse.Status.ERROR).build();
        }

        List<String> validations = rule.applyValidations();

        if (validations != null) {
            return ServiceResponse.builder().responseMessages(validations).status(ServiceResponse.Status.ERROR).build();
        }

        eventRuleRepository.save(rule);

        return ServiceResponse.builder().status(ServiceResponse.Status.OK).build();
    }

    @Override
    public List<EventRule> getAll() {
        return eventRuleRepository.findAll();
    }

    @Override
    public EventRule findById(String id) {
        return eventRuleRepository.findOne(id);
    }
}
