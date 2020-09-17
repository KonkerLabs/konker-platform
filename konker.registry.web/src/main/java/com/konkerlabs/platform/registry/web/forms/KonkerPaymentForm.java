package com.konkerlabs.platform.registry.web.forms;

import com.konkerlabs.platform.registry.business.model.KonkerPaymentCustomer;
import com.konkerlabs.platform.registry.web.forms.api.ModelBuilder;
import lombok.Data;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Data
public class KonkerPaymentForm implements ModelBuilder<KonkerPaymentCustomer, KonkerPaymentForm,Void> {

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

    public KonkerPaymentForm() {

	}

    @Override
    public KonkerPaymentCustomer toModel() {
        LocalDate now = LocalDate.now();
		LocalDate expiresAt = LocalDate.of(now.getYear(), now.getMonthValue() + 1, 5);
        return KonkerPaymentCustomer.builder()
                .tokenCard(getCardToken())
                .planName(getPlan().toUpperCase())
                .dateFirstPayment(expiresAt.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
                .customerName(getName())
                .email(getEmail())
                .zipCode(getZipCode())
                .street(getStreet())
                .city(getCity())
                .state(getState())
                .country(getCountry())
                .build();
    }

    @Override
    public KonkerPaymentForm fillFrom(KonkerPaymentCustomer model) {
        this.setCardToken(model.getTokenCard());
        this.setPlan(model.getPlanName());
        this.setName(model.getCustomerName());
        this.setEmail(model.getEmail());
        this.setZipCode(model.getZipCode());
        this.setStreet(model.getStreet());
        this.setCity(model.getCity());
        this.setState(model.getState());
        this.setCountry(model.getCountry());
        return this;
    }
}
