package com.konkerlabs.platform.registry.web.forms;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.RestTransformationStep;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.Transformation;
import com.konkerlabs.platform.registry.business.model.enumerations.SupportedHttpMethod;
import com.konkerlabs.platform.registry.web.forms.api.ModelBuilder;
import lombok.Data;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

@Data
public class TransformationForm implements ModelBuilder<Transformation, TransformationForm, Void> {

    // colocar guid? TODO
    private String id;
    private String name;
    private String description;
    private String applicationId;
    private List<TransformationStepForm> steps = new LinkedList() {
        {
            add(new TransformationStepForm());
        }
    };

    public TransformationForm(){
        super();
    }

    public TransformationForm(Tenant tenant){
        super();
        applicationId = tenant.getDomainName();
    }

    @Data
    public static class TransformationStepForm {
        
    	private String method;
        private String urlProtocol;
        private String urlHost;
        private String username;
        private String password;
        private List<TransformationStepHeaderForm> headers = new ArrayList<>();

        public TransformationStepForm() {
        	if (headers.isEmpty()) {
        		// header html template
        		headers.add(new TransformationStepHeaderForm(null, null));
        	}
        }

        public TransformationStepForm(String method,
                                      String url,
                                      String username,
                                      String password,
                                      List<TransformationStepHeaderForm> headers) {
            this.method = method;
            this.setUrl(url);
            this.username = username;
            this.password = password;
            List<TransformationStepHeaderForm> defaultHeaders = new ArrayList<TransformationStepHeaderForm>();
            if (headers == null) {
                headers = new ArrayList<>();
            }
            headers.add(new TransformationStepHeaderForm("Content-Type", "application/json"));
            this.headers = headers != null ? headers : defaultHeaders;
        }

        public TransformationStepForm(String method,
                                      String url,
                                      String username,
                                      String password,
                                      Map<String, String> headers) {

            this.method = method;
            this.setUrl(url);
            this.username = username;
            this.password = password;
            List<TransformationStepHeaderForm> headerFromMap = new ArrayList<>();
            if (headers == null || headers.isEmpty()) {
                headers = new HashMap<>();
                headers.put("", "");
            }
            headers.forEach((key, value) -> {
                headerFromMap.add(new TransformationStepHeaderForm(key, value));
            });
            this.headers = headerFromMap;
        }

        public TransformationStepForm(String method,
                                      String url,
                                      String username,
                                      String password) {

            this.method = method;
            this.setUrl(url);
            this.username = username;
            this.password = password;
        	if (headers.isEmpty()) {
        		// header html template
        		headers.add(new TransformationStepHeaderForm(null, null));
        	}
        }

        public Map<String, String> getHeadersAsMap() {
            Map<String, String> headersAsMap = new HashMap<>();
            getHeaders().stream().forEach(item -> {
            	if (StringUtils.isNotBlank(item.getKey())) {
            		headersAsMap.put(item.getKey(), item.getValue());
            	}
            });
            return headersAsMap;
        }
        
    	public String getUrl() {
    		return MessageFormat.format("{0}://{1}", urlProtocol, urlHost);
    	}

    	public void setUrl(String url) {
    		String tokens[] = url.split("://");

    		if (tokens.length == 2) {
	    		urlProtocol = tokens[0];
	    		urlHost = tokens[1];
    		} else {
	    		urlProtocol = "http"; // default protocol
	    		urlHost = tokens[0];
    		}
    	}
    	
    }

    @Data
    public static class TransformationStepHeaderForm {
        private String key;
        private String value;

        public TransformationStepHeaderForm() {
        }

        public TransformationStepHeaderForm(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    @Override
    public Transformation toModel() {
        return Transformation.builder()
                .id(getId())
                .name(getName())
                .description(getDescription())
                .application(Application.builder().id(getApplicationId()).build())
                .steps(
                        steps.stream()
                                .map(transformationStep -> RestTransformationStep.builder()
                                        .attributes(new HashMap<String, Object>() {
                                            {
                                                put(RestTransformationStep.REST_ATTRIBUTE_METHOD, transformationStep.getMethod());
                                                put(RestTransformationStep.REST_URL_ATTRIBUTE_NAME, transformationStep.getUrl());
                                                put(RestTransformationStep.REST_USERNAME_ATTRIBUTE_NAME, transformationStep.getUsername());
                                                put(RestTransformationStep.REST_PASSWORD_ATTRIBUTE_NAME, transformationStep.getPassword());
                                                put(RestTransformationStep.REST_ATTRIBUTE_HEADERS, transformationStep.getHeadersAsMap());
                                            }
                                        }).build()).collect(Collectors.toList())
                ).build();
    }

    @Override
    public TransformationForm fillFrom(Transformation model) {
        setId(model.getId());
        setName(model.getName());
        setDescription(model.getDescription());
        setApplicationId(model.getApplication().getId());
        getSteps().clear();
        model.getSteps().stream().forEachOrdered(transformationStep ->
                getSteps().add(
                        new TransformationStepForm(
                                (String) transformationStep.getAttributes().get(RestTransformationStep.REST_ATTRIBUTE_METHOD),
                                (String) transformationStep.getAttributes().get(RestTransformationStep.REST_URL_ATTRIBUTE_NAME),
                                (String) transformationStep.getAttributes().get(RestTransformationStep.REST_USERNAME_ATTRIBUTE_NAME),
                                (String) transformationStep.getAttributes().get(RestTransformationStep.REST_PASSWORD_ATTRIBUTE_NAME),
                                (Map<String, String>) transformationStep.getAttributes().get(RestTransformationStep
                                        .REST_ATTRIBUTE_HEADERS)
                        ))
        );
        return this;
    }


    public SupportedHttpMethod[] getMethodList() {
        return SupportedHttpMethod.values();
    }


}
