package com.konkerlabs.platform.registry.business.services;

import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.services.api.IuguService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.business.services.api.TenantService;
import com.konkerlabs.platform.registry.config.IuguConfig;
import com.konkerlabs.platform.registry.config.KonkerInvoiceApiConfig;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Optional;

import static java.text.MessageFormat.format;

@Service
public class IuguServiceImpl implements IuguService {

	private static final Logger LOGGER = LoggerFactory.getLogger(IuguServiceImpl.class);

	@Autowired
	private RestTemplate restTemplate;
	private IuguConfig iuguConfig = new IuguConfig();
	private KonkerInvoiceApiConfig konkerInvoiceApiConfig = new KonkerInvoiceApiConfig();

    public IuguServiceImpl() {
    }


	@Override
	public ServiceResponse<IuguCustomer> createIuguCustomer(IuguCustomer iuguCustomer) {
    	if (!Optional.ofNullable(iuguCustomer).isPresent()) {
			return ServiceResponseBuilder.<IuguCustomer>error()
					.withMessage(Validations.IUGU_CUSTOMER_NULL.getCode())
					.build();
		}

		if (!Optional.ofNullable(iuguCustomer.getEmail()).isPresent()) {
			return ServiceResponseBuilder.<IuguCustomer>error()
					.withMessage(Validations.IUGU_CUSTOMER_EMAIL_NULL.getCode())
					.build();
		}

		if (!Optional.ofNullable(iuguCustomer.getName()).isPresent()) {
			return ServiceResponseBuilder.<IuguCustomer>error()
					.withMessage(Validations.IUGU_CUSTOMER_NAME_NULL.getCode())
					.build();
		}

		HttpEntity<IuguCustomer> entity = new HttpEntity<>(iuguCustomer);

		ResponseEntity<IuguCustomer> response = restTemplate.exchange(
				format("{0}/{1}?api_token={2}", iuguConfig.getApiURL(), "customers", iuguConfig.getApiToken()),
				HttpMethod.POST,
				entity,
				IuguCustomer.class);

		if (response.getStatusCode().is2xxSuccessful()) {
			return ServiceResponseBuilder.<IuguCustomer>ok()
					.withResult(response.getBody())
					.build();
		} else {
			return ServiceResponseBuilder.<IuguCustomer>error()
					.withMessage(Validations.IUGU_CUSTOMER_CREATION_ERROR.getCode())
					.build();
		}
	}

	@Override
	public ServiceResponse<IuguPaymentWay> createPaymentWay(IuguPaymentWay iuguPaymentWay) {
    	if (!Optional.ofNullable(iuguPaymentWay).isPresent()) {
			return ServiceResponseBuilder.<IuguPaymentWay>error()
					.withMessage(Validations.IUGU_PAYMENT_WAY_NULL.getCode())
					.build();
		}

		if (!Optional.ofNullable(iuguPaymentWay.getCustomerId()).isPresent()) {
			return ServiceResponseBuilder.<IuguPaymentWay>error()
					.withMessage(Validations.IUGU_PAYMENT_WAY_CUSTOMER_ID_NULL.getCode())
					.build();
		}

		if (!Optional.ofNullable(iuguPaymentWay.getToken()).isPresent()) {
			return ServiceResponseBuilder.<IuguPaymentWay>error()
					.withMessage(Validations.IUGU_PAYMENT_WAY_TOKEN_NULL.getCode())
					.build();
		}

		HttpEntity<IuguPaymentWay> entity = new HttpEntity<>(iuguPaymentWay);

		ResponseEntity<IuguPaymentWay> response = restTemplate.exchange(
				format("{0}/{1}/{2}/payment_methods?api_token={3}",
						iuguConfig.getApiURL(),
						"customers",
						iuguPaymentWay.getCustomerId(),
						iuguConfig.getApiToken()),
				HttpMethod.POST,
				entity,
				IuguPaymentWay.class);

		if (response.getStatusCode().is2xxSuccessful()) {
			return ServiceResponseBuilder.<IuguPaymentWay>ok()
					.withResult(response.getBody())
					.build();
		} else {
			return ServiceResponseBuilder.<IuguPaymentWay>error()
					.withMessage(Validations.IUGU_PAYMENT_WAY_CREATION_ERROR.getCode())
					.build();
		}
	}

