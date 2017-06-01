package com.konkerlabs.platform.registry.api.web.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.konkerlabs.platform.registry.api.exceptions.BadServiceResponseException;
import com.konkerlabs.platform.registry.api.exceptions.NotFoundResponseException;
import com.konkerlabs.platform.registry.api.model.SilenceTriggerVO;
import com.konkerlabs.platform.registry.business.model.AlertTrigger;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.RestDestination.Validations;
import com.konkerlabs.platform.registry.business.model.SilenceTrigger;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.AlertTriggerService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Scope("request")
@RequestMapping(value = "/{application}/triggers")
@Api(tags = "alert triggers")
public class AlertTriggerRestController extends AbstractRestController implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlertTriggerRestController.class);

    @Autowired
    private AlertTriggerService alertTriggerService;

    private Set<String> validationsCode = new HashSet<>();

    @GetMapping(path = "/")
    // @PreAuthorize("hasAuthority('LIST_ALERT_TRIGGERS')")
    @ApiOperation(
            value = "List all triggers by application",
            response = SilenceTriggerVO.class)
    public List<Object> list(@PathVariable("application") String applicationId) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        ServiceResponse<List<AlertTrigger>> restDestinationResponse = alertTriggerService.listByTenantAndApplication(tenant, application);

        if (!restDestinationResponse.isOk()) {
            throw new BadServiceResponseException(user, restDestinationResponse, validationsCode);
        } else {
            List<Object> alertTriggersVO = new ArrayList<>(restDestinationResponse.getResult().size());

            for (AlertTrigger alertTrigger: restDestinationResponse.getResult()) {
                if (alertTrigger.getClass().equals(SilenceTrigger.class)) {
                    alertTriggersVO.add(new SilenceTriggerVO((SilenceTrigger) alertTrigger));
                } else {
                    LOGGER.warn("Invalid class: {}", alertTrigger.getClass().getName());
                }
            }

            return alertTriggersVO;
        }

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        for (Validations value : Validations.values()) {
            validationsCode.add(value.getCode());
        }
    }

}
