package com.konkerlabs.platform.registry.integration.endpoints;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.integration.processors.DeviceEventProcessor;
import com.konkerlabs.platform.utilities.parsers.json.JsonParsingService;

import lombok.Builder;
import lombok.Data;

@RestController
public class DeviceEventRestEndpoint {

    public enum Messages {
        INVALID_REQUEST_BODY("integration.rest.invalid.body"),
        INVALID_RESOURCE("integration.rest.invalid.resource"),
        INVALID_WAITTIME("integration.rest.invalid.waitTime");

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

    @Autowired
    public DeviceEventRestEndpoint(ApplicationContext applicationContext,
                                   DeviceEventProcessor deviceEventProcessor,
                                   JsonParsingService jsonParsingService,
                                   DeviceEventService deviceEventService) {
        this.applicationContext = applicationContext;
        this.deviceEventProcessor = deviceEventProcessor;
        this.jsonParsingService = jsonParsingService;
        this.deviceEventService = deviceEventService;
    }

    @RequestMapping(value = "pub/{apiKey}/{channel}",
                    method = RequestMethod.POST,
                    consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EventResponse> onEvent(HttpServletRequest servletRequest,
                                          @PathVariable("apiKey") String apiKey,
                                          @PathVariable("channel") String channel,
                                          @AuthenticationPrincipal Device principal,
                                          @RequestBody String body,
                                          Locale locale) {
        if (!jsonParsingService.isValid(body))
            return new ResponseEntity<EventResponse>(buildResponse(Messages.INVALID_REQUEST_BODY.getCode(),locale),HttpStatus.BAD_REQUEST);

        if (!principal.getApiKey().equals(apiKey))
            return new ResponseEntity<EventResponse>(buildResponse(Messages.INVALID_RESOURCE.getCode(),locale),HttpStatus.NOT_FOUND);

        try {
            deviceEventProcessor.process(apiKey,channel,body);
        } catch (BusinessException e) {
            return new ResponseEntity<EventResponse>(buildResponse(e.getMessage(),locale),HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<EventResponse>(HttpStatus.OK);
    }
    
    @RequestMapping(value = "sub/{apiKey}/{channel}", method = RequestMethod.POST,
    		consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public DeferredResult<List<Event>> subEvent(HttpServletRequest servletRequest,
                  @PathVariable("apiKey") String apiKey, 
                  @PathVariable("channel") String channel,
                  @AuthenticationPrincipal Device principal, 
                  @RequestParam(name = "offset", required = false) Long offset,
                  @RequestParam(name = "waitTime", required = false) Long waitTime,
                  Locale locale) {
    	
    	DeferredResult<List<Event>> deferredResult = new DeferredResult<>(waitTime);

    	if (!principal.getApiKey().equals(apiKey)) {
    		deferredResult.setErrorResult(new Exception(applicationContext.getMessage(Messages.INVALID_RESOURCE.getCode(),null, locale)));
    		return deferredResult;
    	}
    	
    	if (waitTime != null && waitTime.compareTo(new Long("30000")) > 0) {
    		deferredResult.setErrorResult(new Exception(applicationContext.getMessage(Messages.INVALID_WAITTIME.getCode(),null, locale)));
    		return deferredResult;
    	}

    	try {
    		deviceEventProcessor.process(apiKey, channel, Instant.ofEpochMilli(offset), waitTime, deferredResult);
    	} catch (BusinessException e) {
    		deferredResult.setErrorResult(e.getMessage());
    	}
    	
		deferredResult.onTimeout(() -> deferredResult.setResult(new ArrayList<>()));
    	
    	return deferredResult;
    }

    private EventResponse buildResponse(String message, Locale locale) {
        return EventResponse.builder()
                .code(message)
                .message(applicationContext.getMessage(message,null, locale)).build();
    }

    @Data
    @Builder
    static class EventResponse {
        private String code;
        private String message;
    }
}