	@Override
	public ServiceResponse<KonkerIuguPlan> createKonkerIuguPlan(KonkerIuguPlan konkerIuguPlan) {
    	if (!Optional.ofNullable(konkerIuguPlan).isPresent()) {
			return ServiceResponseBuilder.<KonkerIuguPlan>error()
					.withMessage(Validations.IUGU_KONKER_PLAN_NULL.getCode())
					.build();
		}

		if (!Optional.ofNullable(konkerIuguPlan.getTenantDomain()).isPresent()) {
			return ServiceResponseBuilder.<KonkerIuguPlan>error()
					.withMessage(Validations.IUGU_KONKER_PLAN_TENANT_DOMAIN_NULL.getCode())
					.build();
		}

		if (!Optional.ofNullable(konkerIuguPlan.getTenantName()).isPresent()) {
			return ServiceResponseBuilder.<KonkerIuguPlan>error()
					.withMessage(Validations.IUGU_KONKER_PLAN_TENANT_NAME_NULL.getCode())
					.build();
		}

		if (!Optional.ofNullable(konkerIuguPlan.getIuguCustomerId()).isPresent()) {
			return ServiceResponseBuilder.<KonkerIuguPlan>error()
					.withMessage(Validations.IUGU_KONKER_PLAN_CUSTOMER_ID_NULL.getCode())
					.build();
		}

		if (!Optional.ofNullable(konkerIuguPlan.getIuguPlanIdentifier()).isPresent()) {
			return ServiceResponseBuilder.<KonkerIuguPlan>error()
					.withMessage(Validations.IUGU_KONKER_PLAN_IDENTIFIER_NULL.getCode())
					.build();
		}

		HttpHeaders headers = getHttpHeaders();
		HttpEntity<KonkerIuguPlan> entity = new HttpEntity<>(konkerIuguPlan, headers);

		ResponseEntity<KonkerIuguPlan> response = restTemplate.exchange(
				format("{0}/tenant/plan",
						konkerInvoiceApiConfig.getUrl()),
				HttpMethod.POST,
				entity,
				KonkerIuguPlan.class);

		if (response.getStatusCode().is2xxSuccessful()) {
			return ServiceResponseBuilder.<KonkerIuguPlan>ok()
					.withResult(response.getBody())
					.build();
		} else {
			return ServiceResponseBuilder.<KonkerIuguPlan>error()
					.withMessage(Validations.IUGU_KONKER_PLAN_CREATION_ERROR.getCode())
					.build();
		}
	}

