package com.konkerlabs.platform.registry.api.web.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.konkerlabs.platform.registry.api.exceptions.BadServiceResponseException;
import com.konkerlabs.platform.registry.api.model.EventVO;
import com.konkerlabs.platform.registry.api.model.RestDestinationVO;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.RestDestination.Validations;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.api.RestDestinationService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Scope("request")
@RequestMapping(value = "/outgoingEvents")
@Api(tags = "events")
public class OutgoingEventsController implements InitializingBean {

    @Autowired
    private DeviceEventService deviceEventService;

    @Autowired
    private User user;

    private Set<String> validationsCode = new HashSet<>();

    @GetMapping(path = "/")
    @PreAuthorize("hasAuthority('VIEW_DEVICE_LOG')")
    @ApiOperation(
            value = "List all rest destinations by organization",
            response = RestDestinationVO.class)
    public List<EventVO> list() throws BadServiceResponseException {

        Tenant tenant = user.getTenant();

        ServiceResponse<List<Event>> restDestinationResponse = deviceEventService.findIncomingBy(tenant, null, null, null, null, false, 3);

        if (!restDestinationResponse.isOk()) {
            throw new BadServiceResponseException(user, restDestinationResponse, validationsCode);
        } else {
            return new EventVO().apply(restDestinationResponse.getResult());
        }

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        for (Validations value : Validations.values()) {
            validationsCode.add(value.getCode());
        }

        for (RestDestinationService.Validations value : RestDestinationService.Validations.values()) {
            validationsCode.add(value.getCode());
        }

    }

}
