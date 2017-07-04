package com.konkerlabs.platform.registry.web.csv;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.konkerlabs.platform.registry.business.model.Event.EventActor;
import com.konkerlabs.platform.registry.business.model.Event.EventDecorator;
import org.apache.commons.lang3.ArrayUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;

public class EventCsvDownload {
	
	public void download(List<EventDecorator> data, HttpServletResponse response, List<String> additionalHeaders) throws IOException, SecurityException, NoSuchMethodException {
		String headerKey = "Content-Disposition";
		String headerValue = String.format("attachment; filename=\"%s\"", "events.csv");

		response.setContentType("application/csv");
		response.setHeader(headerKey, headerValue);
		PrintWriter writer = response.getWriter();
		
		String[] header = createHeader(EventDecorator.class);
		String[] additionalHeader = additionalHeaders.toArray(new String[0]);
		header = ArrayUtils.addAll(header, additionalHeader);
		
		StringBuffer bufferHeader = new StringBuffer();
		for (String head : header) {
			bufferHeader.append(head).append(",");
		}
		bufferHeader.deleteCharAt(bufferHeader.lastIndexOf(","));
		writer.println(bufferHeader.toString());
				
		for (EventDecorator event : data) {
			Map<String, String> jsonMap = new LinkedHashMap<>();
			
			jsonToMap("", new ObjectMapper().readTree(event.getPayload()), jsonMap);
			
			StringBuffer bufferJson = new StringBuffer();
			Arrays.asList(additionalHeader).forEach(c -> {
				String jsonMapValue = jsonMap.get(c);
				jsonMapValue = handleSpecialCharacters(jsonMapValue);
				bufferJson.append(Optional.ofNullable(jsonMapValue).orElse("")).append(",");
			});
			
			bufferJson.deleteCharAt(bufferJson.lastIndexOf(","));
			
			writer.println(event.getTimestampFormated() +","+
						event.getTimestamp() +","+
						Optional.ofNullable(event.getIncoming()).orElse(EventActor.builder().tenantDomain("").build()).getTenantDomain() + "," +
						Optional.ofNullable(event.getIncoming()).orElse(EventActor.builder().applicationName("").build()).getApplicationName() + "," +
					    Optional.ofNullable(event.getIncoming()).orElse(EventActor.builder().deviceGuid("").build()).getDeviceGuid() + "," +
						Optional.ofNullable(event.getIncoming()).orElse(EventActor.builder().deviceId("").build()).getDeviceId() + "," +
						Optional.ofNullable(event.getIncoming()).orElse(EventActor.builder().channel("").build()).getChannel() + "," +
						bufferJson.toString());
		}
		
		writer.flush();
		writer.close();
	}

	private String handleSpecialCharacters(String jsonMapValue) {
		if (!Optional.ofNullable(jsonMapValue).isPresent()) {
			return null;
		}
		
		if (jsonMapValue.contains("\"\"")) {
			String value = "";
			value = jsonMapValue.replaceAll("\"\"", "\"\"\"\"");
			value = "\"".concat(value).concat("\"");
			jsonMapValue = value.substring(0, value.lastIndexOf("\"\"")).concat("\" \"");
			return jsonMapValue;
		}
		
		if (jsonMapValue.contains("\"")) {
			jsonMapValue = jsonMapValue.replaceAll("\"", "\"\"");
			jsonMapValue = "\"".concat(jsonMapValue).concat("\"");
			return jsonMapValue;
		}
		
		if (jsonMapValue.contains(",") || jsonMapValue.contains("/") 
				|| jsonMapValue.contains("\"")) {
			jsonMapValue = "\"".concat(jsonMapValue).concat("\"");
			return jsonMapValue;
		}
		
		if (jsonMapValue.contains("\\")) {
			String[] split = jsonMapValue.split("\\\\");
			String value = "";
			for (String str : split) {
				value = value.concat("\"\"").concat(str).concat("\"\"");
			}
			jsonMapValue = value.substring(0, value.lastIndexOf("\"\"")).concat("\" \"");
			return jsonMapValue;
		}
		
		return jsonMapValue;
	}
	
	private String[] createHeader(Class<EventDecorator> clazz) throws  SecurityException, NoSuchMethodException {
		List<String> listHeader = new ArrayList<>();
		
		for (Field field : clazz.getDeclaredFields()) {
			String fieldName = field.getName();
			fieldName = fieldName.substring(0, 1).toUpperCase().concat(fieldName.substring(1));
			
			Method method = clazz.getDeclaredMethod("get".concat(fieldName));
			
			if (method.getReturnType().getCanonicalName().contains("com.konkerlabs.platform.registry")) {
				for (Field localField : method.getReturnType().getDeclaredFields()) {
					if (!"URI_SCHEME".equals(localField.getName())) {
						listHeader.add("tenantDomain".equals(localField.getName()) 
								? fieldName.concat(".organization") 
								: fieldName.concat("." + localField.getName()));
					}
				}
			} else if(!"Payload".equals(fieldName)) {
				listHeader.add(fieldName);
			}
		}
		
		return listHeader.toArray(new String[0]);
	}
	
	public void jsonToMap(String currentPath, JsonNode jsonNode, Map<String, String> map) {
		if (jsonNode.isObject()) {
			ObjectNode objectNode = (ObjectNode) jsonNode;
			Iterator<Entry<String, JsonNode>> iter = objectNode.fields();
			String pathPrefix = currentPath.isEmpty() ? "" : currentPath + ".";
			
			while (iter.hasNext()) {
				Entry<String, JsonNode> entry = iter.next();
				jsonToMap(pathPrefix + entry.getKey(), entry.getValue(), map);
			}
		} else if (jsonNode.isArray()) {
			ArrayNode arrayNode = (ArrayNode) jsonNode;
			for (int i = 0; i < arrayNode.size(); i++) {
				jsonToMap(currentPath + "." + i, arrayNode.get(i), map);
			}
		} else if (jsonNode.isValueNode()) {
			ValueNode valueNode = (ValueNode) jsonNode;
			map.put(currentPath, valueNode.asText());
		}
	}

}
