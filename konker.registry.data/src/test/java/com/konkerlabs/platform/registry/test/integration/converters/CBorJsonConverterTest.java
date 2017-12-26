package com.konkerlabs.platform.registry.test.integration.converters;

import static org.hamcrest.MatcherAssert.assertThat;

import org.hamcrest.Matchers;
import org.junit.Test;

import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.integration.converters.CBorJsonConverter;

public class CBorJsonConverterTest {
	
	private CBorJsonConverter converter = new CBorJsonConverter();
	
	@Test
    public void testJsonArrayToCBorBytesToJson() {
        String json = "[1,2,3]";

        ServiceResponse<byte[]> fromJsonResponse = converter.fromJson(json);
        assertThat(fromJsonResponse.isOk(), Matchers.is(true));

        byte[] cborBytes = fromJsonResponse.getResult();

        ServiceResponse<String> toJsonResponse = converter.toJson(cborBytes);
        assertThat(toJsonResponse.isOk(), Matchers.is(true));
        assertThat(toJsonResponse.getResult(), Matchers.is("\"[1,2,3]\""));

    }

    @Test
    public void testJsonToCBorBytesToJson() {

        String json = "{\"temp\":34,\"unit\":\"C\"}";

        ServiceResponse<byte[]> fromJsonResponse = converter.fromJson(json);
        assertThat(fromJsonResponse.isOk(), Matchers.is(true));

        byte[] cborBytes = fromJsonResponse.getResult();

        ServiceResponse<String> toJsonResponse = converter.toJson(cborBytes);
        assertThat(toJsonResponse.isOk(), Matchers.is(true));
        assertThat(toJsonResponse.getResult(), Matchers.is("\"{\\\"temp\\\":34,\\\"unit\\\":\\\"C\\\"}\""));

    }

    @Test
    public void shouldReturnErrorIfConvertInvalidCBor() {
        byte cborData[] = {(byte)-129, (byte)1, (byte)2, (byte)3};

        ServiceResponse<String> fromJsonResponse = converter.toJson(cborData);
        assertThat(fromJsonResponse.isOk(), Matchers.is(false));

    }

}