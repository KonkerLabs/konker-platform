package com.konkerlabs.platform.registry.integration.endpoints;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.integration.processors.DeviceEventProcessor;
import com.konkerlabs.platform.utilities.parsers.json.JsonParsingService;
import lombok.Builder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

@RestController
@RequestMapping("pub")
public class DeviceEventRestEndpoint {

    public enum Messages {
        INVALID_REQUEST_BODY("integration.rest.invalid.body"),
        INVALID_RESOURCE("integration.rest.invalid.resource");

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

    @Autowired
    public DeviceEventRestEndpoint(ApplicationContext applicationContext,
                                   DeviceEventProcessor deviceEventProcessor,
                                   JsonParsingService jsonParsingService) {
        this.applicationContext = applicationContext;
        this.deviceEventProcessor = deviceEventProcessor;
        this.jsonParsingService = jsonParsingService;
    }

    @RequestMapping(value = "/{apiKey}/{channel}",
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
