package com.konkerlabs.platform.registry.test.business.services;

import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.repositories.UserRepository;
import com.konkerlabs.platform.registry.business.services.api.KonkerPaymentService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.TenantService;
import com.konkerlabs.platform.registry.config.KonkerPaymentConfig;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import java.text.MessageFormat;

import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.hasErrorMessage;
import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.isResponseOk;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        MongoTestConfiguration.class,
        BusinessTestConfiguration.class,
        KonkerPaymentServiceTest.PaymentWayServiceTestConfig.class
})
@UsingDataSet(locations = {
        "/fixtures/tenants.json",
        "/fixtures/users.json"
})
public class KonkerPaymentServiceTest extends BusinessLayerTestSupport {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private KonkerPaymentService konkerPaymentService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    private KonkerPaymentConfig konkerPaymentConfig = new KonkerPaymentConfig();
    private User user;

    @Before
    public void setUp() throws Exception {
        user = userRepository.findOne("admin@konkerlabs.com");
    }

    @After
    public void tearDown() throws Exception {

    }


    @Test
    public void shouldReturnErrorCustomerNull() {
        ServiceResponse<KonkerPaymentCustomer> response = konkerPaymentService.createCustomer(null);

        Assert.assertThat(response, hasErrorMessage(KonkerPaymentService.Validations.IUGU_CUSTOMER_NULL.getCode()));
    }

    @Test
    public void shouldReturnErrorCustomerEmailNull() {
        ServiceResponse<KonkerPaymentCustomer> response = konkerPaymentService.createCustomer(KonkerPaymentCustomer.builder()
                .build());

        Assert.assertThat(response, hasErrorMessage(KonkerPaymentService.Validations.IUGU_CUSTOMER_EMAIL_NULL.getCode()));
    }

    @Test
    public void shouldReturnErrorCustomerNameNull() {
        ServiceResponse<KonkerPaymentCustomer> response = konkerPaymentService.createCustomer(KonkerPaymentCustomer.builder()
                .email("email@teste.com")
                .build());

        Assert.assertThat(response, hasErrorMessage(KonkerPaymentService.Validations.IUGU_CUSTOMER_NAME_NULL.getCode()));
    }

    @Test
    public void shouldCreateIuguCustomer() {
        KonkerPaymentCustomer konkerPaymentCustomer = KonkerPaymentCustomer.builder()
                .email("email@teste.com")
                .customerName("Email Teste")
                .zipCode("05500-100")
                .street("Avenida dos Testes")
                .city("São Paulo")
                .state("SP")
                .build();

        HttpEntity<KonkerPaymentCustomer> request = new HttpEntity<>(konkerPaymentCustomer, getHttpHeaders());
        when(restTemplate.exchange("http://localhost:80/customers",
                HttpMethod.POST,
                request,
                KonkerPaymentCustomer.class))
                .thenReturn(ResponseEntity.ok(KonkerPaymentCustomer.builder().build()));

        ServiceResponse<KonkerPaymentCustomer> response = konkerPaymentService.createCustomer(konkerPaymentCustomer);

        Assert.assertThat(response, isResponseOk());
        Assert.assertNotNull(response.getResult());
    }

    @Test
    public void shouldReturnErrorCreateIuguCustomer() {
        KonkerPaymentCustomer konkerPaymentCustomer = KonkerPaymentCustomer.builder()
                .email("email@teste.com")
                .customerName("Email Teste")
                .zipCode("05500-100")
                .street("Avenida dos Testes")
                .city("São Paulo")
                .state("SP")
                .build();

        HttpEntity<KonkerPaymentCustomer> request = new HttpEntity<>(konkerPaymentCustomer, getHttpHeaders());
        ResponseEntity<KonkerPaymentCustomer> responseEntity = ResponseEntity.badRequest().body(null);
        when(restTemplate.exchange("http://localhost:80/customers",
                HttpMethod.POST,
                request,
                KonkerPaymentCustomer.class))
                .thenReturn(responseEntity);

        ServiceResponse<KonkerPaymentCustomer> response = konkerPaymentService.createCustomer(konkerPaymentCustomer);

        Assert.assertThat(response, hasErrorMessage(KonkerPaymentService.Validations.IUGU_CUSTOMER_CREATION_ERROR.getCode()));
    }

    @Test
    public void shouldReturnErrorPayKitNull() {
        ServiceResponse<KonkerKit> response = konkerPaymentService.payForKit(user, null);

        Assert.assertThat(response, hasErrorMessage(KonkerPaymentService.Validations.IUGU_KONKER_PLAN_NULL.getCode()));
    }

    @Test
    public void shouldReturnErrorPayKitCustomerNull() {
        ServiceResponse<KonkerKit> response = konkerPaymentService.payForKit(null, KonkerKit.builder().build());

        Assert.assertThat(response, hasErrorMessage(KonkerPaymentService.Validations.IUGU_CUSTOMER_NULL.getCode()));
    }

