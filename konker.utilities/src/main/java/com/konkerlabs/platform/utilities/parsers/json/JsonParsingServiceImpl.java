package com.konkerlabs.platform.utilities.parsers.json;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class JsonParsingServiceImpl implements JsonParsingService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public Map<String, JsonPathData> toFlatMap(String json) throws JsonProcessingException {
        Optional.ofNullable(json)
            .filter(s -> !s.isEmpty())
            .orElseThrow(() -> new IllegalArgumentException("JSON cannot be null or empty"));

        Map<String, JsonPathData> map = new HashMap<>();
        try {
            addKeys("", OBJECT_MAPPER.readTree(json), map, null);
        } catch (IOException e) {
            throw new JsonParseException("Failed to parse json",null,e);
        }

        return map;
    }

    @Override
    public Map<String, Object> toMap(String json) throws JsonProcessingException {
        Optional.ofNullable(json)
                .filter(s -> !s.isEmpty())
                .orElseThrow(() -> new IllegalArgumentException("JSON cannot be null or empty"));

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
        Optional.ofNullable(map)
                .filter(s -> !s.isEmpty())
                .orElseThrow(() -> new IllegalArgumentException("Map cannot be null or empty"));

        return OBJECT_MAPPER.writeValueAsString(map);
    }

    @Override
    public boolean isValid(String json) {
        try {
            return Optional.ofNullable(json).isPresent() &&
                   Optional.ofNullable(OBJECT_MAPPER.readTree(json)).isPresent();
        } catch (IOException|IllegalArgumentException e) {
            return false;
        }
    }

    private void addKeys(String currentPath, JsonNode jsonNode, Map<String, JsonPathData> map, List<JsonNodeType> knownTypes) {
        if (jsonNode.isObject()) {
            ObjectNode objectNode = (ObjectNode) jsonNode;
            Iterator<Map.Entry<String, JsonNode>> iterator = objectNode.fields();
            String pathPrefix = currentPath.isEmpty() ? "" : currentPath + ".";

            if (knownTypes == null)
                knownTypes = new ArrayList<>();

            knownTypes.add(JsonNodeType.OBJECT);

            while (iterator.hasNext()) {
                Map.Entry<String, JsonNode> entry = iterator.next();
                addKeys(pathPrefix + entry.getKey(), entry.getValue(), map, new ArrayList<>(knownTypes));
            }
        } else if (jsonNode.isArray()) {
            ArrayNode arrayNode = (ArrayNode) jsonNode;

            if (currentPath.isEmpty())
                currentPath = "root";

            if (knownTypes == null)
                knownTypes = new ArrayList<>();

            knownTypes.add(JsonNodeType.ARRAY);

            for (int i = 0; i < arrayNode.size(); i++) {
                addKeys(currentPath + "." + i, arrayNode.get(i), map, new ArrayList<>(knownTypes));
            }
        } else if (jsonNode.isValueNode()) {
            ValueNode valueNode = (ValueNode) jsonNode;
            knownTypes.add(valueNode.getNodeType());
            JsonPathData.JsonPathDataBuilder data = JsonPathData.builder().types(knownTypes);
            switch (valueNode.getNodeType()) {
                case NUMBER: {
                    if (valueNode.asText().contains("."))
                        map.put(currentPath, data.value(valueNode.asDouble()).build());
                    else
                        map.put(currentPath, data.value(valueNode.asLong()).build());
                    break;
                }
                case BOOLEAN: {
                    map.put(currentPath, data.value(valueNode.asBoolean()).build());
                    break;
                }
                default: {
                    map.put(currentPath, data.value(valueNode.asText()).build());
                    break;
                }
            }
        }
    }
}
