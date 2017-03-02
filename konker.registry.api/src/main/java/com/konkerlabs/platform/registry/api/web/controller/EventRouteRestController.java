package com.konkerlabs.platform.registry.api.web.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.konkerlabs.platform.registry.api.exceptions.BadServiceResponseException;
import com.konkerlabs.platform.registry.api.model.EventRouteVO;
import com.konkerlabs.platform.registry.api.model.RestResponse;
import com.konkerlabs.platform.registry.api.model.RouteActorType;
import com.konkerlabs.platform.registry.api.model.RouteActorVO;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.EventRoute;
import com.konkerlabs.platform.registry.business.model.EventRoute.RouteActor;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.Transformation;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.EventRouteService;
import com.konkerlabs.platform.registry.business.services.api.EventRouteService.Validations;

import io.swagger.annotations.ApiOperation;

import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.TransformationService;

@RestController
@Scope("request")
@RequestMapping(value = "/routes")
public class EventRouteRestController implements InitializingBean {

    @Autowired
    private EventRouteService eventRouteService;

    @Autowired
    private DeviceRegisterService deviceRegisterService;

    @Autowired
    private TransformationService transformationService;

    @Autowired
    private User user;

    private Set<String> validationsCode = new HashSet<>();

    @GetMapping(path = "/")
    @PreAuthorize("hasAuthority('LIST_ROUTES')")
    @ApiOperation(
            value = "List all routes by organization",
            response = EventRouteVO.class)
    public List<EventRouteVO> list() throws BadServiceResponseException {

        Tenant tenant = user.getTenant();

        ServiceResponse<List<EventRoute>> routeResponse = eventRouteService.getAll(tenant);

        if (!routeResponse.isOk()) {
            throw new BadServiceResponseException(user, routeResponse, validationsCode);
        } else {
            List<EventRouteVO> listVO = new ArrayList<>();
            for (EventRoute route : routeResponse.getResult()) {
                listVO.add(new EventRouteVO(route));
            }
            return listVO;
        }

    }

    @GetMapping(path = "/{routeGuid}")
    @PreAuthorize("hasAuthority('SHOW_DEVICE_ROUTE')")
    @ApiOperation(
            value = "Get a route by guid",
            response = RestResponse.class
    )
    public EventRouteVO read(@PathVariable("routeGuid") String routeGuid) throws BadServiceResponseException {

        Tenant tenant = user.getTenant();

        ServiceResponse<EventRoute> routeResponse = eventRouteService.getByGUID(tenant, routeGuid);

        if (!routeResponse.isOk()) {
            throw new BadServiceResponseException(user, routeResponse, validationsCode);
        } else {
            return new EventRouteVO(routeResponse.getResult());
        }

    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_DEVICE_ROUTE')")
    @ApiOperation(value = "Create a route")
    public EventRouteVO create(@RequestBody EventRouteVO routeForm) throws BadServiceResponseException {

        Tenant tenant = user.getTenant();

        RouteActor incoming = getRouteActor(tenant, routeForm.getIncoming());
        RouteActor outgoing = getRouteActor(tenant, routeForm.getOutgoing());
        Transformation transformation = getTransformation(tenant, routeForm);

        EventRoute route = EventRoute.builder()
                .name(routeForm.getName())
                .description(routeForm.getDescription())
                .incoming(incoming)
                .outgoing(outgoing)
                .filteringExpression(routeForm.getFilteringExpression())
                .transformation(transformation)
                .active(true)
                .build();

        ServiceResponse<EventRoute> routeResponse = eventRouteService.save(tenant, route);

        if (!routeResponse.isOk()) {
            throw new BadServiceResponseException(user, routeResponse, validationsCode);
        } else {
            return new EventRouteVO(routeResponse.getResult());
        }

    }

    private Transformation getTransformation(Tenant tenant, EventRouteVO routeForm) throws BadServiceResponseException {

        String guid = routeForm.getTransformationGuid();

        if (StringUtils.isNoneBlank(guid)) {
            ServiceResponse<Transformation> transformationResponse = transformationService.get(tenant, guid);
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
    private RouteActor getRouteActor(Tenant tenant, RouteActorVO routeForm) throws BadServiceResponseException {

        RouteActor routeActor = RouteActor.builder().build();

        if (RouteActorType.DEVICE.name().equalsIgnoreCase(routeForm.getType())) {
            ServiceResponse<Device> deviceResponse =  deviceRegisterService.getByDeviceGuid(tenant, routeForm.getGuid());
            if (deviceResponse.isOk()) {
                routeActor.setDisplayName(deviceResponse.getResult().getName());
                routeActor.setUri(deviceResponse.getResult().toURI());
                routeActor.setData(new HashMap<String, String>() {{ put(EventRoute.DEVICE_MQTT_CHANNEL, routeForm.getChannel()); }} );
                return routeActor;
            } else {
                throw new BadServiceResponseException(user, deviceResponse, validationsCode);
            }
        }

        return null;

    }


    @PutMapping(path = "/{routeGuid}")
    @PreAuthorize("hasAuthority('EDIT_DEVICE_ROUTE')")
    @ApiOperation(value = "Update a route")
    public void update(@PathVariable("routeGuid") String routeGuid, @RequestBody EventRouteVO routeForm) throws BadServiceResponseException {

        Tenant tenant = user.getTenant();

        EventRoute routeFromDB = null;
        ServiceResponse<EventRoute> routeResponse = eventRouteService.getByGUID(tenant, routeGuid);

        if (!routeResponse.isOk()) {
            throw new BadServiceResponseException(user, routeResponse, validationsCode);
        } else {
            routeFromDB = routeResponse.getResult();
        }

        RouteActor incoming = getRouteActor(tenant, routeForm.getIncoming());
        RouteActor outgoing = getRouteActor(tenant, routeForm.getOutgoing());
        Transformation transformation = getTransformation(tenant, routeForm);

        // update fields
        routeFromDB.setName(routeForm.getName());
        routeFromDB.setDescription(routeForm.getDescription());
        routeFromDB.setIncoming(incoming);
        routeFromDB.setOutgoing(outgoing);
        routeFromDB.setTransformation(transformation);
        routeFromDB.setFilteringExpression(routeForm.getFilteringExpression());
        routeFromDB.setActive(routeForm.isActive());

        ServiceResponse<EventRoute> updateResponse = eventRouteService.update(tenant, routeGuid, routeFromDB);

        if (!updateResponse.isOk()) {
            throw new BadServiceResponseException(user, updateResponse, validationsCode);
        }

    }

    @DeleteMapping(path = "/{routeGuid}")
    @PreAuthorize("hasAuthority('REMOVE_DEVICE_ROUTE')")
    @ApiOperation(value = "Delete a route")
    public void delete(@PathVariable("routeGuid") String routeGuid) throws BadServiceResponseException {

        Tenant tenant = user.getTenant();

        ServiceResponse<EventRoute> routeResponse = eventRouteService.remove(tenant, routeGuid);

        if (!routeResponse.isOk()) {
            throw new BadServiceResponseException(user, routeResponse, validationsCode);
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
