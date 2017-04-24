package com.konkerlabs.platform.registry.api.web.controller;

import com.konkerlabs.platform.registry.api.exceptions.BadServiceResponseException;
import com.konkerlabs.platform.registry.api.exceptions.NotFoundResponseException;
import com.konkerlabs.platform.registry.api.model.*;
import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.model.EventRoute.RouteActor;
import com.konkerlabs.platform.registry.business.services.api.*;
import com.konkerlabs.platform.registry.business.services.api.EventRouteService.Validations;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@Scope("request")
@RequestMapping(value = "/{application}/routes")
@Api(tags = "routes")
public class EventRouteRestController extends AbstractRestController implements InitializingBean {

    @Autowired
    private EventRouteService eventRouteService;

    @Autowired
    private DeviceRegisterService deviceRegisterService;

    @Autowired
    private TransformationService transformationService;

    @Autowired
    private RestDestinationService restDestinationService;

    private Set<String> validationsCode = new HashSet<>();

    @GetMapping(path = "/")
    @PreAuthorize("hasAuthority('LIST_ROUTES')")
    @ApiOperation(
            value = "List all routes by application",
            response = EventRouteVO.class)
    public List<EventRouteVO> list(@PathVariable("application") String applicationId) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        ServiceResponse<List<EventRoute>> routeResponse = eventRouteService.getAll(tenant, application);

