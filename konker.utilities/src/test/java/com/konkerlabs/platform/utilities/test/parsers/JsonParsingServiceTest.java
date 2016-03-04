package com.konkerlabs.platform.utilities.test.parsers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.konkerlabs.platform.utilities.config.UtilitiesConfig;
import com.konkerlabs.platform.utilities.parsers.json.JsonParsingService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    JsonParsingServiceTest.FlatMap.class
})
public class JsonParsingServiceTest {

    public static class JsonParsingServiceTestBase {

        @Rule
        public ExpectedException thrown = ExpectedException.none();

    }

    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(classes = {
        UtilitiesConfig.class
    })
    public static class FlatMap extends JsonParsingServiceTestBase {

        private String invalidJson = "{\n" +
                "    \"ts\" : \"2016-03-03T18:15:00Z\",\n" +
                "    \"value\" : 31.0\n" +
                "    \"command : {\n" +
                "      \"type\" : \"ButtonPressed\"\n" +
                "      },\n" +
                "    \"data\" : {\n" +
                "      \"channels\" : [\n" +
                "        { \"name\" : \"channel_0\" }\n" +
                "      ]\n" +
                "    },\n" +
                "    \"time\" : 123\n" +
                "  }";

        private String validJson = "{\n" +
                "    \"ts\" : \"2016-03-03T18:15:00Z\",\n" +
                "    \"value\" : 31.0,\n" +
                "    \"command\" : {\n" +
                "      \"type\" : \"ButtonPressed\"\n" +
                "      },\n" +
                "    \"data\" : {\n" +
                "      \"channels\" : [\n" +
                "        { \"name\" : \"channel_0\" }\n" +
                "      ]\n" +
                "    },\n" +
                "    \"time\" : 123\n" +
                "  }";

        @Autowired
        private JsonParsingService service;
        private HashMap<String, Object> expected;

        @Before
        public void setUp() throws Exception {
            expected = new HashMap<>();

            expected.put("ts","2016-03-03T18:15:00Z");
            expected.put("value",31.0);
            expected.put("command.type","ButtonPressed");
            expected.put("data.channels.0.name","channel_0");
            expected.put("time",123L);
        }

        @Test
        public void shouldRaiseAnExceptionIfJsonIsNull() throws Exception {
            thrown.expect(IllegalArgumentException.class);
            thrown.expectMessage("JSON cannot be null or empty");

            service.toFlatMap(null);
        }
        @Test
        public void shouldRaiseAnExceptionIfJsonIsEmpty() throws Exception {
            thrown.expect(IllegalArgumentException.class);
            thrown.expectMessage("JSON cannot be null or empty");

            service.toFlatMap("");
        }
        @Test
        public void shouldRaiseAnExceptionIfJsonIsNotValid() throws Exception {
            thrown.expect(JsonProcessingException.class);

            service.toFlatMap(invalidJson);
        }
        @Test
        public void shouldCreateAFlatMap() throws Exception {
            Map<String,Object> actual = service.toFlatMap(validJson);

            assertThat(actual,notNullValue());
            assertThat(actual,equalTo(expected));
        }

    }

}