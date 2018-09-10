package com.konkerlabs.platform.registry.data.core.integration.converters;

import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Qualifier("application/json")
public class DefaultJsonConverter implements JsonConverter {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Override
    public ServiceResponse<String> toJson(byte bytes[]) {
        return ServiceResponseBuilder.<String>ok().withResult(new String(bytes)).build();
    }

    @Override
    public ServiceResponse<byte[]> fromJson(String json) {
        return ServiceResponseBuilder.<byte[]>ok().withResult(json.getBytes()).build();
    }

}
