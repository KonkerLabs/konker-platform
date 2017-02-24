package com.konkerlabs.platform.registry.api.web.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import com.konkerlabs.platform.registry.business.model.EventRoute;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.services.api.EventRouteService;
import com.konkerlabs.platform.registry.business.services.api.EventRouteService.Validations;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;

@RestController
@Scope("request")
@RequestMapping(value = "/routes")
public class EventRouteRestController {

    @Autowired
    private EventRouteService eventRouteService;

    @Autowired
    private User user;

    @Autowired
    private MessageSource messageSource;

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

        EventRoute route = EventRoute.builder().name(routeForm.getName())
                .description(routeForm.getDescription()).active(true).build();

        ServiceResponse<EventRoute> routeResponse = eventRouteService.save(tenant, route);

        if (!routeResponse.isOk()) {
            return createErrorResponse(routeResponse);
        } else {
            return RestResponseBuilder.ok().withHttpStatus(HttpStatus.CREATED).withMessages(getMessages(routeResponse))
                    .withResult(new EventRouteVO(routeResponse.getResult())).build();
        }

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

        // update fields
        routeFromDB.setName(routeForm.getName());
        routeFromDB.setDescription(routeForm.getDescription());

        ServiceResponse<EventRoute> updateResponse = eventRouteService.update(tenant, routeGuid, routeFromDB);

        if (!updateResponse.isOk()) {
            return createErrorResponse(routeResponse);

        } else {
            return RestResponseBuilder.ok().withHttpStatus(HttpStatus.OK).withMessages(getMessages(routeResponse))
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

        for (Validations value : EventRouteService.Validations.values()) {
            if (responseMessages.containsKey(value.getCode())) {
                return true;
            }
        }

        return false;
    }

}
