package com.konkerlabs.platform.registry.test.web.forms;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.web.forms.DeviceRegistrationForm;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class DeviceRegistrationFormTest {

    private DeviceRegistrationForm form;
    private Device model;

    @Before
    public void setUp() {
        form = new DeviceRegistrationForm();

        form.setDeviceId("device_id");
        form.setName("device_name");
        form.setDescription("device_description");

        model = Device.builder()
                .deviceId(form.getDeviceId())
                .name(form.getName())
                .description(form.getDescription()).build();
    }

    @Test
    public void shouldTranslateFromFormToModel() throws Exception {
        assertThat(form.toModel(),equalTo(model));
    }

    public void shouldTranslateFromModelToForm() throws Exception {
        DeviceRegistrationForm givenForm = new DeviceRegistrationForm().fillFrom(model);
        assertThat(givenForm,equalTo(form));
    }

}