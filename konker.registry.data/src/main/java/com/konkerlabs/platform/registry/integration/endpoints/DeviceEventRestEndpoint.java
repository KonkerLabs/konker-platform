package com.konkerlabs.platform.registry.integration.endpoints;

import com.fasterxml.jackson.annotation.JsonView;
import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.data.services.JedisTaskService;
import com.konkerlabs.platform.registry.integration.gateways.HttpGateway;
import com.konkerlabs.platform.registry.integration.processors.DeviceEventProcessor;
import com.konkerlabs.platform.registry.integration.serializers.EventJsonView;
import com.konkerlabs.platform.registry.integration.serializers.EventVO;
import com.konkerlabs.platform.utilities.parsers.json.JsonParsingService;
import lombok.Builder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    public enum Messages {
        INVALID_REQUEST_BODY("integration.rest.invalid.body"),
        INVALID_RESOURCE("integration.rest.invalid.resource"),
        INVALID_WAITTIME("integration.rest.invalid.waitTime"),
        INVALID_CHANNEL_PATTERN("integration.rest.invalid.channel"),
    	DEVICE_NOT_FOUND("integration.event_processor.channel.not_found"),
    	INVALID_REQUEST_ORIGIN("integration.rest.invalid_requrest_origin");

        private String code;

        public String getCode() {
            return code;
        }

        Messages(String code) {
            this.code = code;
        }
    }

    private ApplicationContext applicationContext;
    private DeviceEventProcessor deviceEventProcessor;
    private JsonParsingService jsonParsingService;
    private DeviceEventService deviceEventService;
    private DeviceRegisterService deviceRegisterService;
    private Executor executor;
    private JedisTaskService jedisTaskService;

    @Autowired
    public DeviceEventRestEndpoint(ApplicationContext applicationContext,
                                   DeviceEventProcessor deviceEventProcessor,
                                   JsonParsingService jsonParsingService,
                                   DeviceEventService deviceEventService,
                                   DeviceRegisterService deviceRegisterService,
                                   Executor executor,
                                   JedisTaskService jedisTaskService) {
        this.applicationContext = applicationContext;
        this.deviceEventProcessor = deviceEventProcessor;
        this.jsonParsingService = jsonParsingService;
        this.deviceEventService = deviceEventService;
        this.deviceRegisterService = deviceRegisterService;
        this.executor = executor;
        this.jedisTaskService = jedisTaskService;
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
    			String subChannel = Optional.ofNullable(channel).isPresent() ? apiKey+"."+channel : apiKey;
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

        try {
            deviceEventProcessor.process(apiKey,channel,body);
        } catch (BusinessException e) {
            return new ResponseEntity<EventResponse>(buildResponse(e.getMessage(),locale),HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<EventResponse>(
        		EventResponse.builder().code(String.valueOf(HttpStatus.OK.value()))
        		.message(HttpStatus.OK.name()).build(),
        		HttpStatus.OK);
    }

    @Data
    @Builder
    static class EventResponse {
        private String code;
        private String message;
    }
}
