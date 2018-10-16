package com.konkerlabs.platform.registry.integration.endpoints;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.data.upload.UploadRepository;
import com.konkerlabs.platform.registry.data.core.integration.gateway.HttpGateway;
import com.konkerlabs.platform.registry.integration.processors.DeviceEventProcessor;
import com.konkerlabs.platform.utilities.parsers.json.JsonParsingService;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

@RestController
public class DeviceUploadRestEndpoint {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public enum Messages {
        INVALID_RESOURCE("integration.rest.invalid.resource"),
        INVALID_REQUEST_ORIGIN("integration.rest.invalid_request_origin"),
        INVALID_META_DATA("integration.rest.invalid_meta_data");

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
    private final UploadRepository uploadRepository;
    private final JsonParsingService jsonParsingService;

    @Autowired
    public DeviceUploadRestEndpoint(ApplicationContext applicationContext,
                                    DeviceEventProcessor deviceEventProcessor,
                                    UploadRepository uploadRepository,
                                    JsonParsingService jsonParsingService) {
        this.applicationContext = applicationContext;
        this.deviceEventProcessor = deviceEventProcessor;
        this.uploadRepository = uploadRepository;
        this.jsonParsingService = jsonParsingService;
    }

    @PostMapping(path = "/upload/{apiKey}/{channel}", consumes = "multipart/form-data")
    public ResponseEntity<EventResponse> create(
            HttpServletRequest servletRequest,
            Locale locale,
            @PathVariable("apiKey") String apiKey,
            @PathVariable("channel") String channel,
            @AuthenticationPrincipal Device device,
            @RequestParam(value = "file") MultipartFile file,
            @RequestParam(value = "meta-data", required = false) String metaData) {

        String filename = file.getOriginalFilename();
        String guid = UUID.randomUUID().toString();
        String fileKey = String.format("%s/%s/%s", device.getApplication().getName(), device.getGuid(), guid);
        String md5hash = null;

        try {
            md5hash = DigestUtils.md5Hex(file.getBytes());
        } catch (IOException e) {
            return new ResponseEntity<>(buildResponse(e.getMessage(), locale), HttpStatus.BAD_REQUEST);
        }

        if (!device.getApiKey().equals(apiKey))
            return new ResponseEntity<>(buildResponse(Messages.INVALID_RESOURCE.getCode(), locale), HttpStatus.NOT_FOUND);

        if (servletRequest.getHeader(HttpGateway.KONKER_VERSION_HEADER) != null)
            return new ResponseEntity<>(buildResponse(Messages.INVALID_REQUEST_ORIGIN.getCode(), locale), HttpStatus.FORBIDDEN);

        try {
            uploadRepository.upload(file.getInputStream(), fileKey, filename, false);
        } catch (Exception e) {
            return new ResponseEntity<>(buildResponse(e.getMessage(), locale), HttpStatus.BAD_REQUEST);
        }

        try {
            uploadRepository.upload(file.getInputStream(), fileKey, filename, false);
        } catch (Exception e) {
            return new ResponseEntity<>(buildResponse(e.getMessage(), locale), HttpStatus.BAD_REQUEST);
        }

        if (StringUtils.isNotBlank(metaData)) {
            if (!jsonParsingService.isValid(metaData)) {
                return new ResponseEntity<>(buildResponse(Messages.INVALID_META_DATA.getCode(), locale), HttpStatus.BAD_REQUEST);
            }
        }

        JsonNode payload = getPayload(file, filename, guid, md5hash, metaData);

        try {
            deviceEventProcessor.process(apiKey, channel, payload.toString());
        } catch (BusinessException e) {
            return new ResponseEntity<>(buildResponse(e.getMessage(), locale), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(
                EventResponse.builder()
                        .code(String.valueOf(HttpStatus.OK.value()))
                        .message(HttpStatus.OK.name())
                        .payload(payload)
                        .build(),
                HttpStatus.OK);

    }

    private JsonNode getPayload(MultipartFile file, String filename, String guid, String md5hash, String metaData) {
        ObjectMapper objectMapper = new ObjectMapper(new JsonFactory());
        JsonNode jsonNode = objectMapper.createObjectNode();

        ((ObjectNode) jsonNode).put("filename", filename);
        ((ObjectNode) jsonNode).put("size", file.getSize());
        ((ObjectNode) jsonNode).put("contentType", file.getContentType());
        ((ObjectNode) jsonNode).put("md5", md5hash);
        ((ObjectNode) jsonNode).put("guid", guid);

        if (StringUtils.isNotBlank(metaData)) {
            try {
                JsonNode metaNode = objectMapper.readTree(metaData);
                ((ObjectNode) jsonNode).set("metaData", metaNode);
            } catch (IOException e) {
                LOGGER.error("Invalid meta-data: " + metaData, e);
            }
        }

        return jsonNode;
    }

    private EventResponse buildResponse(String code, Locale locale) {
        String message = null;

        try {
            message = applicationContext.getMessage(code, null, locale);
        } catch (NoSuchMessageException e) {
            LOGGER.error("NoSuchMessageException: ", e.getMessage());
        }

        return EventResponse.builder()
                .code(code)
                .message(message).build();
    }

    @Data
    @Builder
    static class EventResponse {
        private String code;
        private String message;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private JsonNode payload;
    }

}
