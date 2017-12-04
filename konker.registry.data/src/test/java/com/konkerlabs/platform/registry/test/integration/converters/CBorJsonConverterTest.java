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
        String json = "[1, 2, 3]";

        ServiceResponse<byte[]> fromJsonResponse = converter.fromJson(json);
        assertThat(fromJsonResponse.isOk(), Matchers.is(true));

        byte[] cborBytes = fromJsonResponse.getResult();

        ServiceResponse<String> toJsonResponse = converter.toJson(cborBytes);
        assertThat(toJsonResponse.isOk(), Matchers.is(true));
        assertThat(toJsonResponse.getResult(), Matchers.is("[1, 2, 3]"));

    }

    @Test
    public void testJsonToCBorBytesToJson() {

        String json = "{\"temp\": 34, \"unit\": \"C\"}";

        ServiceResponse<byte[]> fromJsonResponse = converter.fromJson(json);
        assertThat(fromJsonResponse.isOk(), Matchers.is(true));

        byte[] cborBytes = fromJsonResponse.getResult();

        ServiceResponse<String> toJsonResponse = converter.toJson(cborBytes);
        assertThat(toJsonResponse.isOk(), Matchers.is(true));
        assertThat(toJsonResponse.getResult(), Matchers.is("{\"temp\": 34, \"unit\": \"C\"}"));

    }

    @Test
    public void shouldReturnErrorIfConvertInvalidCBor() {
        byte cborData[] = {(byte)-120, (byte)25, (byte)123, (byte)34, (byte)116, (byte)101, 
        				   (byte)109, (byte)112, (byte)34, (byte)58, (byte)32, (byte)51, 
        				   (byte)52, (byte)44, (byte)32, (byte)34, (byte)117, (byte)110, 
        				   (byte)105, (byte)116, (byte)34, (byte)58, (byte)32, (byte)34, 
        				   (byte)67, (byte)34, (byte)125};

        ServiceResponse<String> fromJsonResponse = converter.toJson(cborData);
        assertThat(fromJsonResponse.isOk(), Matchers.is(false));

    }

}
