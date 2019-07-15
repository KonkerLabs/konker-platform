package com.konkerlabs.platform.registry.integration.endpoints;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.DeviceFirmware;
import com.konkerlabs.platform.registry.business.model.DeviceFwUpdate;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.services.api.DeviceConfigSetupService;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.data.core.services.JedisTaskService;
import com.konkerlabs.platform.registry.data.core.integration.gateway.HttpGateway;
import com.konkerlabs.platform.registry.business.model.enumerations.FirmwareUpdateStatus;
import com.konkerlabs.platform.registry.business.services.api.*;
import com.konkerlabs.platform.registry.data.core.services.routes.api.EventRouteExecutor;
import com.konkerlabs.platform.registry.data.core.integration.processors.DeviceEventProcessor;
import com.konkerlabs.platform.registry.integration.serializers.EventJsonView;
import com.konkerlabs.platform.registry.integration.serializers.EventVO;
import com.konkerlabs.platform.utilities.parsers.json.JsonParsingService;
import lombok.Builder;
import lombok.Data;
import org.bson.types.Binary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.regex.Pattern;

@RestController
public class DeviceEventRestEndpoint {

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public enum Messages {
        INVALID_REQUEST_BODY("integration.rest.invalid.body"),
        INVALID_RESOURCE("integration.rest.invalid.resource"),
        INVALID_MGMT_CHANNEL("integration.rest.invalid.mgmt_channel"),
        INVALID_WAITTIME("integration.rest.invalid.waitTime"),
        INVALID_CHANNEL_PATTERN("integration.rest.invalid.channel"),
    	DEVICE_NOT_FOUND("integration.event_processor.channel.not_found"),
    	INVALID_REQUEST_ORIGIN("integration.rest.invalid_request_origin");

        private final String code;

        public String getCode() {
            return code;
        }

        Messages(String code) {
            this.code = code;
        }
    }

    private final ApplicationContext applicationContext;
    private final DeviceEventProcessor deviceEventProcessor;
    private final JsonParsingService jsonParsingService;
    private final DeviceEventService deviceEventService;
    private final DeviceRegisterService deviceRegisterService;
    private final Executor executor;
    private final JedisTaskService jedisTaskService;
    private final DeviceConfigSetupService deviceConfigSetupService;
    private final DeviceFirmwareUpdateService deviceFirmwareUpdateService;

    @Autowired
    public DeviceEventRestEndpoint(ApplicationContext applicationContext,
                                   DeviceEventProcessor deviceEventProcessor,
                                   JsonParsingService jsonParsingService,
                                   DeviceEventService deviceEventService,
                                   DeviceRegisterService deviceRegisterService,
                                   DeviceFirmwareUpdateService deviceFirmwareUpdateService,
                                   Executor executor,
                                   JedisTaskService jedisTaskService,
                                   DeviceConfigSetupService deviceConfigSetupService) {
        this.applicationContext = applicationContext;
        this.deviceEventProcessor = deviceEventProcessor;
        this.jsonParsingService = jsonParsingService;
        this.deviceEventService = deviceEventService;
        this.deviceRegisterService = deviceRegisterService;
        this.deviceFirmwareUpdateService = deviceFirmwareUpdateService;
        this.executor = executor;
        this.jedisTaskService = jedisTaskService;
        this.deviceConfigSetupService = deviceConfigSetupService;
    }

