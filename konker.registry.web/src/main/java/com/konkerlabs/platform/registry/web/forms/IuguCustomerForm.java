package com.konkerlabs.platform.registry.web.forms;

import com.konkerlabs.platform.registry.business.model.IuguCustomer;
import com.konkerlabs.platform.registry.web.forms.api.ModelBuilder;
import lombok.Data;

@Data
public class IuguCustomerForm implements ModelBuilder<IuguCustomer, IuguCustomerForm,Void> {

    private String id;
    private String email;
    private String name;
    private String zipCode;
    private String street;
    private String city;
    private String state;
    private String country;
    private String cardToken;
    private String plan;
    private boolean kit;
    private Long quantityKit;

    public IuguCustomerForm() {

	}

    @Override
    public IuguCustomer toModel() {
        return IuguCustomer.builder()
                .id(getId())
                .email(getEmail())
                .name(getName())
                .zipCode(getZipCode())
                .street(getStreet())
                .city(getCity())
                .state(getState())
                .country(getCountry())
                .build();
    }

    @Override
    public IuguCustomerForm fillFrom(IuguCustomer model) {
        this.setId(model.getId());
        this.setEmail(model.getEmail());
        this.setName(model.getName());
        this.setZipCode(model.getZipCode());
        this.setStreet(model.getStreet());
        this.setCity(model.getCity());
        this.setState(model.getState());
        this.setCountry(model.getCountry());
        return this;
    }
}
