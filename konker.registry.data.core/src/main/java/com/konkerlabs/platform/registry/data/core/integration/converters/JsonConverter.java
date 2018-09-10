package com.konkerlabs.platform.registry.data.core.integration.converters;

import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;

public interface JsonConverter {

    ServiceResponse<String> toJson(byte[] bytes);

    ServiceResponse<byte[]> fromJson(String json);

}
