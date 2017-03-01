package com.konkerlabs.platform.registry.api.web.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.konkerlabs.platform.registry.api.model.EventRouteVO;
import com.konkerlabs.platform.registry.api.model.RestResponseBuilder;
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
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
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

    @Autowired
    private MessageSource messageSource;

    private Set<String> validationsCode = new HashSet<>();

    @GetMapping(path = "/")
    public ResponseEntity<?> list() {

        Tenant tenant = user.getTenant();

        ServiceResponse<List<EventRoute>> routeResponse = eventRouteService.getAll(tenant);

        if (!routeResponse.isOk()) {
            return createErrorResponse(routeResponse);
        } else {
            List<EventRouteVO> listVO = new ArrayList<>();
            for (EventRoute route : routeResponse.getResult()) {
                listVO.add(new EventRouteVO(route));
            }
            return RestResponseBuilder.ok().withHttpStatus(HttpStatus.OK).withResult(listVO).build();
        }

    }

    @GetMapping(path = "/{routeGuid}")
    public ResponseEntity<?> read(@PathVariable("routeGuid") String routeGuid) {

        Tenant tenant = user.getTenant();

        ServiceResponse<EventRoute> routeResponse = eventRouteService.getByGUID(tenant, routeGuid);

        if (!routeResponse.isOk()) {
            return createErrorResponse(routeResponse);
        } else {
            EventRouteVO obj = new EventRouteVO(routeResponse.getResult());
            return RestResponseBuilder.ok().withHttpStatus(HttpStatus.OK).withResult(obj).build();
        }

    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody EventRouteVO routeForm) {

        Tenant tenant = user.getTenant();

        RouteActor incoming = null;
        ServiceResponse<RouteActor> incomingResponse = getRouteActor(tenant, routeForm.getIncoming());
        if (incomingResponse.isOk()) {
            incoming = incomingResponse.getResult();
        } else {
            return createErrorResponse(incomingResponse);
        }

        RouteActor outgoing = null;
        ServiceResponse<RouteActor> outgoingResponse = getRouteActor(tenant, routeForm.getOutgoing());
        if (outgoingResponse.isOk()) {
            outgoing = outgoingResponse.getResult();
        } else {
            return createErrorResponse(outgoingResponse);
        }

        Transformation transformation = null;
        ServiceResponse<Transformation> transformationResponse = getTransformation(tenant, routeForm);
        if (transformationResponse.isOk()) {
            transformation = transformationResponse.getResult();
        } else {
            return createErrorResponse(transformationResponse);
        }

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
            return createErrorResponse(routeResponse);
        } else {
            return RestResponseBuilder.ok().withHttpStatus(HttpStatus.CREATED).withMessages(getMessages(routeResponse))
                    .withResult(new EventRouteVO(routeResponse.getResult())).build();
        }

    }

    private ServiceResponse<Transformation> getTransformation(Tenant tenant, EventRouteVO routeForm) {

        String guid = routeForm.getTransformationGuid();

        if (StringUtils.isNoneBlank(guid)) {
            ServiceResponse<Transformation> transformationResponse = transformationService.get(tenant, guid);
            if (transformationResponse.isOk()) {
                Transformation transformation = transformationResponse.getResult();
                return ServiceResponseBuilder.<Transformation>ok().withResult(transformation).build();
            } else {
                return ServiceResponseBuilder.<Transformation>error().withMessages(transformationResponse.getResponseMessages()).build();
            }
        }

        return ServiceResponseBuilder.<Transformation>ok().withResult(null).build();

    }

    @SuppressWarnings("serial")
    private ServiceResponse<RouteActor> getRouteActor(Tenant tenant, RouteActorVO routeForm) {

        RouteActor routeActor = RouteActor.builder().build();

        if (RouteActorType.DEVICE.name().equalsIgnoreCase(routeForm.getType())) {
            ServiceResponse<Device> deviceResponse =  deviceRegisterService.getByDeviceGuid(tenant, routeForm.getGuid());
            if (deviceResponse.isOk()) {
                routeActor.setDisplayName(deviceResponse.getResult().getName());
                routeActor.setUri(deviceResponse.getResult().toURI());
                routeActor.setData(new HashMap<String, String>() {{ put(EventRoute.DEVICE_MQTT_CHANNEL, routeForm.getChannel()); }} );
                return ServiceResponseBuilder.<RouteActor>ok().withResult(routeActor).build();
            } else {
                return ServiceResponseBuilder.<RouteActor>error().withMessages(deviceResponse.getResponseMessages()).build();
            }
        }

        return ServiceResponseBuilder.<RouteActor>ok().withResult(null).build();

    }


    @PutMapping(path = "/{routeGuid}")
    public ResponseEntity<?> update(@PathVariable("routeGuid") String routeGuid, @RequestBody EventRouteVO routeForm) {

        Tenant tenant = user.getTenant();

        EventRoute routeFromDB = null;
        ServiceResponse<EventRoute> routeResponse = eventRouteService.getByGUID(tenant, routeGuid);

        if (!routeResponse.isOk()) {
            return createErrorResponse(routeResponse);
        } else {
            routeFromDB = routeResponse.getResult();
        }

        RouteActor incoming = null;
        ServiceResponse<RouteActor> incomingResponse = getRouteActor(tenant, routeForm.getIncoming());
        if (incomingResponse.isOk()) {
            incoming = incomingResponse.getResult();
        } else {
            return createErrorResponse(incomingResponse);
        }

        RouteActor outgoing = null;
        ServiceResponse<RouteActor> outgoingResponse = getRouteActor(tenant, routeForm.getOutgoing());
        if (outgoingResponse.isOk()) {
            outgoing = outgoingResponse.getResult();
        } else {
            return createErrorResponse(outgoingResponse);
        }

        Transformation transformation = null;
        ServiceResponse<Transformation> transformationResponse = getTransformation(tenant, routeForm);
        if (transformationResponse.isOk()) {
            transformation = transformationResponse.getResult();
        } else {
            return createErrorResponse(transformationResponse);
        }

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
            return createErrorResponse(updateResponse);

        } else {
            return RestResponseBuilder.ok().withHttpStatus(HttpStatus.OK).withMessages(getMessages(updateResponse))
                    .build();
        }

    }

    @DeleteMapping(path = "/{routeGuid}")
    public ResponseEntity<?> delete(@PathVariable("routeGuid") String routeGuid) {

        Tenant tenant = user.getTenant();

        ServiceResponse<EventRoute> routeResponse = eventRouteService.remove(tenant, routeGuid);

        if (!routeResponse.isOk()) {
            return createErrorResponse(routeResponse);
        } else {
            return RestResponseBuilder.ok().withHttpStatus(HttpStatus.NO_CONTENT)
                    .withMessages(getMessages(routeResponse)).build();
        }

    }

    private List<String> getMessages(ServiceResponse<?> serviceResponse) {
        List<String> messages = serviceResponse.getResponseMessages().entrySet().stream()
                .map(v -> messageSource.getMessage(v.getKey(), v.getValue(), user.getLanguage().getLocale()))
                .collect(Collectors.toList());

        return messages;
    }

    private ResponseEntity<?> createErrorResponse(ServiceResponse<?> serviceResponse) {

        if (containsValidations(serviceResponse)) {

            return RestResponseBuilder.error().withHttpStatus(HttpStatus.BAD_REQUEST)
                    .withMessages(getMessages(serviceResponse)).build();
        } else {

            return RestResponseBuilder.error().withHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                    .withMessages(getMessages(serviceResponse)).build();
        }

    }

    private boolean containsValidations(ServiceResponse<?> routeResponse) {

        Map<String, Object[]> responseMessages = routeResponse.getResponseMessages();

        for (String key: responseMessages.keySet()) {
            if (validationsCode.contains(key)) {
                return true;
            }
        }

        return false;

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