    @Test
    public void shouldPayKit() {
        KonkerKit konkerKit = KonkerKit.builder()
                .amount(2l)
                .build();

        HttpHeaders headers = getHttpHeaders();
        HttpEntity<KonkerKit> request = new HttpEntity<>(konkerKit, headers);
        when(restTemplate.exchange(
                MessageFormat.format("http://localhost:80/customers/{0}/buy_kit", user.getTenant().getDomainName()),
                HttpMethod.POST,
                request,
                KonkerKit.class))
                .thenReturn(ResponseEntity.ok(KonkerKit.builder().build()));

        ServiceResponse<KonkerKit> response = konkerPaymentService.payForKit(user, konkerKit);

        Assert.assertThat(response, isResponseOk());
        Assert.assertNotNull(response.getResult());
    }

    @Test
    public void shouldPayKitErrorToPay() {
        KonkerKit konkerKit = KonkerKit.builder()
                .amount(2l)
                .build();

        HttpHeaders headers = getHttpHeaders();
        HttpEntity<KonkerKit> request = new HttpEntity<>(konkerKit, headers);
        ResponseEntity<KonkerKit> responseEntity = ResponseEntity.badRequest().body(null);
        when(restTemplate.exchange(
                MessageFormat.format("http://localhost:80/customers/{0}/buy_kit", user.getTenant().getDomainName()),
                HttpMethod.POST,
                request,
                KonkerKit.class))
                .thenReturn(responseEntity);

        ServiceResponse<KonkerKit> response = konkerPaymentService.payForKit(user, konkerKit);

        Assert.assertThat(response, hasErrorMessage(KonkerPaymentService.Validations.IUGU_KONKER_PLAN_PAY_KIT_ERROR.getCode()));
    }

    @Test
    public void shouldReturnErrorNextChargeTenantNull() {
        ServiceResponse<KonkerIuguCharge> response = konkerPaymentService.findNextCharge(null);

        Assert.assertThat(response, hasErrorMessage(TenantService.Validations.TENANT_NULL.getCode()));
    }

    @Test
    public void shouldReturnErrorNextChargeTenantDomainNull() {
        ServiceResponse<KonkerIuguCharge> response = konkerPaymentService.findNextCharge(Tenant.builder().build());

        Assert.assertThat(response, hasErrorMessage(TenantService.Validations.TENANT_DOMAIN_NULL.getCode()));
    }

    @Test
    public void shouldFindNextCharge() {
        KonkerIuguCharge konkerIuguCharge = KonkerIuguCharge.builder()
                .nextCharge("01 Jun, 2020")
                .nextChargeValue("R$ 1,99")
                .maskedCardNumber("xxxx xxxx xxxx 4242")
                .build();

        Tenant tenant = Tenant.builder()
                .domainName("xpto99xx")
                .build();

        HttpHeaders headers = getHttpHeaders();
        HttpEntity<KonkerIuguCharge> request = new HttpEntity<>(headers);
        ResponseEntity<KonkerIuguCharge> responseEntity = ResponseEntity.ok().body(konkerIuguCharge);
        when(restTemplate.exchange(
                MessageFormat.format("http://localhost:80/customers/{0}/charges/next", tenant.getDomainName()),
                HttpMethod.GET,
                request,
                KonkerIuguCharge.class))
                .thenReturn(responseEntity);

        ServiceResponse<KonkerIuguCharge> response = konkerPaymentService.findNextCharge(tenant);

        Assert.assertThat(response, isResponseOk());
        Assert.assertNotNull(response.getResult());
    }

    @Test
    public void shouldFindNextChargeNotFound() {
        KonkerIuguCharge konkerIuguCharge = KonkerIuguCharge.builder()
                .nextCharge("01 Jun, 2020")
                .nextChargeValue("R$ 1,99")
                .maskedCardNumber("xxxx xxxx xxxx 4242")
                .build();

        Tenant tenant = Tenant.builder()
                .domainName("xpto99xx")
                .build();

        HttpHeaders headers = getHttpHeaders();
        HttpEntity<KonkerIuguCharge> request = new HttpEntity<>(headers);
        ResponseEntity<KonkerIuguCharge> responseEntity = ResponseEntity.badRequest().body(null);
        when(restTemplate.exchange(
                MessageFormat.format("http://localhost:80/customers/{0}/charges/next", tenant.getDomainName()),
                HttpMethod.GET,
                request,
                KonkerIuguCharge.class))
                .thenReturn(responseEntity);

        ServiceResponse<KonkerIuguCharge> response = konkerPaymentService.findNextCharge(tenant);

        Assert.assertThat(response, hasErrorMessage(KonkerPaymentService.Validations.IUGU_KONKER_CHARGE_NOT_FOUND.getCode()));
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + konkerPaymentConfig.getApiToken());
        return headers;
    }

    static class PaymentWayServiceTestConfig {
        @Bean
        public RestTemplate restTemplate() {
            return Mockito.mock(RestTemplate.class);
        }
    }

}
