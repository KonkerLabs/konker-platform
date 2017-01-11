package com.konkerlabs.platform.registry.web.csv;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ArrayUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.konkerlabs.platform.registry.business.model.Event.EventActor;
import com.konkerlabs.platform.registry.business.model.Event.EventDecorator;

public class EventCsvDownload {
	
	public void download(List<EventDecorator> data, HttpServletResponse response) throws IOException, SecurityException, NoSuchMethodException {
		String headerKey = "Content-Disposition";
		String headerValue = String.format("attachment; filename=\"%s\"", "events.csv");

		response.setContentType("application/csv");
		response.setHeader(headerKey, headerValue);
		PrintWriter writer = response.getWriter();
		
		String[] header = createHeader(EventDecorator.class);
		String[] additionalHeader = createAdditionalHeader(data);
		header = ArrayUtils.addAll(header, additionalHeader);
		
		StringBuffer bufferHeader = new StringBuffer();
		for (String head : header) {
			bufferHeader.append(head).append(", ");
		}
		writer.println(bufferHeader.toString());
				
		for (EventDecorator event : data) {
			Map<String, String> jsonMap = new LinkedHashMap<>();
			jsonToMap("", new ObjectMapper().readTree(event.getPayload()), jsonMap);
			
			StringBuffer bufferJson = new StringBuffer();
			Arrays.asList(additionalHeader).forEach(c -> bufferJson.append(jsonMap.get(c)).append(", "));
			bufferJson.deleteCharAt(bufferJson.lastIndexOf(","));
			
			writer.println(event.getTimestampFormated() +", "+
						event.getTimestamp() +", "+
						Optional.ofNullable(event.getIncoming()).orElse(EventActor.builder().tenantDomain("").build()).getTenantDomain() +", "+
						Optional.ofNullable(event.getIncoming()).orElse(EventActor.builder().deviceGuid("").build()).getDeviceGuid() +", "+
						Optional.ofNullable(event.getIncoming()).orElse(EventActor.builder().deviceId("").build()).getDeviceId() +", "+
						Optional.ofNullable(event.getIncoming()).orElse(EventActor.builder().channel("").build()).getChannel() +", "+
						Optional.ofNullable(event.getOutgoing()).orElse(EventActor.builder().tenantDomain("").build()).getTenantDomain() +", "+
						Optional.ofNullable(event.getOutgoing()).orElse(EventActor.builder().deviceGuid("").build()).getDeviceGuid() +", "+
						Optional.ofNullable(event.getOutgoing()).orElse(EventActor.builder().deviceId("").build()).getDeviceId() +", "+
						Optional.ofNullable(event.getOutgoing()).orElse(EventActor.builder().channel("").build()).getChannel() +", "+
						bufferJson.toString());
		}
		
		writer.flush();
		writer.close();
	}
	
	private String[] createAdditionalHeader(List<EventDecorator> data) throws JsonProcessingException, IOException {
		String[] additionalHeader = new String[0];
		if (!data.isEmpty()) {
			Map<String, String> map = new LinkedHashMap<>();
			
			jsonToMap("", 
					new ObjectMapper().readTree(data.get(0).getPayload()), 
					map);
			additionalHeader = map.keySet().toArray(new String[0]);
			return additionalHeader;
		}
		
		return additionalHeader;
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
						listHeader.add(fieldName.concat("." + localField.getName()));
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
				jsonToMap(currentPath + "[" + i + "]", arrayNode.get(i), map);
			}
		} else if (jsonNode.isValueNode()) {
			ValueNode valueNode = (ValueNode) jsonNode;
			map.put(currentPath, valueNode.asText());
		}
	}

}
