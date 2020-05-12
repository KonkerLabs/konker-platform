package com.konkerlabs.platform.registry.test.business.services;

import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.services.api.IuguService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.TenantService;
import com.konkerlabs.platform.registry.config.KonkerInvoiceApiConfig;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import org.apache.commons.codec.binary.Base64;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.hasErrorMessage;
import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.isResponseOk;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        MongoTestConfiguration.class,
        BusinessTestConfiguration.class,
        IuguServiceTest.PaymentWayServiceTestConfig.class
})
public class IuguServiceTest extends BusinessLayerTestSupport {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private IuguService iuguService;

    @Autowired
    private RestTemplate restTemplate;

    private KonkerInvoiceApiConfig konkerInvoiceApiConfig = new KonkerInvoiceApiConfig();

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }


    @Test
    public void shouldReturnErrorCustomerNull() {
        ServiceResponse<IuguCustomer> response = iuguService.createIuguCustomer(null);

        Assert.assertThat(response, hasErrorMessage(IuguService.Validations.IUGU_CUSTOMER_NULL.getCode()));
    }

    @Test
    public void shouldReturnErrorCustomerEmailNull() {
        ServiceResponse<IuguCustomer> response = iuguService.createIuguCustomer(IuguCustomer.builder()
                .build());

        Assert.assertThat(response, hasErrorMessage(IuguService.Validations.IUGU_CUSTOMER_EMAIL_NULL.getCode()));
    }

    @Test
    public void shouldReturnErrorCustomerNameNull() {
        ServiceResponse<IuguCustomer> response = iuguService.createIuguCustomer(IuguCustomer.builder()
                .email("email@teste.com")
                .build());

        Assert.assertThat(response, hasErrorMessage(IuguService.Validations.IUGU_CUSTOMER_NAME_NULL.getCode()));
    }

    @Test
    public void shouldCreateIuguCustomer() {
        IuguCustomer iuguCustomer = IuguCustomer.builder()
                .email("email@teste.com")
                .name("Iugu Customer")
                .zipCode("05500-100")
                .street("Avenida dos Testes")
                .city("São Paulo")
                .state("SP")
                .build();

        HttpEntity<IuguCustomer> request = new HttpEntity<>(iuguCustomer);
        when(restTemplate.exchange("https://api.iugu.com/v1/customers?api_token=b17421313f9a8db907afa7b7047fbcd8",
                HttpMethod.POST,
                request,
                IuguCustomer.class))
                .thenReturn(ResponseEntity.ok(IuguCustomer.builder().id("77C2565F6F064A26ABED4255894224F0").build()));

        ServiceResponse<IuguCustomer> response = iuguService.createIuguCustomer(iuguCustomer);

        Assert.assertThat(response, isResponseOk());
        Assert.assertNotNull(response.getResult());
        Assert.assertNotNull(response.getResult().getId());
    }

    @Test
    public void shouldReturnErrorCreateIuguCustomer() {
        IuguCustomer iuguCustomer = IuguCustomer.builder()
                .email("email@teste.com")
                .name("Iugu Customer")
                .zipCode("05500-100")
                .street("Avenida dos Testes")
                .city("São Paulo")
                .state("SP")
                .build();

        HttpEntity<IuguCustomer> request = new HttpEntity<>(iuguCustomer);
        ResponseEntity<IuguCustomer> responseEntity = ResponseEntity.badRequest().body(null);
        when(restTemplate.exchange("https://api.iugu.com/v1/customers?api_token=b17421313f9a8db907afa7b7047fbcd8",
                HttpMethod.POST,
                request,
                IuguCustomer.class))
                .thenReturn(responseEntity);

        ServiceResponse<IuguCustomer> response = iuguService.createIuguCustomer(iuguCustomer);

        Assert.assertThat(response, hasErrorMessage(IuguService.Validations.IUGU_CUSTOMER_CREATION_ERROR.getCode()));
    }

    @Test
    public void shouldReturnErrorPaymentWayNull() {
        ServiceResponse<IuguPaymentWay> response = iuguService.createPaymentWay(null);

        Assert.assertThat(response, hasErrorMessage(IuguService.Validations.IUGU_PAYMENT_WAY_NULL.getCode()));
    }

    @Test
    public void shouldReturnErrorPaymentWayCustomerIdNull() {
        ServiceResponse<IuguPaymentWay> response = iuguService.createPaymentWay(IuguPaymentWay.builder().build());

        Assert.assertThat(response, hasErrorMessage(IuguService.Validations.IUGU_PAYMENT_WAY_CUSTOMER_ID_NULL.getCode()));
    }

    @Test
    public void shouldReturnErrorPaymentWayTokenNull() {
        ServiceResponse<IuguPaymentWay> response = iuguService.createPaymentWay(IuguPaymentWay.builder()
                .customerId("9B41FB19CBA44913B1EF990A10382E7E")
                .build());

        Assert.assertThat(response, hasErrorMessage(IuguService.Validations.IUGU_PAYMENT_WAY_TOKEN_NULL.getCode()));
    }

    @Test
    public void shouldCreateIuguPaymentWay() {
        IuguPaymentWay iuguPaymentWay = IuguPaymentWay.builder()
                .customerId("77C2565F6F064A26ABED4255894224F0")
                .description("Forma de pagamento iugu")
                .token("948afce0-d247-4f30-8909-4942722ec335")
                .setAsDefault(true)
                .build();

        HttpEntity<IuguPaymentWay> request = new HttpEntity<>(iuguPaymentWay);
        when(restTemplate.exchange(
                "https://api.iugu.com/v1/customers/77C2565F6F064A26ABED4255894224F0/payment_methods?api_token=b17421313f9a8db907afa7b7047fbcd8",
                HttpMethod.POST,
                request,
                IuguPaymentWay.class))
                .thenReturn(ResponseEntity.ok(IuguPaymentWay.builder().id("77C2565F6F064A26ABED4255894224F0").build()));

        ServiceResponse<IuguPaymentWay> response = iuguService.createPaymentWay(iuguPaymentWay);

        Assert.assertThat(response, isResponseOk());
        Assert.assertNotNull(response.getResult());
        Assert.assertNotNull(response.getResult().getId());
    }

    @Test
    public void shouldReturnErrorCreateIuguPaymentWay() {
        IuguPaymentWay iuguPaymentWay = IuguPaymentWay.builder()
                .customerId("77C2565F6F064A26ABED4255894224F0")
                .description("Forma de pagamento iugu")
                .token("948afce0-d247-4f30-8909-4942722ec335")
                .setAsDefault(true)
                .build();

        HttpEntity<IuguPaymentWay> request = new HttpEntity<>(iuguPaymentWay);
        ResponseEntity<IuguPaymentWay> responseEntity = ResponseEntity.badRequest().body(null);
        when(restTemplate.exchange(
                "https://api.iugu.com/v1/customers/77C2565F6F064A26ABED4255894224F0/payment_methods?api_token=b17421313f9a8db907afa7b7047fbcd8",
                HttpMethod.POST,
                request,
                IuguPaymentWay.class))
                .thenReturn(responseEntity);

        ServiceResponse<IuguPaymentWay> response = iuguService.createPaymentWay(iuguPaymentWay);

        Assert.assertThat(response, hasErrorMessage(IuguService.Validations.IUGU_PAYMENT_WAY_CREATION_ERROR.getCode()));
    }

    @Test
    public void shouldReturnErrorKonkerIuguPlanNull() {
        ServiceResponse<KonkerIuguPlan> response = iuguService.createKonkerIuguPlan(null);

        Assert.assertThat(response, hasErrorMessage(IuguService.Validations.IUGU_KONKER_PLAN_NULL.getCode()));
    }

    @Test
    public void shouldReturnErrorKonkerIuguPlanTenantDomainNull() {
        ServiceResponse<KonkerIuguPlan> response = iuguService.createKonkerIuguPlan(KonkerIuguPlan.builder().build());

        Assert.assertThat(response, hasErrorMessage(IuguService.Validations.IUGU_KONKER_PLAN_TENANT_DOMAIN_NULL.getCode()));
    }

    @Test
    public void shouldReturnErrorKonkerIuguPlanTenantNameNull() {
        ServiceResponse<KonkerIuguPlan> response = iuguService.createKonkerIuguPlan(KonkerIuguPlan.builder()
                .tenantDomain("xpto99")
                .build());

        Assert.assertThat(response, hasErrorMessage(IuguService.Validations.IUGU_KONKER_PLAN_TENANT_NAME_NULL.getCode()));
    }

    @Test
    public void shouldReturnErrorKonkerIuguPlanCutomerIdNull() {
        ServiceResponse<KonkerIuguPlan> response = iuguService.createKonkerIuguPlan(KonkerIuguPlan.builder()
                .tenantDomain("xpto99")
                .tenantName("XPTO")
                .build());

        Assert.assertThat(response, hasErrorMessage(IuguService.Validations.IUGU_KONKER_PLAN_CUSTOMER_ID_NULL.getCode()));
    }

    @Test
    public void shouldReturnErrorKonkerIuguPlanIdentifierNull() {
        ServiceResponse<KonkerIuguPlan> response = iuguService.createKonkerIuguPlan(KonkerIuguPlan.builder()
                .tenantDomain("xpto99")
                .tenantName("XPTO")
                .iuguCustomerId("77C2565F6F064A26ABED4255894224F0")
                .build());

        Assert.assertThat(response, hasErrorMessage(IuguService.Validations.IUGU_KONKER_PLAN_IDENTIFIER_NULL.getCode()));
    }

    @Test
    public void shouldCreateKonkerIuguPlan() {
        KonkerIuguPlan konkerIuguPlan = KonkerIuguPlan.builder()
                .tenantDomain("xpto99")
                .tenantName("XPTO")
                .iuguCustomerId("77C2565F6F064A26ABED4255894224F0")
                .iuguPlanIdentifier("STARTER")
                .build();

        HttpHeaders headers = getHttpHeaders();
        HttpEntity<KonkerIuguPlan> request = new HttpEntity<>(konkerIuguPlan, headers);
        ResponseEntity<KonkerIuguPlan> responseEntity = ResponseEntity.ok().body(KonkerIuguPlan.builder().build());
        when(restTemplate.exchange(
                "http://localhost:8000/tenant/plan",
                HttpMethod.POST,
                request,
                KonkerIuguPlan.class))
                .thenReturn(responseEntity);

        ServiceResponse<KonkerIuguPlan> response = iuguService.createKonkerIuguPlan(konkerIuguPlan);

        Assert.assertThat(response, isResponseOk());
        Assert.assertNotNull(response.getResult());
    }

    @Test
    public void shouldReturnErrorCreateKonkerIuguPlan() {
        KonkerIuguPlan konkerIuguPlan = KonkerIuguPlan.builder()
                .tenantDomain("xpto99")
                .tenantName("XPTO")
                .iuguCustomerId("77C2565F6F064A26ABED4255894224F0")
                .iuguPlanIdentifier("STARTER")
                .build();

        HttpHeaders headers = getHttpHeaders();
        HttpEntity<KonkerIuguPlan> request = new HttpEntity<>(konkerIuguPlan, headers);
        ResponseEntity<KonkerIuguPlan> responseEntity = ResponseEntity.badRequest().body(null);
        when(restTemplate.exchange(
                "http://localhost:8000/tenant/plan",
                HttpMethod.POST,
                request,
                KonkerIuguPlan.class))
                .thenReturn(responseEntity);

        ServiceResponse<KonkerIuguPlan> response = iuguService.createKonkerIuguPlan(konkerIuguPlan);

        Assert.assertThat(response, hasErrorMessage(IuguService.Validations.IUGU_KONKER_PLAN_CREATION_ERROR.getCode()));
    }

    @Test
    public void shouldReturnErrorPayKitNull() {
        ServiceResponse<IuguSubscription> response = iuguService.payForKit(null);

        Assert.assertThat(response, hasErrorMessage(IuguService.Validations.IUGU_KONKER_PLAN_NULL.getCode()));
    }

    @Test
    public void shouldReturnErrorPayKitCustomerIdNull() {
        ServiceResponse<IuguSubscription> response = iuguService.payForKit(KonkerIuguPlan.builder().build());

        Assert.assertThat(response, hasErrorMessage(IuguService.Validations.IUGU_KONKER_PLAN_CUSTOMER_ID_NULL.getCode()));
    }

    @Test
    public void shouldPayKit() {
        KonkerIuguPlan konkerIuguPlan = KonkerIuguPlan.builder()
                .tenantDomain("xpto99")
                .tenantName("XPTO")
                .iuguCustomerId("77C2565F6F064A26ABED4255894224F0")
                .iuguPlanIdentifier("STARTER")
                .build();

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
                        IuguSubscription.Item.builder().description("NodeMCU (placa de desenvolvimento contendo um ESP8266)").priceCents(0l).quantity(1l).recurrent(true).build(),
                        IuguSubscription.Item.builder().description("Protoboards de 170 pontos").priceCents(0l).quantity(2l).recurrent(true).build(),
                        IuguSubscription.Item.builder().description("Termistor de 1k Ohm").priceCents(0l).quantity(1l).recurrent(true).build(),
                        IuguSubscription.Item.builder().description("Resistores de 470 Ohms").priceCents(0l).quantity(4l).recurrent(true).build()
                ))
                .build();

        HttpHeaders headers = getHttpHeaders();
        HttpEntity<IuguSubscription> request = new HttpEntity<>(iuguSubscription, headers);
        when(restTemplate.exchange("https://api.iugu.com/v1/subscriptions?api_token=b17421313f9a8db907afa7b7047fbcd8",
                HttpMethod.POST,
                request,
                IuguSubscription.class))
                .thenReturn(ResponseEntity.ok(IuguSubscription.builder().id("77C2565F6F064A26ABED4255894224FF").build()));

        ServiceResponse<IuguSubscription> response = iuguService.payForKit(konkerIuguPlan);

        Assert.assertThat(response, isResponseOk());
        Assert.assertNotNull(response.getResult());
        Assert.assertNotNull(response.getResult().getId());
    }

    @Test
    public void shouldPayKitErrorToPay() {
        KonkerIuguPlan konkerIuguPlan = KonkerIuguPlan.builder()
                .tenantDomain("xpto99")
                .tenantName("XPTO")
                .iuguCustomerId("77C2565F6F064A26ABED4255894224F0")
                .iuguPlanIdentifier("STARTER")
                .build();

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
                        IuguSubscription.Item.builder().description("NodeMCU (placa de desenvolvimento contendo um ESP8266)").priceCents(0l).quantity(1l).recurrent(true).build(),
                        IuguSubscription.Item.builder().description("Protoboards de 170 pontos").priceCents(0l).quantity(2l).recurrent(true).build(),
                        IuguSubscription.Item.builder().description("Termistor de 1k Ohm").priceCents(0l).quantity(1l).recurrent(true).build(),
                        IuguSubscription.Item.builder().description("Resistores de 470 Ohms").priceCents(0l).quantity(4l).recurrent(true).build()
                ))
                .build();

        HttpHeaders headers = getHttpHeaders();
        HttpEntity<IuguSubscription> request = new HttpEntity<>(iuguSubscription, headers);
        ResponseEntity<IuguSubscription> responseEntity = ResponseEntity.badRequest().body(null);
        when(restTemplate.exchange("https://api.iugu.com/v1/subscriptions?api_token=b17421313f9a8db907afa7b7047fbcd8",
                HttpMethod.POST,
                request,
                IuguSubscription.class))
                .thenReturn(responseEntity);

        ServiceResponse<IuguSubscription> response = iuguService.payForKit(konkerIuguPlan);

        Assert.assertThat(response, hasErrorMessage(IuguService.Validations.IUGU_KONKER_PLAN_PAY_KIT_ERROR.getCode()));
    }

    @Test
    public void shouldReturnErrorNextChargeTenantNull() {
        ServiceResponse<KonkerIuguCharge> response = iuguService.findNextCharge(null);

        Assert.assertThat(response, hasErrorMessage(TenantService.Validations.TENANT_NULL.getCode()));
    }

    @Test
    public void shouldReturnErrorNextChargeTenantDomainNull() {
        ServiceResponse<KonkerIuguCharge> response = iuguService.findNextCharge(Tenant.builder().build());

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
                MessageFormat.format("http://localhost:8000/tenant/{0}/charges/next", tenant.getDomainName()),
                HttpMethod.GET,
                request,
                KonkerIuguCharge.class))
                .thenReturn(responseEntity);

        ServiceResponse<KonkerIuguCharge> response = iuguService.findNextCharge(tenant);

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
                MessageFormat.format("http://localhost:8000/tenant/{0}/charges/next", tenant.getDomainName()),
                HttpMethod.GET,
                request,
                KonkerIuguCharge.class))
                .thenReturn(responseEntity);

        ServiceResponse<KonkerIuguCharge> response = iuguService.findNextCharge(tenant);

        Assert.assertThat(response, hasErrorMessage(IuguService.Validations.IUGU_KONKER_CHARGE_NOT_FOUND.getCode()));
    }

    @Test
    public void shouldReturnErrorIuguSubscriptionNull() {
        ServiceResponse<IuguSubscription> response = iuguService.createSubscription(null);

        Assert.assertThat(response, hasErrorMessage(IuguService.Validations.IUGU_SUBSCRIPTION_NULL.getCode()));
    }

    @Test
    public void shouldReturnErrorIuguSubscriptionCustomerIdNull() {
        ServiceResponse<IuguSubscription> response = iuguService.createSubscription(IuguSubscription.builder().build());

        Assert.assertThat(response, hasErrorMessage(IuguService.Validations.IUGU_SUBSCRIPTION_CUSTOMER_ID_NULL.getCode()));
    }

    @Test
    public void shouldReturnErrorIuguSubscriptionPlanIdentifierNull() {
        ServiceResponse<IuguSubscription> response = iuguService.createSubscription(IuguSubscription.builder()
                .customerId("77C2565F6F064A26ABED4255894224F0")
                .build());

        Assert.assertThat(response, hasErrorMessage(IuguService.Validations.IUGU_SUBSCRIPTION_PLAN_IDENTIFIER_NULL.getCode()));
    }

    @Test
    public void shouldCreateIuguSubscription() {
        IuguSubscription iuguSubscription = IuguSubscription.builder()
                .planIdentifier("STARTER")
                .customerId("77C2565F6F064A26ABED4255894224F0")
                .expiresAt(LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
                .onlyOnChargeSuccess("true")
                .ignoreDueEmail("false")
                .payableWith("credit_card")
                .creditsBased("false")
                .twoStep("false")
                .suspendOnInvoiceExpired("false")
                .build();

        HttpHeaders headers = getHttpHeaders();
        HttpEntity<IuguSubscription> request = new HttpEntity<>(iuguSubscription, headers);
        when(restTemplate.exchange("https://api.iugu.com/v1/subscriptions?api_token=b17421313f9a8db907afa7b7047fbcd8",
                HttpMethod.POST,
                request,
                IuguSubscription.class))
                .thenReturn(ResponseEntity.ok(IuguSubscription.builder().id("77C2565F6F064A26ABED4255894224FF").build()));

        ServiceResponse<IuguSubscription> response = iuguService.createSubscription(iuguSubscription);

        Assert.assertThat(response, isResponseOk());
        Assert.assertNotNull(response.getResult());
        Assert.assertNotNull(response.getResult().getId());
    }

    @Test
    public void shouldCreateIuguSubscriptionError() {
        IuguSubscription iuguSubscription = IuguSubscription.builder()
                .planIdentifier("STARTER")
                .customerId("77C2565F6F064A26ABED4255894224F0")
                .expiresAt(LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
                .onlyOnChargeSuccess("true")
                .ignoreDueEmail("false")
                .payableWith("credit_card")
                .creditsBased("false")
                .twoStep("false")
                .suspendOnInvoiceExpired("false")
                .build();

        HttpHeaders headers = getHttpHeaders();
        HttpEntity<IuguSubscription> request = new HttpEntity<>(iuguSubscription, headers);
        ResponseEntity<IuguSubscription> responseEntity = ResponseEntity.badRequest().body(null);
        when(restTemplate.exchange("https://api.iugu.com/v1/subscriptions?api_token=b17421313f9a8db907afa7b7047fbcd8",
                HttpMethod.POST,
                request,
                IuguSubscription.class))
                .thenReturn(responseEntity);

        ServiceResponse<IuguSubscription> response = iuguService.createSubscription(iuguSubscription);

        Assert.assertThat(response, hasErrorMessage(IuguService.Validations.IUGU_SUBSCRIPTION_ERROR.getCode()));
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

    static class PaymentWayServiceTestConfig {
        @Bean
        public RestTemplate restTemplate() {
            return Mockito.mock(RestTemplate.class);
        }
    }
   
}
