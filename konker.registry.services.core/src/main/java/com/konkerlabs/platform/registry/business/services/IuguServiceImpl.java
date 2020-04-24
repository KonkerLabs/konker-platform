package com.konkerlabs.platform.registry.business.services;

import com.konkerlabs.platform.registry.business.model.IuguCustomer;
import com.konkerlabs.platform.registry.business.model.IuguPaymentWay;
import com.konkerlabs.platform.registry.business.model.KonkerIuguPlan;
import com.konkerlabs.platform.registry.business.services.api.IuguService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
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
				format("{0}/tenantPlan",
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
					.withMessage(Validations.IUGU_PAYMENT_WAY_CREATION_ERROR.getCode())
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
