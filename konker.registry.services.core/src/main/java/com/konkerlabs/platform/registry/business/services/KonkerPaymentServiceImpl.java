package com.konkerlabs.platform.registry.business.services;

import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.services.api.KonkerPaymentService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.business.services.api.TenantService;
import com.konkerlabs.platform.registry.config.KonkerPaymentConfig;
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
public class KonkerPaymentServiceImpl implements KonkerPaymentService {

	private static final Logger LOGGER = LoggerFactory.getLogger(KonkerPaymentServiceImpl.class);

	@Autowired
	private RestTemplate restTemplate;
	private KonkerPaymentConfig konkerPaymentConfig = new KonkerPaymentConfig();

    public KonkerPaymentServiceImpl() {
    }

	@Override
	public ServiceResponse<KonkerPaymentCustomer> createCustomer(KonkerPaymentCustomer konkerPaymentCustomer) {
    	if (!Optional.ofNullable(konkerPaymentCustomer).isPresent()) {
			return ServiceResponseBuilder.<KonkerPaymentCustomer>error()
					.withMessage(Validations.IUGU_CUSTOMER_NULL.getCode())
					.build();
		}

		if (!Optional.ofNullable(konkerPaymentCustomer.getEmail()).isPresent()) {
			return ServiceResponseBuilder.<KonkerPaymentCustomer>error()
					.withMessage(Validations.IUGU_CUSTOMER_EMAIL_NULL.getCode())
					.build();
		}

		if (!Optional.ofNullable(konkerPaymentCustomer.getCustomerName()).isPresent()) {
			return ServiceResponseBuilder.<KonkerPaymentCustomer>error()
					.withMessage(Validations.IUGU_CUSTOMER_NAME_NULL.getCode())
					.build();
		}

		HttpHeaders headers = getHttpHeaders();
		HttpEntity<KonkerPaymentCustomer> entity = new HttpEntity<>(konkerPaymentCustomer, headers);

		ResponseEntity<KonkerPaymentCustomer> response = restTemplate.exchange(
				format("{0}/{1}", konkerPaymentConfig.getUrl(), "customers"),
				HttpMethod.POST,
				entity,
				KonkerPaymentCustomer.class);

		if (response.getStatusCode().is2xxSuccessful()) {
			return ServiceResponseBuilder.<KonkerPaymentCustomer>ok()
					.withResult(response.getBody())
					.build();
		} else {
			return ServiceResponseBuilder.<KonkerPaymentCustomer>error()
					.withMessage(Validations.IUGU_CUSTOMER_CREATION_ERROR.getCode())
					.build();
		}
	}

	@Override
	public ServiceResponse<KonkerKit> payForKit(User user, KonkerKit konkerKit) {
    	if (!Optional.ofNullable(user).isPresent()) {
			return ServiceResponseBuilder.<KonkerKit>error()
					.withMessage(Validations.IUGU_CUSTOMER_NULL.getCode())
					.build();
		}

		if (!Optional.ofNullable(konkerKit).isPresent()) {
			return ServiceResponseBuilder.<KonkerKit>error()
					.withMessage(Validations.IUGU_KONKER_PLAN_NULL.getCode())
					.build();
		}

		HttpHeaders headers = getHttpHeaders();
		HttpEntity<KonkerKit> entity = new HttpEntity<>(konkerKit, headers);

		ResponseEntity<KonkerKit> response = restTemplate.exchange(
				format("{0}/customers/{1}/buy_kit",
						konkerPaymentConfig.getUrl(),
						user.getTenant().getDomainName()),
				HttpMethod.POST,
				entity,
				KonkerKit.class);

		if (response.getStatusCode().is2xxSuccessful()) {
			return ServiceResponseBuilder.<KonkerKit>ok()
					.withResult(response.getBody())
					.build();
		} else {
			return ServiceResponseBuilder.<KonkerKit>error()
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
                format("{0}/customers/{1}/charges/next",
                        konkerPaymentConfig.getUrl(),
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



	private HttpHeaders getHttpHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + konkerPaymentConfig.getApiToken());
		return headers;
	}
}
