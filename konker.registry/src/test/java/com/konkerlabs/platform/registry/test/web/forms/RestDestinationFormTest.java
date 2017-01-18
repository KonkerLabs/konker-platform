package com.konkerlabs.platform.registry.test.web.forms;

import com.konkerlabs.platform.registry.business.model.RestDestination;
import com.konkerlabs.platform.registry.web.forms.RestDestinationForm;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class RestDestinationFormTest {

    private RestDestination model;
    private RestDestinationForm form;

    @Before
    public void setUp() {
        model = RestDestination.builder()
                .name("Name")
                .serviceURI("http://localhost:8080/path?query=1")
                .serviceUsername("username")
                .servicePassword("password")
                .method("POST")
                .active(true).build();

        form = new RestDestinationForm();
        form.setName(model.getName());
        form.setServiceURI(model.getServiceURI());
        form.setServiceUsername(model.getServiceUsername());
        form.setServicePassword(model.getServicePassword());
        form.setActive(model.isActive());
        form.setMethod(model.getMethod());
    }

    @Test
    public void shouldTranslateFromFormToModel() throws Exception {
        assertThat(form.toModel(), equalTo(model));
    }

    @Test
    public void shouldTranslateFromModelToForm() throws Exception {
        assertThat(new RestDestinationForm().fillFrom(model), equalTo(form));
    }
}