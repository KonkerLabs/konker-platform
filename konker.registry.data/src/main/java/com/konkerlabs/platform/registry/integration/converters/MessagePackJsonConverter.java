package com.konkerlabs.platform.registry.integration.converters;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import org.msgpack.jackson.dataformat.MessagePackFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
@Qualifier("application/msgpack")
public class MessagePackJsonConverter implements JsonConverter {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Override
    public ServiceResponse<String> toJson(byte bytes[]) {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            // read message pack
            ObjectMapper objectMapper = new ObjectMapper(new MessagePackFactory());
            JsonNode node = objectMapper.readTree(bytes);

            // write json
            JsonFactory jsonFactory = new JsonFactory();
            JsonGenerator jsonGenerator = jsonFactory.createGenerator(bos);

            objectMapper.writeTree(jsonGenerator, node);
        } catch (IOException | NullPointerException e) {
            LOGGER.error("Exception converting message pack to JSON", e);
            return ServiceResponseBuilder.<String>error().build();
        }

        return ServiceResponseBuilder.<String>ok().withResult(new String(bos.toByteArray())).build();

    }

    @Override
    public ServiceResponse<byte[]> fromJson(String json) {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            // read json
            ObjectMapper objectMapper = new ObjectMapper(new JsonFactory());
            JsonNode node = objectMapper.readTree(json);

            // write message pack
            MessagePackFactory messagePackFactory = new MessagePackFactory();
            JsonGenerator jsonGenerator = messagePackFactory.createGenerator(bos);

            objectMapper.writeTree(jsonGenerator, node);
        } catch (IOException e) {
            LOGGER.error("Exception converting message pack to JSON", e);
            return ServiceResponseBuilder.<byte[]>error().build();
        }

        return ServiceResponseBuilder.<byte[]>ok().withResult(bos.toByteArray()).build();

    }

}
