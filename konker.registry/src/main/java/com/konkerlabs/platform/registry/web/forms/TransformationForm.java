package com.konkerlabs.platform.registry.web.forms;

import com.konkerlabs.platform.registry.business.model.Transformation;
import com.konkerlabs.platform.registry.web.forms.api.ModelBuilder;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

@Data
public class TransformationForm implements ModelBuilder<Transformation,TransformationForm,Void> {

    private String name;
    private String description;
    private List<TransformationStep> steps = new LinkedList(){
        {add(new TransformationStep());}
    };

    @Data
    public static class TransformationStep {
        private String url;
        private String username;
        private String password;

        public TransformationStep() {
        }

        public TransformationStep(String url, String username, String password) {
            this.url = url;
            this.username = username;
            this.password = password;
        }
    }

    @Override
    public Transformation toModel() {
        return null;
    }

    @Override
    public TransformationForm fillFrom(Transformation model) {
        return null;
    }
}
