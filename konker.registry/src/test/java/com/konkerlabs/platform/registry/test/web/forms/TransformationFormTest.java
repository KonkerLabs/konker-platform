package com.konkerlabs.platform.registry.test.web.forms;

import com.konkerlabs.platform.registry.business.model.RestTransformationStep;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.Transformation;
import com.konkerlabs.platform.registry.web.forms.TransformationForm;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class TransformationFormTest {

    private TransformationForm form;
    private Tenant tenant;
    private Transformation model;

    @Before
    public void setUp() {
        tenant = Tenant.builder()
            .domainName("domain")
            .name("name")
            .id("id").build();

        form = new TransformationForm();
        form.setName("Transformation name");
        form.setDescription("Description");
        form.setSteps(new LinkedList<TransformationForm.TransformationStepForm>() {
            {add(new TransformationForm.TransformationStepForm("METHOD", "URL","USERNAME","PASSWORD",
                    new ArrayList<TransformationForm.TransformationStepHeaderForm>()));}
        });

        model = Transformation.builder()
            .name(form.getName())
            .description(form.getDescription())
            .step(
                RestTransformationStep.builder()
                    .attributes(new HashMap<String,Object>() {
                        {
                            put(RestTransformationStep.REST_ATTRIBUTE_METHOD,form.getSteps().get(0).getMethod());
                            put(RestTransformationStep.REST_URL_ATTRIBUTE_NAME,form.getSteps().get(0).getUrl());
                            put(RestTransformationStep.REST_USERNAME_ATTRIBUTE_NAME,form.getSteps().get(0).getUsername());
                            put(RestTransformationStep.REST_PASSWORD_ATTRIBUTE_NAME,form.getSteps().get(0).getPassword());
                            put(RestTransformationStep.REST_ATTRIBUTE_HEADERS, form.getSteps().get(0).getHeadersAsMap());
                        }
                    }).build()
            )
            .build();
    }

    @Test
    public void shouldTranslateFormToCorrespondingModel() {
        assertThat(form.toModel(),equalTo(model));
    }

    @Test
    public void shouldFillFormFromAModel() {
        assertThat(new TransformationForm().fillFrom(model),equalTo(form));
    }
}