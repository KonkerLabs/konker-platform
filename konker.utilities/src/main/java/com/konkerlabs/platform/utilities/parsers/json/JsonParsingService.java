package com.konkerlabs.platform.utilities.parsers.json;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Map;

public interface JsonParsingService {

    Map<String,Object> toFlatMap(String json) throws JsonProcessingException;

}
