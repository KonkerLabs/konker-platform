package com.konkerlabs.platform.utilities.parsers.json;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class JsonParsingServiceImpl implements JsonParsingService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public Map<String, Object> toFlatMap(String json) throws JsonProcessingException {
        Optional.ofNullable(json)
            .filter(s -> !s.isEmpty())
            .orElseThrow(() -> new IllegalArgumentException("JSON cannot be null or empty"));

        Map<String, Object> map = new HashMap<>();
        try {
            addKeys("", OBJECT_MAPPER.readTree(json), map);
        } catch (IOException e) {
            throw new JsonParseException("Failed to parse json",null,e);
        }

        return map;
    }

    @Override
    public Map<String, Object> toMap(String json) throws JsonProcessingException {
        try {
            return OBJECT_MAPPER.readValue(json,
                    new TypeReference<Map<String, Object>>() {
                    });
        } catch (IOException e) {
            throw new JsonParseException("Failed to parse json",null,e);
        }

    }

    @Override
    public String toJsonString(Map<String, Object> map) throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsString(map);
    }

    private void addKeys(String currentPath, JsonNode jsonNode, Map<String, Object> map) {
        if (jsonNode.isObject()) {
            ObjectNode objectNode = (ObjectNode) jsonNode;
            Iterator<Map.Entry<String, JsonNode>> iterator = objectNode.fields();
            String pathPrefix = currentPath.isEmpty() ? "" : currentPath + ".";

            while (iterator.hasNext()) {
                Map.Entry<String, JsonNode> entry = iterator.next();
                addKeys(pathPrefix + entry.getKey(), entry.getValue(), map);
            }
        } else if (jsonNode.isArray()) {
            ArrayNode arrayNode = (ArrayNode) jsonNode;
            for (int i = 0; i < arrayNode.size(); i++) {
                addKeys(currentPath + "." + i, arrayNode.get(i), map);
            }
        } else if (jsonNode.isValueNode()) {
            ValueNode valueNode = (ValueNode) jsonNode;
            switch (valueNode.getNodeType()) {
                case NUMBER: {
                    if (valueNode.asText().contains("."))
                        map.put(currentPath, valueNode.asDouble());
                    else
                        map.put(currentPath, valueNode.asLong());
                    break;
                }
                case BOOLEAN: {
                    map.put(currentPath, valueNode.asBoolean());
                    break;
                }
                default: {
                    map.put(currentPath, valueNode.asText());
                    break;
                }
            }
        }
    }
}
