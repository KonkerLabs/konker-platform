package com.konkerlabs.platform.registry.test.web.forms;

import com.konkerlabs.platform.registry.business.model.SmsDestination;
import com.konkerlabs.platform.registry.web.forms.SmsDestinationForm;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class SmsDestinationFormTest {

    private SmsDestination model;
    private SmsDestinationForm form;

    @Before
    public void setUp() {
        model = SmsDestination.builder()
                .name("Name")
                .description("Description")
                .phoneNumber("+5511987654321")
                .active(true).build();

        form = new SmsDestinationForm();
        form.setName(model.getName());
        form.setDescription(model.getDescription());
        form.setPhoneNumber(model.getPhoneNumber());
        form.setActive(model.isActive());
    }

    @Test
    public void shouldTranslateFromFormToModel() throws Exception {
        assertThat(form.toModel(),equalTo(model));
    }
    @Test
    public void shouldTranslateFromModelToForm() throws Exception {
        assertThat(new SmsDestinationForm().fillFrom(model),equalTo(form));
    }

}