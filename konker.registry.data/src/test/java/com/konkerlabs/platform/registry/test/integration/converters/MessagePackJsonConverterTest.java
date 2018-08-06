package com.konkerlabs.platform.registry.test.integration.converters;

import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.integration.converters.MessagePackJsonConverter;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

public class MessagePackJsonConverterTest {

    private final MessagePackJsonConverter subject = new MessagePackJsonConverter();

    @Test
    public void testJsonToBytesToJson() {

        String json = "[1, 2, 3]";

        ServiceResponse<byte[]> fromJsonResponse = subject.fromJson(json);
        assertThat(fromJsonResponse.isOk(), Matchers.is(true));

        byte[] messagePackBytes = fromJsonResponse.getResult();

        ServiceResponse<String> toJsonResponse = subject.toJson(messagePackBytes);
        assertThat(toJsonResponse.isOk(), Matchers.is(true));
        assertThat(toJsonResponse.getResult(), Matchers.is("[1,2,3]"));

    }

    @Test
    public void testJsonToBytesToJson_2() {

        String json = "{\"temp\": 34, \"unit\": \"C\"}";

        ServiceResponse<byte[]> fromJsonResponse = subject.fromJson(json);
        assertThat(fromJsonResponse.isOk(), Matchers.is(true));

        byte[] messagePackBytes = fromJsonResponse.getResult();

        ServiceResponse<String> toJsonResponse = subject.toJson(messagePackBytes);
        assertThat(toJsonResponse.isOk(), Matchers.is(true));
        assertThat(toJsonResponse.getResult(), Matchers.is("{\"temp\":34,\"unit\":\"C\"}"));

    }

    @Test
    public void shouldReturnErrorIfConvertInvalidJson() {

        String json = "[1, 2, 'null']";

        ServiceResponse<byte[]> fromJsonResponse = subject.fromJson(json);
        assertThat(fromJsonResponse.isOk(), Matchers.is(false));

    }

    @Test
    public void shouldReturnErrorIfConvertInvalidMessagePack() {

        byte msgPackBytes[] = {(byte) -101,(byte) 2,(byte) 3,(byte) 100,(byte) 100};

        ServiceResponse<String> fromJsonResponse = subject.toJson(msgPackBytes);
        assertThat(fromJsonResponse.isOk(), Matchers.is(false));

    }


}
