package com.konkerlabs.platform.registry.api.web.controller;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.konkerlabs.platform.registry.api.exceptions.BadRequestResponseException;
import com.konkerlabs.platform.registry.api.exceptions.BadServiceResponseException;
import com.konkerlabs.platform.registry.api.exceptions.NotFoundResponseException;
import com.konkerlabs.platform.registry.api.model.EventVO;
import com.konkerlabs.platform.registry.api.model.EventsFilter;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@Scope("request")
@RequestMapping(value = "/{application}/outgoingEvents")
@Api(tags = "events")
public class OutgoingEventsRestController extends AbstractRestController implements InitializingBean  {

    @Autowired
    private DeviceEventService deviceEventService;

    private Set<String> validationsCode = new HashSet<>();

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_DEVICE_LOG')")
    @ApiOperation(
            value = "Search outgoing events",
            response = EventVO.class,
            notes = IncomingEventsRestController.SEARCH_NOTES,
            produces = "application/json"
            )
    public List<EventVO> list(
            @ApiParam(value = "Application ID", required = true)
            @PathVariable(value = "application") String applicationId,
            @ApiParam(value = "Query string", example = "deviceGuid:818599ad-3502-4e70-a852-fc7af8e0a9f4")
            @RequestParam(required = false, defaultValue = "", name = "q") String query,
            @ApiParam(value = "The sort order", allowableValues = "newest,oldest")
            @RequestParam(required = false, defaultValue = "newest") String sort,
            @ApiParam(value = "The number of results returned", allowableValues = "range[1, 10000]")
            @RequestParam(required = false, defaultValue = "100") Integer limit
        ) throws BadServiceResponseException, BadRequestResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        boolean ascending = false;
        if (sort.equalsIgnoreCase("oldest")) {
            ascending = true;
        }

        if (limit > 10000) {
            throw new BadRequestResponseException("Invalid limit. Max: 10000");
        }

        EventsFilter filter = new EventsFilter();
        filter.parse(query);
        String deviceGuid = filter.getDeviceGuid();
        String channel = filter.getChannel();
        Instant startingTimestamp = filter.getStartingTimestamp();
        Instant endTimestamp = filter.getEndTimestamp();

        ServiceResponse<List<Event>> restDestinationResponse = deviceEventService.findOutgoingBy(tenant, application, user.getParentUser(), deviceGuid, channel, startingTimestamp, endTimestamp, ascending, limit);

        if (!restDestinationResponse.isOk()) {
            throw new BadServiceResponseException( restDestinationResponse, validationsCode);
        } else {
            return new EventVO().apply(restDestinationResponse.getResult());
        }

    }

    @Override
    public void afterPropertiesSet() {
    	for (DeviceEventService.Validations value : DeviceEventService.Validations.values()) {
    		validationsCode.add(value.getCode());
    	}

    	for (ApplicationService.Validations value : ApplicationService.Validations.values()) {
    		validationsCode.add(value.getCode());
    	}
    }

}
