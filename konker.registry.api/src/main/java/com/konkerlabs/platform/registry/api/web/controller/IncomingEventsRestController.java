package com.konkerlabs.platform.registry.api.web.controller;

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
import com.konkerlabs.platform.registry.api.model.EventVO;
import com.konkerlabs.platform.registry.api.model.EventsFilter;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.RestDestination.Validations;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.api.RestDestinationService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@Scope("request")
@RequestMapping(value = "/incomingEvents")
@Api(tags = "events")
public class IncomingEventsRestController implements InitializingBean {

    @Autowired
    private DeviceEventService deviceEventService;

    @Autowired
    private User user;

    private Set<String> validationsCode = new HashSet<>();

    private static final String SEACH_NOTES =
        "### Query Search Terms\n\n" +
        "* `device`\n\n" +
        "* `channel`\n\n" +
        "* `timestamp`\n\n" +
        "\n\n" +
        "### Query Examples\n\n" +
        "* `q=device:818599ad-0000-0000-0000-000000000000`\n\n" +
        "* `q=channel:temperature+device:818599ad-0000-0000-0000-000000000000`\n\n" +
        "* `q=channel:temperature`\n\n";

    @GetMapping(path = "/{application}/")
    @PreAuthorize("hasAuthority('VIEW_DEVICE_LOG')")
    @ApiOperation(
            value = "Search incoming events",
            response = EventVO.class,
            notes = SEACH_NOTES,
            produces = "application/json"
            )
    public List<EventVO> list(
            @ApiParam(value = "Application ID", required = true)
            @PathVariable(value = "application") String application,
            @ApiParam(value = "Query string", example = "deviceGuid:818599ad-3502-4e70-a852-fc7af8e0a9f4")
            @RequestParam(required = false, defaultValue = "", name = "q") String query,
            @ApiParam(value = "The sort order", allowableValues = "newest,oldest")
            @RequestParam(required = false, defaultValue = "newest") String sort,
            @ApiParam(value = "The number of results returned")
            @RequestParam(required = false, defaultValue = "100") Integer limit
        ) throws BadServiceResponseException, BadRequestResponseException {

        Tenant tenant = user.getTenant();

        boolean ascending = false;
        if (sort.equalsIgnoreCase("oldest")) {
            ascending = true;
        }

        EventsFilter filter = new EventsFilter(query);
        String deviceGuid = filter.getDeviceGuid();
        String channel = filter.getChannel();

        ServiceResponse<List<Event>> restDestinationResponse = deviceEventService.findIncomingBy(tenant, deviceGuid, channel, null, null, ascending, 3);

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