    @RequestMapping(
            value = { "sub/{apiKey}", "sub/{apiKey}/{channel}" },
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @JsonView(EventJsonView.class)
    public DeferredResult<List<EventVO>> subEvent(HttpServletRequest servletRequest,
                                                  @PathVariable("apiKey") String apiKey,
                                                  @PathVariable(name="channel", required=false) String channel,
                                                  @AuthenticationPrincipal Device principal,
                                                  @RequestParam(name = "offset", required = false) Optional<Long> offset,
                                                  @RequestParam(name = "waitTime", required = false) Optional<Long> waitTime,
                                                  Locale locale,
                                                  HttpServletResponse httpResponse) {

    	DeferredResult<List<EventVO>> deferredResult = new DeferredResult<>(waitTime.orElse(new Long("0")), Collections.emptyList());

    	Device device = deviceRegisterService.findByApiKey(apiKey);

    	if (!principal.getApiKey().equals(apiKey)) {
    		deferredResult.setErrorResult(applicationContext.getMessage(Messages.INVALID_RESOURCE.getCode(), null, locale));
            httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            return deferredResult;
    	}

    	if (waitTime.isPresent() && waitTime.get().compareTo(new Long("30000")) > 0) {
            deferredResult.setErrorResult(applicationContext.getMessage(Messages.INVALID_WAITTIME.getCode(), null, locale));
            httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            return deferredResult;
    	}

    	if(Optional.ofNullable(channel).isPresent() &&
    			(channel.length() > 32 || Pattern.compile("[^A-Za-z0-9_-]").matcher(channel).find())){
            deferredResult.setErrorResult(applicationContext.getMessage(Messages.INVALID_CHANNEL_PATTERN.getCode(), null, locale));
            httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            return deferredResult;
        }

    	if (!Optional.of(device).isPresent()) {
    		deferredResult.setErrorResult(applicationContext.getMessage(Messages.DEVICE_NOT_FOUND.getCode(), null, locale));
            httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
    		return deferredResult;
    	}

    	Instant startTimestamp = offset.isPresent() ? Instant.ofEpochMilli(offset.get()) : null;
    	boolean asc = offset.isPresent();
    	Integer limit = offset.isPresent() ? 50 : 1;

    	ServiceResponse<List<Event>> response = deviceEventService.findOutgoingBy(device.getTenant(), device.getApplication(), device.getGuid(),
    			channel, startTimestamp, null, asc, limit);

    	if (!response.getResult().isEmpty() || !waitTime.isPresent() || (waitTime.isPresent() && waitTime.get().equals(new Long("0")))) {
            deferredResult.setResult(EventVO.from(response.getResult()));

    	} else {
    		CompletableFuture.supplyAsync(() -> {
    			String subChannel = Optional.ofNullable(channel).isPresent() ? apiKey+ '.' +channel : apiKey;
    			return jedisTaskService.subscribeToChannel(subChannel, startTimestamp, asc, limit);
    		}, executor)
    		.whenCompleteAsync((result, throwable) -> deferredResult.setResult(EventVO.from(result)), executor);
    	}

    	return deferredResult;
    }

    private EventResponse buildResponse(String message, Locale locale) {
        return EventResponse.builder()
                .code(message)
                .message(applicationContext.getMessage(message,null, locale)).build();
    }

    @RequestMapping(value = "pub/{apiKey}/mgmt/{channel}",
            method = RequestMethod.POST)
    public ResponseEntity<EventResponse> onMgmtEvent(HttpServletRequest servletRequest,
                                                 @PathVariable("apiKey") String apiKey,
                                                 @PathVariable("channel") String channel,
                                                 @AuthenticationPrincipal Device principal,
                                                 @RequestBody String body,
                                                 Locale locale) {
        if (!jsonParsingService.isValid(body))
            return new ResponseEntity<EventResponse>(buildResponse(Messages.INVALID_REQUEST_BODY.getCode(),locale), HttpStatus.BAD_REQUEST);

        if (!principal.getApiKey().equals(apiKey))
            return new ResponseEntity<EventResponse>(buildResponse(Messages.INVALID_RESOURCE.getCode(),locale), HttpStatus.NOT_FOUND);

        if (!isValidMgmtChannel(channel))
            return new ResponseEntity<EventResponse>(buildResponse(Messages.INVALID_MGMT_CHANNEL.getCode(),locale), HttpStatus.NOT_FOUND);

        if (servletRequest.getHeader(HttpGateway.KONKER_VERSION_HEADER) != null)
            return new ResponseEntity<EventResponse>(buildResponse(Messages.INVALID_REQUEST_ORIGIN.getCode(), locale), HttpStatus.FORBIDDEN);

        try {
            deviceEventProcessor.process(apiKey, String.format("mgmt/%s", channel),body);
        } catch (BusinessException e) {
            return new ResponseEntity<EventResponse>(buildResponse(e.getMessage(),locale),HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<EventResponse>(
                EventResponse.builder().code(String.valueOf(HttpStatus.OK.value()))
                        .message(HttpStatus.OK.name()).build(),
                HttpStatus.OK);
    }

    private boolean isValidMgmtChannel(String channel) {
        return "battery".equals(channel);
    }

    @RequestMapping(value = "pub/{apiKey}/{channel}",
            method = RequestMethod.POST)
    public ResponseEntity<EventResponse> onEvent(HttpServletRequest servletRequest,
                                                 @PathVariable("apiKey") String apiKey,
                                                 @PathVariable("channel") String channel,
                                                 @AuthenticationPrincipal Device principal,
                                                 @RequestBody String body,
                                                 Locale locale) {
        if (!jsonParsingService.isValid(body))
            return new ResponseEntity<EventResponse>(buildResponse(Messages.INVALID_REQUEST_BODY.getCode(),locale), HttpStatus.BAD_REQUEST);

        if (!principal.getApiKey().equals(apiKey))
            return new ResponseEntity<EventResponse>(buildResponse(Messages.INVALID_RESOURCE.getCode(),locale), HttpStatus.NOT_FOUND);

		if (servletRequest.getHeader(HttpGateway.KONKER_VERSION_HEADER) != null)
			return new ResponseEntity<EventResponse>(buildResponse(Messages.INVALID_REQUEST_ORIGIN.getCode(), locale), HttpStatus.FORBIDDEN);

		String clientIP = servletRequest.getHeader("X-FORWARDED-FOR");
		if (clientIP == null || "".equals(clientIP)) {
		    clientIP = servletRequest.getRemoteAddr();
        }

        try {
            deviceEventProcessor.process(apiKey,channel,body);

            if (principal.isDebug()) {
                deviceEventProcessor.process(apiKey,
                        EventRouteExecutor.DEBUG_CHANNEL,
                        "{\"sourceIP\" : \"" + clientIP + "\"}");
            }
        } catch (BusinessException e) {
            return new ResponseEntity<EventResponse>(buildResponse(e.getMessage(),locale),HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<EventResponse>(
        		EventResponse.builder().code(String.valueOf(HttpStatus.OK.value()))
        		.message(HttpStatus.OK.name()).build(),
        		HttpStatus.OK);
    }

    @RequestMapping(value = "cfg/{apiKey}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<EventResponse> configEvent(HttpServletRequest servletRequest,
                                                 @PathVariable("apiKey") String apiKey,
                                                 @AuthenticationPrincipal Device principal,
                                                 Locale locale) {
    	Device device = deviceRegisterService.findByApiKey(apiKey);

    	if (!principal.getApiKey().equals(apiKey)) {
            return new ResponseEntity<>(
                    EventResponse.builder().code(String.valueOf(HttpStatus.BAD_REQUEST.value()))
                            .message(applicationContext.getMessage(Messages.INVALID_RESOURCE.getCode(), null, locale)).build(),
                    HttpStatus.BAD_REQUEST);
    	}

    	if (!Optional.ofNullable(device).isPresent()) {
    		return new ResponseEntity<>(
                    EventResponse.builder().code(String.valueOf(HttpStatus.BAD_REQUEST.value()))
                            .message(applicationContext.getMessage(Messages.DEVICE_NOT_FOUND.getCode(), null, locale)).build(),
                    HttpStatus.BAD_REQUEST);
    	}

    	ServiceResponse<String> serviceResponse = deviceConfigSetupService
    			.findByModelAndLocation(device.getTenant(), device.getApplication(), device.getDeviceModel(), device.getLocation());

    	if (serviceResponse.isOk()) {
    		return new ResponseEntity<>(
                    EventResponse.builder().code(String.valueOf(HttpStatus.OK.value()))
                            .message(serviceResponse.getResult()).build(),
                    HttpStatus.OK);
    	} else {
    		return new ResponseEntity<>(
                    EventResponse.builder().code(String.valueOf(HttpStatus.NOT_FOUND.value()))
                            .message("{ }")
                            .build(),
                    HttpStatus.NOT_FOUND);
    	}
    }

    @RequestMapping(value = "firmware/{apiKey}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<JsonNode> downloadFirmware(HttpServletRequest servletRequest,
                                                     @PathVariable("apiKey") String apiKey,
                                                     @AuthenticationPrincipal Device principal,
                                                     Locale locale) {
        LOGGER.info("Firmware update info: {}", apiKey);

        Device device = deviceRegisterService.findByApiKey(apiKey);

        if (!principal.getApiKey().equals(apiKey)) {
            return new ResponseEntity(
                    EventResponse.builder().code(String.valueOf(HttpStatus.BAD_REQUEST.value()))
                            .message(applicationContext.getMessage(Messages.INVALID_RESOURCE.getCode(), null, locale)).build(),
                    HttpStatus.BAD_REQUEST);
        }

        if (!Optional.ofNullable(device).isPresent()) {
            return new ResponseEntity(
                EventResponse.builder().code(String.valueOf(HttpStatus.BAD_REQUEST.value()))
                        .message(applicationContext.getMessage(Messages.DEVICE_NOT_FOUND.getCode(), null, locale)).build(),
                HttpStatus.BAD_REQUEST);
        }

        ServiceResponse<DeviceFwUpdate> serviceResponse = deviceFirmwareUpdateService.findPendingFwUpdateByDevice(
                device.getTenant(),
                device.getApplication(),
                device
                );

        if (serviceResponse.isOk()) {
            ObjectMapper objectMapper = new ObjectMapper(new JsonFactory());
            JsonNode node = objectMapper.createObjectNode();

            ((ObjectNode) node).put("version", serviceResponse.getResult().getVersion());

            return new ResponseEntity<JsonNode>(node, HttpStatus.OK);
        } else {
            return new ResponseEntity(
                    EventResponse.builder().code(String.valueOf(HttpStatus.NOT_FOUND.value()))
                            .message(applicationContext.getMessage(DeviceFirmwareUpdateService.Validations.FIRMWARE_UPDATE_PENDING_STATUS_DOES_NOT_EXIST.getCode(), null, locale)).build(),
                    HttpStatus.NOT_FOUND);
        }
    }

    @Data
    public static class DeviceFwUpdateVO {

        @NotNull
        private String version;
        @NotNull
        private FirmwareUpdateStatus status;

    }

    @RequestMapping(value = "firmware/{apiKey}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<JsonNode> updateFirmwareUpdateStatus(HttpServletRequest servletRequest,
                                                     @PathVariable("apiKey") String apiKey,
                                                     @AuthenticationPrincipal Device principal,
                                                     @RequestBody
                                                     @Valid
                                                     DeviceFwUpdateVO deviceFwUpdateVO,
                                                     Locale locale) {
        Device device = deviceRegisterService.findByApiKey(apiKey);

        if (!principal.getApiKey().equals(apiKey)) {
            return new ResponseEntity(
                    EventResponse.builder().code(String.valueOf(HttpStatus.BAD_REQUEST.value()))
                            .message(applicationContext.getMessage(Messages.INVALID_RESOURCE.getCode(), null, locale)).build(),
                    HttpStatus.BAD_REQUEST);
        }

        if (!Optional.ofNullable(device).isPresent()) {
            return new ResponseEntity(
                    EventResponse.builder().code(String.valueOf(HttpStatus.BAD_REQUEST.value()))
                            .message(applicationContext.getMessage(Messages.DEVICE_NOT_FOUND.getCode(), null, locale)).build(),
                    HttpStatus.BAD_REQUEST);
        }

        ServiceResponse<DeviceFwUpdate> serviceResponse = deviceFirmwareUpdateService.findPendingFwUpdateByDevice(
                device.getTenant(),
                device.getApplication(),
                device
        );

        if (serviceResponse.isOk()) {
            DeviceFwUpdate deviceFwUpdate = serviceResponse.getResult();
            if (!deviceFwUpdate.getVersion().equals(deviceFwUpdateVO.getVersion())) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            deviceFwUpdate.setStatus(deviceFwUpdateVO.getStatus());
            ServiceResponse<DeviceFwUpdate> deviceFwUpdateServiceResponse = deviceFirmwareUpdateService.updateStatus(
                    device.getTenant(),
                    device.getApplication(),
                    device,
                    deviceFwUpdate.getVersion(),
                    deviceFwUpdateVO.getStatus()
                    );

            DeviceFwUpdate deviceFwUpdateDB = deviceFwUpdateServiceResponse.getResult();

            ObjectMapper objectMapper = new ObjectMapper(new JsonFactory());
            JsonNode node = objectMapper.createObjectNode();

            ((ObjectNode) node).put("version", deviceFwUpdateDB.getVersion());
            ((ObjectNode) node).put("status", deviceFwUpdateDB.getStatus().name());

            return new ResponseEntity<JsonNode>(node, HttpStatus.OK);
        } else {
            return new ResponseEntity(
                    EventResponse.builder().code(String.valueOf(HttpStatus.NOT_FOUND.value()))
                            .message(applicationContext.getMessage(DeviceFirmwareUpdateService.Validations.FIRMWARE_UPDATE_PENDING_STATUS_DOES_NOT_EXIST.getCode(), null, locale)).build(),
                    HttpStatus.NOT_FOUND);
        }

    }

    @RequestMapping(value = "firmware/{apiKey}/binary", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity downloadFirmwareBinary(HttpServletRequest servletRequest,
                                           @PathVariable("apiKey") String apiKey,
                                           @AuthenticationPrincipal Device principal,
                                           Locale locale) {
        LOGGER.info("Downloading firmware: {}", apiKey);

        Device device = deviceRegisterService.findByApiKey(apiKey);

        if (!principal.getApiKey().equals(apiKey)) {
            return new ResponseEntity<>(
                    EventResponse.builder().code(String.valueOf(HttpStatus.BAD_REQUEST.value()))
                            .message(applicationContext.getMessage(Messages.INVALID_RESOURCE.getCode(), null, locale)).build(),
                    HttpStatus.BAD_REQUEST);
        }

        if (!Optional.ofNullable(device).isPresent()) {
            return new ResponseEntity<>(
                    EventResponse.builder().code(String.valueOf(HttpStatus.BAD_REQUEST.value()))
                            .message(applicationContext.getMessage(Messages.DEVICE_NOT_FOUND.getCode(), null, locale)).build(),
                    HttpStatus.BAD_REQUEST);
        }

        ServiceResponse<DeviceFwUpdate> serviceResponse = deviceFirmwareUpdateService.findPendingFwUpdateByDevice(
                device.getTenant(),
                device.getApplication(),
                device
        );

        if (serviceResponse.isOk()) {
            DeviceFirmware deviceFirmware = serviceResponse.getResult().getDeviceFirmware();
            Binary binary = deviceFirmware.getFirmware();
            String fileName = deviceFirmware.getVersion().replaceAll("\\.", "-") + ".bin";

            byte out[] = binary.getData();

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add("content-disposition", "attachment; filename=" + fileName);
            responseHeaders.add("Content-Type", "application/octet-stream");

            return new ResponseEntity(out, responseHeaders,HttpStatus.OK);
        } else {
            return new ResponseEntity<>(
                    EventResponse.builder().code(String.valueOf(HttpStatus.NOT_FOUND.value()))
                            .message(applicationContext.getMessage(DeviceFirmwareUpdateService.Validations.FIRMWARE_UPDATE_PENDING_STATUS_DOES_NOT_EXIST.getCode(), null, locale)).build(),
                    HttpStatus.NOT_FOUND);
        }
    }

    @Data
    @Builder
    static class EventResponse {
        private String code;
        private String message;
    }
}
