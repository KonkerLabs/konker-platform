package com.konkerlabs.platform.registry.integration.converters;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;

@Service
@Qualifier("application/cbor")
public class CBorJsonConverter implements JsonConverter {
	
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Override
	public ServiceResponse<String> toJson(byte[] bytes) {
		try {
			CBORFactory factory = new CBORFactory();
			ObjectMapper mapper = new ObjectMapper(factory);
			String json = mapper.readValue(bytes, String.class);
			
			return ServiceResponseBuilder.<String>ok().withResult(json).build();
		} catch (IOException e) {
			LOGGER.error("Exception converting CBOR to JSON", e);
            return ServiceResponseBuilder.<String>error().build();
		}
	}

	@Override
	public ServiceResponse<byte[]> fromJson(String json) {
		try {
			CBORFactory factory = new CBORFactory();
			ObjectMapper mapper = new ObjectMapper(factory);
			byte[] cborData = mapper.writeValueAsBytes(json);
			
			return ServiceResponseBuilder.<byte[]>ok().withResult(cborData).build();
		} catch (JsonProcessingException e) {
			LOGGER.error("Exception converting JSON to CBOR", e);
            return ServiceResponseBuilder.<byte[]>error().build();
		}
	}

}