        if (!routeResponse.isOk()) {
            throw new BadServiceResponseException(user, routeResponse, validationsCode);
        } else {
            return new EventRouteVO().apply(routeResponse.getResult());
        }

    }

    @GetMapping(path = "/{routeGuid}")
    @PreAuthorize("hasAuthority('SHOW_DEVICE_ROUTE')")
    @ApiOperation(
            value = "Get a route by guid",
            response = RestResponse.class
    )
    public EventRouteVO read(@PathVariable("application") String applicationId,
                             @PathVariable("routeGuid") String routeGuid) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        ServiceResponse<EventRoute> routeResponse = eventRouteService.getByGUID(tenant, application, routeGuid);

        if (!routeResponse.isOk()) {
            throw new NotFoundResponseException(user, routeResponse);
        } else {
            return new EventRouteVO().apply(routeResponse.getResult());
        }

    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_DEVICE_ROUTE')")
    @ApiOperation(value = "Create a route")
    public EventRouteVO create(
            @PathVariable("application") String applicationId,
            @ApiParam(name = "body", required = true)
            @RequestBody EventRouteInputVO routeForm) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        RouteActor incoming = getRouteActor(tenant, application, routeForm.getIncoming());
        RouteActor outgoing = getRouteActor(tenant, application, routeForm.getOutgoing());
        Transformation transformation = getTransformation(tenant, application, routeForm);

        EventRoute route = EventRoute.builder()
                .name(routeForm.getName())
                .description(routeForm.getDescription())
                .incoming(incoming)
                .outgoing(outgoing)
                .filteringExpression(routeForm.getFilteringExpression())
                .transformation(transformation)
                .active(true)
                .build();

        ServiceResponse<EventRoute> routeResponse = eventRouteService.save(tenant, application, route);

        if (!routeResponse.isOk()) {
            throw new BadServiceResponseException(user, routeResponse, validationsCode);
        } else {
            return new EventRouteVO().apply(routeResponse.getResult());
        }

    }

    private Transformation getTransformation(Tenant tenant, Application application, EventRouteInputVO routeForm) throws BadServiceResponseException {

        String guid = routeForm.getTransformationGuid();

        if (StringUtils.isNoneBlank(guid)) {
            ServiceResponse<Transformation> transformationResponse = transformationService.get(tenant, application, guid);
            if (transformationResponse.isOk()) {
                Transformation transformation = transformationResponse.getResult();
                return transformation;
            } else {
                throw new BadServiceResponseException(user, transformationResponse, validationsCode);
            }
        }

        return null;

    }

    @SuppressWarnings("serial")
    private RouteActor getRouteActor(Tenant tenant, Application application, RouteActorVO routeForm) throws BadServiceResponseException {

        RouteActor routeActor = RouteActor.builder().build();

        if (routeForm == null) {
            return null;
        }

        if (RouteActorType.DEVICE.name().equalsIgnoreCase(routeForm.getType())) {
            ServiceResponse<Device> deviceResponse = deviceRegisterService.getByDeviceGuid(tenant, application, routeForm.getGuid());
            if (deviceResponse.isOk()) {
                routeActor.setDisplayName(deviceResponse.getResult().getName());
                routeActor.setUri(deviceResponse.getResult().toURI());
                routeActor.setData(new HashMap<String, String>() {{ put(EventRoute.DEVICE_MQTT_CHANNEL, routeForm.getChannel()); }} );
                return routeActor;
            } else {
                throw new BadServiceResponseException(user, deviceResponse, validationsCode);
            }
        } else if (RouteActorType.REST.name().equalsIgnoreCase(routeForm.getType())) {
            ServiceResponse<RestDestination> restResponse = restDestinationService.getByGUID(tenant, application, routeForm.getGuid());
            if (restResponse.isOk()) {
                routeActor.setDisplayName(restResponse.getResult().getName());
                routeActor.setUri(restResponse.getResult().toURI());
                routeActor.setData(new HashMap<String, String>() {} );
                return routeActor;
            } else {
                throw new BadServiceResponseException(user, restResponse, validationsCode);
            }
        }

        return null;

    }


    @PutMapping(path = "/{routeGuid}")
    @PreAuthorize("hasAuthority('EDIT_DEVICE_ROUTE')")
    @ApiOperation(value = "Update a route")
    public void update(
            @PathVariable("application") String applicationId,
            @PathVariable("routeGuid") String routeGuid,
            @ApiParam(name = "body", required = true)
            @RequestBody EventRouteInputVO routeForm) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        EventRoute routeFromDB = null;
        ServiceResponse<EventRoute> routeResponse = eventRouteService.getByGUID(tenant, application, routeGuid);

        if (!routeResponse.isOk()) {
            throw new BadServiceResponseException(user, routeResponse, validationsCode);
        } else {
            routeFromDB = routeResponse.getResult();
        }

        RouteActor incoming = getRouteActor(tenant, application, routeForm.getIncoming());
        RouteActor outgoing = getRouteActor(tenant, application, routeForm.getOutgoing());
        Transformation transformation = getTransformation(tenant, application, routeForm);

        // update fields
        routeFromDB.setName(routeForm.getName());
        routeFromDB.setDescription(routeForm.getDescription());
        routeFromDB.setIncoming(incoming);
        routeFromDB.setOutgoing(outgoing);
        routeFromDB.setTransformation(transformation);
        routeFromDB.setFilteringExpression(routeForm.getFilteringExpression());
        routeFromDB.setActive(routeForm.isActive());

        ServiceResponse<EventRoute> updateResponse = eventRouteService.update(tenant, application, routeGuid, routeFromDB);

        if (!updateResponse.isOk()) {
            throw new BadServiceResponseException(user, updateResponse, validationsCode);
        }

    }

    @DeleteMapping(path = "/{routeGuid}")
    @PreAuthorize("hasAuthority('REMOVE_DEVICE_ROUTE')")
    @ApiOperation(value = "Delete a route")
    public void delete(
            @PathVariable("application") String applicationId,
            @PathVariable("routeGuid") String routeGuid) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        ServiceResponse<EventRoute> routeResponse = eventRouteService.remove(tenant, application, routeGuid);

        if (!routeResponse.isOk()) {
            if (routeResponse.getResponseMessages().containsKey(Validations.EVENT_ROUTE_NOT_FOUND.getCode())) {
                throw new NotFoundResponseException(user, routeResponse);
            } else {
                throw new BadServiceResponseException(user, routeResponse, validationsCode);
            }
        }

    }

    @Override
    public void afterPropertiesSet() throws Exception {

        for (Validations value : EventRouteService.Validations.values()) {
            validationsCode.add(value.getCode());
        }

        for (com.konkerlabs.platform.registry.business.model.EventRoute.Validations value : EventRoute.Validations.values()) {
            validationsCode.add(value.getCode());
        }

        for (com.konkerlabs.platform.registry.business.model.Transformation.Validations value : Transformation.Validations.values()) {
            validationsCode.add(value.getCode());
        }

        for (com.konkerlabs.platform.registry.business.services.api.TransformationService.Validations value : TransformationService.Validations.values()) {
            validationsCode.add(value.getCode());
        }

    }

}
