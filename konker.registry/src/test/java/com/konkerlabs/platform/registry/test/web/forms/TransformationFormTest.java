package com.konkerlabs.platform.registry.test.web.forms;

import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.Transformation;
import com.konkerlabs.platform.registry.web.forms.TransformationForm;
import org.junit.Before;

import java.util.LinkedList;

public class TransformationFormTest {

    private TransformationForm form;
    private Tenant tenant;

    @Before
    public void setUp() {
        tenant = Tenant.builder()
            .domainName("domain")
            .name("name")
            .id("id").build();

        form = new TransformationForm();
        form.setName("Transformation name");
        form.setDescription("Description");
        form.setSteps(new LinkedList<TransformationForm.TransformationStep>() {
            {add(new TransformationForm.TransformationStep("URL","USERNAME","PASSWORD"));}
        });

//        model = Transformation.builder()
//            .name(form.getName())
//            .description(form.getDescription())
//            .type(Transformation.TransformationType.REST)
    }

}