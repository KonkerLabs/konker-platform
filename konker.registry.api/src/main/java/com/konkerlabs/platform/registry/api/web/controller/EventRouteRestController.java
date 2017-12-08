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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
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

    @Autowired
    private LocationSearchService locationSearchService;

    @Autowired
    private DeviceModelService deviceModelService;

    private Set<String> validationsCode = new HashSet<>();

    public static final String SEARCH_NOTES =
            "## Examples\n\n" +
            "### Device to Device\n\n" +
            "```\n" +
            "{\n" +
            "        \"name\": \"route\",\n" +
            "        \"description\": \"\",\n" +
            "        \"incoming\": {\n" +
            "          \"type\": \"DEVICE\",\n" +
            "          \"guid\": \"818599ad-3502-4e70-a852-fc7af8e0a9f3\",\n" +
            "          \"channel\": \"temperature\"\n" +
            "        },\n" +
            "        \"outgoing\": {\n" +
            "          \"type\": \"DEVICE\",\n" +
            "          \"guid\": \"6be96783-334f-48ad-9180-6fb0a412e562\",\n" +
            "          \"channel\": \"temp\"\n" +
            "        },\n" +
            "        \"filteringExpression\": \"\",\n" +
            "        \"transformationGuid\": null,\n" +
            "        \"active\": true\n" +
            "}\n" +
            "```\n\n" +
            "### Application to Device\n\n" +
            "```\n" +
            "{\n" +
            "        \"name\": \"route\",\n" +
            "        \"description\": \"\",\n" +
            "        \"incoming\": {\n" +
            "          \"type\": \"APPLICATION\"\n" +
            "        },\n" +
            "        \"outgoing\": {\n" +
            "          \"type\": \"DEVICE\",\n" +
            "          \"guid\": \"6be96783-334f-48ad-9180-6fb0a412e562\",\n" +
            "          \"channel\": \"temp\"\n" +
            "        },\n" +
            "        \"filteringExpression\": \"\",\n" +
            "        \"transformationGuid\": null,\n" +
            "        \"active\": true\n" +
            "}\n" +
            "```\n\n" +
            "### Model Location to Rest\n\n" +
            "```\n" +
            "{\n" +
            "        \"name\": \"route\",\n" +
            "        \"description\": \"\",\n" +
            "        \"incoming\": {\n" +
            "          \"type\": \"MODEL_LOCATION\",\n" +
            "          \"deviceModelName\": \"default\",\n" +
            "          \"locationName\": \"br\"\n" +
            "        },\n" +
            "        \"outgoing\": {\n" +
            "          \"type\": \"REST\",\n" +
            "          \"guid\": \"6be96783-334f-48ad-9180-6fb0a412e562\"\n" +
            "        },\n" +
            "        \"filteringExpression\": \"\",\n" +
            "        \"transformationGuid\": null,\n" +
            "        \"active\": true\n" +
            "}\n" +
            "```\n\n" +
            "### Device to Amazon Kinesis\n\n" +
            "```\n" +
            "{\n" +
            "        \"name\": \"route\",\n" +
            "        \"description\": \"\",\n" +
            "        \"incoming\": {\n" +
            "          \"type\": \"DEVICE\",\n" +
            "          \"guid\": \"818599ad-3502-4e70-a852-fc7af8e0a9f3\",\n" +
            "          \"channel\": \"temperature\"\n" +
            "        },\n" +
            "        \"outgoing\": {\n" +
            "          \"type\": \"AMAZON_KINESIS\",\n" +
            "          \"key\": \"key\",\n" +
            "          \"secret\": \"secret\",\n" +
            "          \"region\": \"us-west-1\",\n" +
            "          \"StreamName\": \"stream\"\n" +
            "        },\n" +
            "        \"filteringExpression\": \"\",\n" +
            "        \"transformationGuid\": null,\n" +
            "        \"active\": true\n" +
            "}\n" +
            "```\n\n" ;

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
            List<EventRouteVO> routesVO = new ArrayList<>();

            for (EventRouteVO routeVO : new EventRouteVO().apply(routeResponse.getResult())) {
                routesVO.add(patch(tenant, application, routeVO));
            }

            return routesVO;
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
            return patch(tenant, application, new EventRouteVO().apply(routeResponse.getResult()));
        }

    }

    private EventRouteVO patch(Tenant tenant, Application application, EventRouteVO routeVO) {

        routeVO.setIncoming(patchRoute(tenant, application, routeVO.getIncoming()));
        routeVO.setOutgoing(patchRoute(tenant, application, routeVO.getOutgoing()));

        return routeVO;
    }

    private RouteActorVO patchRoute(Tenant tenant, Application application, RouteActorVO actorVO) {

        if (Optional.ofNullable(actorVO).isPresent() && actorVO.getType().equals(RouteActorVO.TYPE_MODEL_LOCATION)) {
            RouteModelLocationActorVO deviceActorVO = (RouteModelLocationActorVO) actorVO;

            ServiceResponse<DeviceModel> deviceModelResponse = deviceModelService.getByTenantApplicationAndGuid(tenant, application, deviceActorVO.getDeviceModelGuid());
            if (deviceModelResponse.isOk()) {
                deviceActorVO.setDeviceModelGuid(null);
                deviceActorVO.setDeviceModelName(deviceModelResponse.getResult().getName());
            }

            ServiceResponse<Location> locationResponse = locationSearchService.findByGuid(tenant, application, deviceActorVO.getLocationGuid());
            if (locationResponse.isOk()) {
                deviceActorVO.setLocationGuid(null);
                deviceActorVO.setLocationName(locationResponse.getResult().getName());
            }
        }

        return actorVO;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_DEVICE_ROUTE')")
    @ApiOperation(
            value = "Create a route",
            response = EventRouteVO.class,
            notes = SEARCH_NOTES
            )
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
            return patch(tenant, application, new EventRouteVO().apply(routeResponse.getResult()));
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
    private RouteActor getRouteActor(Tenant tenant, Application application, RouteActorVO actorVO) throws BadServiceResponseException {

        RouteActor routeActor = RouteActor.builder().build();

        if (actorVO == null) {
            return null;
        }

        if (RouteActorType.DEVICE.name().equalsIgnoreCase(actorVO.getType())) {
            RouteDeviceActorVO deviceActorForm = (RouteDeviceActorVO) actorVO;
            ServiceResponse<Device> deviceResponse = deviceRegisterService.getByDeviceGuid(tenant, application, deviceActorForm.getGuid());
            if (deviceResponse.isOk()) {
                routeActor.setDisplayName(deviceResponse.getResult().getName());
                routeActor.setUri(deviceResponse.getResult().toURI());
                routeActor.setData(new HashMap<String, String>() {{ put(EventRoute.DEVICE_MQTT_CHANNEL, deviceActorForm.getChannel()); }} );
                return routeActor;
            } else {
                throw new BadServiceResponseException(user, deviceResponse, validationsCode);
            }
        } else if (RouteActorType.REST.name().equalsIgnoreCase(actorVO.getType())) {
            RouteRestActorVO restActorForm = (RouteRestActorVO) actorVO;
            ServiceResponse<RestDestination> restResponse = restDestinationService.getByGUID(tenant, application, restActorForm.getGuid());
            if (restResponse.isOk()) {
                routeActor.setDisplayName(restResponse.getResult().getName());
                routeActor.setUri(restResponse.getResult().toURI());
                routeActor.setData(new HashMap<String, String>() {} );
                return routeActor;
            } else {
                throw new BadServiceResponseException(user, restResponse, validationsCode);
            }

        } else if (RouteActorType.MODEL_LOCATION.name().equalsIgnoreCase(actorVO.getType())) {
            RouteModelLocationActorVO modelLocationActorForm = (RouteModelLocationActorVO) actorVO;

            DeviceModel deviceModel = null;
            Location location = null;

            ServiceResponse<DeviceModel> deviceModelResponse = deviceModelService.getByTenantApplicationAndName(tenant, application, modelLocationActorForm.getDeviceModelName());
            if (deviceModelResponse.isOk()) {
                deviceModel = deviceModelResponse.getResult();
            } else {
                throw new BadServiceResponseException(user, deviceModelResponse, validationsCode);
            }

            ServiceResponse<Location> locationResponse = locationSearchService.findByName(tenant, application, modelLocationActorForm.getLocationName(), false);
            if (locationResponse.isOk()) {
                location = locationResponse.getResult();
            } else {
                throw new BadServiceResponseException(user, locationResponse, validationsCode);
            }

            routeActor.setDisplayName(MessageFormat.format("{0} @ {1}", deviceModel.getName(), location.getName()));
            routeActor.setUri(DeviceModelLocation.builder().tenant(tenant).deviceModel(deviceModel).location(location).build().toURI());
            routeActor.setData(new HashMap<String, String>() {{ put(EventRoute.DEVICE_MQTT_CHANNEL, modelLocationActorForm.getChannel()); }} );

            return routeActor;
        } else if (RouteActorType.AMAZON_KINESIS.name().equalsIgnoreCase(actorVO.getType())) {
            RouteAmazonKinesisActorVO amazonKinesisActorForm = (RouteAmazonKinesisActorVO) actorVO;
            AmazonKinesis kinesisProperties = AmazonKinesis.builder().tenant(tenant)
                    .key(amazonKinesisActorForm.getKey())
                    .secret(amazonKinesisActorForm.getSecret())
                    .region(amazonKinesisActorForm.getRegion())
                    .streamName(amazonKinesisActorForm.getStreamName())
                    .build();

            routeActor.setDisplayName(MessageFormat.format("{0} @ {1}", amazonKinesisActorForm.getStreamName(), amazonKinesisActorForm.getRegion()));
            routeActor.setUri(kinesisProperties.toURI());
            routeActor.setData(kinesisProperties.getValues());

            return routeActor;
        } else if (RouteActorType.APPLICATION.name().equalsIgnoreCase(actorVO.getType())) {
        	routeActor.setDisplayName(application.getName());
            routeActor.setUri(application.toURI());
        	return routeActor;
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

        for (EventRoute.Validations value : EventRoute.Validations.values()) {
            validationsCode.add(value.getCode());
        }

        for (Transformation.Validations value : Transformation.Validations.values()) {
            validationsCode.add(value.getCode());
        }

        for (TransformationService.Validations value : TransformationService.Validations.values()) {
            validationsCode.add(value.getCode());
        }

        for (DeviceModelService.Validations value : DeviceModelService.Validations.values()) {
            validationsCode.add(value.getCode());
        }

        for (LocationService.Validations value : LocationService.Validations.values()) {
            validationsCode.add(value.getCode());
        }
        validationsCode.add(LocationService.Messages.LOCATION_NOT_FOUND.getCode());

    }

}
