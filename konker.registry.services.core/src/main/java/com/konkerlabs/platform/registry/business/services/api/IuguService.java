package com.konkerlabs.platform.registry.business.services.api;

import com.konkerlabs.platform.registry.business.model.IuguCustomer;
import com.konkerlabs.platform.registry.business.model.IuguPaymentWay;
import com.konkerlabs.platform.registry.business.model.IuguSubscription;
import com.konkerlabs.platform.registry.business.model.KonkerIuguPlan;

public interface IuguService {


    enum Validations {
        IUGU_CUSTOMER_NULL("service.iugu.customer.null"),
        IUGU_CUSTOMER_EMAIL_NULL("service.iugu.customer.email.null"),
        IUGU_CUSTOMER_NAME_NULL("service.iugu.customer.name.null"),
        IUGU_CUSTOMER_CREATION_ERROR("service.iugu.customer.creation.error"),
        IUGU_PAYMENT_WAY_NULL("service.iugu.payment.way.null"),
        IUGU_PAYMENT_WAY_CUSTOMER_ID_NULL("service.iugu.payment.way.customer_id_null"),
        IUGU_PAYMENT_WAY_TOKEN_NULL("service.iugu.payment.way.token_null"),
        IUGU_PAYMENT_WAY_CREATION_ERROR("service.iugu.payment.way.creation.error"),
        IUGU_KONKER_PLAN_NULL("service.iugu.konker.plan.null"),
        IUGU_KONKER_PLAN_TENANT_DOMAIN_NULL("service.iugu.konker.plan.tenant.domain.null"),
        IUGU_KONKER_PLAN_TENANT_NAME_NULL("service.iugu.konker.plan.tenant.name.null"),
        IUGU_KONKER_PLAN_IDENTIFIER_NULL("service.iugu.konker.plan.plan_identifier.null"),
        IUGU_KONKER_PLAN_CUSTOMER_ID_NULL("service.iugu.konker.plan.customer_id.null"),
        IUGU_KONKER_PLAN_CREATION_ERROR("service.iugu.konker.plan.creation.error"),
        IUGU_KONKER_PLAN_PAY_KIT_ERROR("service.iugu.konker.plan.pay.kit.error");

        private String code;

        Validations(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    enum Messages {
        PAYMENT_WAY_REGISTERED_SUCCESSFULLY("service.payment.way.registered_success");

        public String getCode() {
            return code;
        }

        private String code;

        Messages(String code) {
            this.code = code;
        }
    }
    
    ServiceResponse<IuguCustomer> createIuguCustomer(IuguCustomer iuguCustomer);

    ServiceResponse<IuguPaymentWay> createPaymentWay(IuguPaymentWay iuguPaymentWay);

    ServiceResponse<KonkerIuguPlan> createKonkerIuguPlan(KonkerIuguPlan konkerIuguPlan);

    ServiceResponse<IuguSubscription> payForKit(KonkerIuguPlan konkerIuguPlan);

}
