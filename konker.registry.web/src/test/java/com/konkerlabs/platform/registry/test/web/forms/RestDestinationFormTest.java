package com.konkerlabs.platform.registry.test.web.forms;

import com.konkerlabs.platform.registry.business.model.RestDestination;
import com.konkerlabs.platform.registry.business.model.RestDestination.RestDestinationHeader;
import com.konkerlabs.platform.registry.business.model.RestDestination.RestDestinationType;
import com.konkerlabs.platform.registry.web.forms.RestDestinationForm;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RestDestinationFormTest {

    private RestDestination model;
    private RestDestinationForm form;

    @Before
    public void setUp() {
    	RestDestinationHeader formHeader = new RestDestinationHeader();
    	formHeader.setKey("Zemaa3Telv");
    	formHeader.setValue("JBhsBW80Ne");

    	List<RestDestinationHeader> formHeaders = new ArrayList<>();
    	formHeaders.add(formHeader);

    	Map<String, String> headersMap = new HashMap<>();
    	headersMap.put(formHeader.getKey(), formHeader.getValue());

        model = RestDestination.builder()
                .name("Name")
                .serviceURI("http://localhost:8080/path?query=1")
                .serviceUsername("username")
                .servicePassword("password")
                .method("POST")
                .headers(headersMap)
                .type(RestDestinationType.FORWARD_MESSAGE)
                .active(true).build();

        form = new RestDestinationForm();
        form.setName(model.getName());
        form.setServiceURI(model.getServiceURI());
        form.setServiceUsername(model.getServiceUsername());
        form.setServicePassword(model.getServicePassword());
        form.setActive(model.isActive());
        form.setMethod(model.getMethod());
        form.setHeaders(formHeaders);
        form.setType(RestDestinationType.FORWARD_MESSAGE);
    }

    @Test
    public void shouldTranslateFromFormToModel() {
        assertThat(form.toModel(), equalTo(model));
    }

    @Test
    public void shouldTranslateFromModelToForm() {
        assertThat(new RestDestinationForm().fillFrom(model), equalTo(form));
    }

    @Test
    public void shouldParseDuplicatedProtocol() {
        form.setServiceURI("http://http://www.domain.com");
        assertThat(form.getServiceProtocol(), equalTo("http"));
        assertThat(form.getServiceHost(), equalTo("www.domain.com"));
    }

}