	@Override
	public ServiceResponse<IuguSubscription> payForKit(KonkerIuguPlan konkerIuguPlan) {
		if (!Optional.ofNullable(konkerIuguPlan).isPresent()) {
			return ServiceResponseBuilder.<IuguSubscription>error()
					.withMessage(Validations.IUGU_KONKER_PLAN_NULL.getCode())
					.build();
		}

		if (!Optional.ofNullable(konkerIuguPlan.getIuguCustomerId()).isPresent()) {
			return ServiceResponseBuilder.<IuguSubscription>error()
					.withMessage(Validations.IUGU_KONKER_PLAN_CUSTOMER_ID_NULL.getCode())
					.build();
		}

		IuguSubscription iuguSubscription = IuguSubscription.builder()
				.planIdentifier("KIT_BASICO_DESENVOLVIMENTO")
				.customerId(konkerIuguPlan.getIuguCustomerId())
				.expiresAt(LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
				.onlyOnChargeSuccess("true")
				.ignoreDueEmail("false")
				.payableWith("credit_card")
				.creditsBased("false")
				.twoStep("false")
				.suspendOnInvoiceExpired("false")
				.subItems(Arrays.asList(
						IuguSubscription.Item.builder().description("NodeMCU (placa de desenvolvimento contendo um ESP8266)").priceCents(0l).quantity(konkerIuguPlan.getQuantityKit() * 1l).recurrent(true).build(),
						IuguSubscription.Item.builder().description("Protoboards de 170 pontos").priceCents(0l).quantity(konkerIuguPlan.getQuantityKit() * 2l).recurrent(true).build(),
						IuguSubscription.Item.builder().description("Termistor de 1k Ohm").priceCents(0l).quantity(konkerIuguPlan.getQuantityKit() * 1l).recurrent(true).build(),
						IuguSubscription.Item.builder().description("Resistores de 470 Ohms").priceCents(0l).quantity(konkerIuguPlan.getQuantityKit() * 4l).recurrent(true).build(),
						IuguSubscription.Item.builder().description("Kit").priceCents(5000l).quantity(konkerIuguPlan.getQuantityKit()).recurrent(true).build()
				))
				.build();

		HttpHeaders headers = getHttpHeaders();
		HttpEntity<IuguSubscription> entity = new HttpEntity<>(iuguSubscription, headers);

		ResponseEntity<IuguSubscription> response = restTemplate.exchange(
				format("{0}/subscriptions?api_token={1}",
						iuguConfig.getApiURL(),
						iuguConfig.getApiToken()),
				HttpMethod.POST,
				entity,
				IuguSubscription.class);

		if (response.getStatusCode().is2xxSuccessful()) {
			IuguSubscription subscriptionCreated = response.getBody();
            restTemplate.exchange(
                    format("{0}/subscriptions/{1}?api_token={2}",
                            iuguConfig.getApiURL(),
                            subscriptionCreated.getId(),
                            iuguConfig.getApiToken()),
                    HttpMethod.DELETE,
                    entity,
                    IuguSubscription.class);

			return ServiceResponseBuilder.<IuguSubscription>ok()
					.withResult(response.getBody())
					.build();
		} else {
			return ServiceResponseBuilder.<IuguSubscription>error()
					.withMessage(Validations.IUGU_KONKER_PLAN_PAY_KIT_ERROR.getCode())
					.build();
		}
	}

    @Override
    public ServiceResponse<KonkerIuguCharge> findNextCharge(Tenant tenant) {
        if (!Optional.ofNullable(tenant).isPresent()) {
            return ServiceResponseBuilder.<KonkerIuguCharge>error()
                    .withMessage(TenantService.Validations.TENANT_NULL.getCode())
                    .build();
        }

        if (!Optional.ofNullable(tenant.getDomainName()).isPresent()) {
            return ServiceResponseBuilder.<KonkerIuguCharge>error()
                    .withMessage(TenantService.Validations.TENANT_DOMAIN_NULL.getCode())
                    .build();
        }

        HttpHeaders headers = getHttpHeaders();
        HttpEntity<KonkerIuguCharge> entity = new HttpEntity<>(headers);

        ResponseEntity<KonkerIuguCharge> response = restTemplate.exchange(
                format("{0}/tenant/{1}/charges/next",
                        konkerInvoiceApiConfig.getUrl(),
                        tenant.getDomainName()),
                HttpMethod.GET,
                entity,
                KonkerIuguCharge.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return ServiceResponseBuilder.<KonkerIuguCharge>ok()
                    .withResult(response.getBody())
                    .build();
        } else {
            return ServiceResponseBuilder.<KonkerIuguCharge>error()
                    .withMessage(Validations.IUGU_KONKER_CHARGE_NOT_FOUND.getCode())
                    .build();
        }
    }

	@Override
	public ServiceResponse<IuguSubscription> createSubscription(IuguSubscription iuguSubscription) {
		if (!Optional.ofNullable(iuguSubscription).isPresent()) {
			return ServiceResponseBuilder.<IuguSubscription>error()
					.withMessage(Validations.IUGU_SUBSCRIPTION_NULL.getCode())
					.build();
		}

		if (!Optional.ofNullable(iuguSubscription.getCustomerId()).isPresent()) {
			return ServiceResponseBuilder.<IuguSubscription>error()
					.withMessage(Validations.IUGU_SUBSCRIPTION_CUSTOMER_ID_NULL.getCode())
					.build();
		}

		if (!Optional.ofNullable(iuguSubscription.getPlanIdentifier()).isPresent()) {
			return ServiceResponseBuilder.<IuguSubscription>error()
					.withMessage(Validations.IUGU_SUBSCRIPTION_PLAN_IDENTIFIER_NULL.getCode())
					.build();
		}

		HttpHeaders headers = getHttpHeaders();
		HttpEntity<IuguSubscription> entity = new HttpEntity<>(iuguSubscription, headers);

		ResponseEntity<IuguSubscription> response = restTemplate.exchange(
				format("{0}/subscriptions?api_token={1}",
						iuguConfig.getApiURL(),
						iuguConfig.getApiToken()),
				HttpMethod.POST,
				entity,
				IuguSubscription.class);

		if (response.getStatusCode().is2xxSuccessful()) {
			return ServiceResponseBuilder.<IuguSubscription>ok()
					.withResult(response.getBody())
					.build();
		} else {
			return ServiceResponseBuilder.<IuguSubscription>error()
					.withMessage(Validations.IUGU_SUBSCRIPTION_ERROR.getCode())
					.build();
		}
	}

	private HttpHeaders getHttpHeaders() {
		byte[] base64Cred = Base64.encodeBase64(konkerInvoiceApiConfig.getUsername()
				.concat(":")
				.concat(konkerInvoiceApiConfig.getPassword()).getBytes());
		String base64Credentials = new String(base64Cred);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Basic " + base64Credentials);
		return headers;
	}
}
