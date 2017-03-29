package com.konkerlabs.platform.registry.web.forms;

import com.konkerlabs.platform.registry.business.model.RestDestination;
import com.konkerlabs.platform.registry.business.model.RestDestination.RestDestinationHeader;
import com.konkerlabs.platform.registry.business.model.enumerations.SupportedHttpMethod;
import com.konkerlabs.platform.registry.web.forms.api.ModelBuilder;
import lombok.Data;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

@Data
public class RestDestinationForm implements ModelBuilder<RestDestination, RestDestinationForm, Void> {

    private String restId;
    private String name;
    private String method;
    private List<RestDestinationHeader> headers;
    private String serviceProtocol;
    private String serviceHost;
    private String serviceUsername;
    private String servicePassword;
    private boolean active;

    public RestDestinationForm() {
        setActive(Boolean.TRUE);
    }

    @Override
    public RestDestination toModel() {
        return RestDestination.builder()
                .name(getName())
                .serviceURI(getServiceURI())
                .serviceUsername(getServiceUsername())
                .servicePassword(getServicePassword())
                .active(isActive())
                .method(getMethod())
                .headers(headersMapToList(getHeaders()))
                .build();
    }

	@Override
    public RestDestinationForm fillFrom(RestDestination model) {
		setRestId(model.getId());
        setName(model.getName());
        setServiceURI(model.getServiceURI());
        setServiceUsername(model.getServiceUsername());
        setServicePassword(model.getServicePassword());
        setActive(model.isActive());
        setMethod(Optional.ofNullable(model.getMethod()).isPresent() ? model.getMethod() :
                SupportedHttpMethod.POST.getCode());
        setHeaders(headersListToMap(model.getHeaders()));
  
        return this;
    }

	public SupportedHttpMethod[] getMethodList() {
        return SupportedHttpMethod.values();
    }
    
    private Map<String, String> headersMapToList(List<RestDestinationHeader> list) {
    	Map<String, String> map = new HashMap<>();
    	
    	if (list == null) {
    		return map;
    	}
    	
    	for (RestDestinationHeader header : list) {
    		if (StringUtils.isNotBlank(header.getKey()))
    			map.put(header.getKey(), header.getValue());
		}
    	
		return map;
	}

    private List<RestDestinationHeader> headersListToMap(Map<String, String> map)  {
    	List<RestDestinationHeader> list = new ArrayList<>();
    	
    	if (map == null) {
    		return list;
    	}
    	
    	for (String key : map.keySet()) {
    		RestDestinationHeader header = new RestDestinationHeader();
    		header.setKey(key);
    		header.setValue(map.get(key));
    		list.add(header);
    	}

		return list;
	}

	public List<RestDestinationHeader> getHeaders() {

		if (headers == null || headers.isEmpty()) {
			headers = new ArrayList<RestDestinationHeader>();
			headers.add(new RestDestinationHeader());
		}

		return headers;

	}


	public String getServiceURI() {
		return MessageFormat.format("{0}://{1}", serviceProtocol, serviceHost);
	}

	public void setServiceURI(String serviceURI) {
		String tokens[] = serviceURI.split("://");

		if (tokens.length == 2) {
			serviceProtocol = tokens[0];
			serviceHost = tokens[1];
		} else {
			serviceProtocol = "http"; // default protocol
			serviceHost = tokens[0];
		}
	}
	
}
