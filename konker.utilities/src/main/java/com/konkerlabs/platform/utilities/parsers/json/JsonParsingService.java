package com.konkerlabs.platform.utilities.parsers.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

public interface JsonParsingService {

    Map<String,JsonPathData> toFlatMap(String json) throws JsonProcessingException;

    Map<String,Object> toMap(String json) throws JsonProcessingException;

    String toJsonString(Map<String, Object> map) throws JsonProcessingException;

    boolean isValid(String json);

    @Data
    @Builder
    class JsonPathData {
        List<JsonNodeType> types;
        Object value;
    }
}
