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

import java.util.*;

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

        private HashMap<String, Object> expectedFlattenMap;
        private HashMap<String, Object> expectedMap;
        private String expectedJsonString;

        @Before
        public void setUp() throws Exception {
            expectedFlattenMap = new HashMap<>();
            expectedFlattenMap.put("ts", "2016-03-03T18:15:00Z");
            expectedFlattenMap.put("value", 31.0);
            expectedFlattenMap.put("command.type", "ButtonPressed");
            expectedFlattenMap.put("data.channels.0.name", "channel_0");
            expectedFlattenMap.put("time", 123L);

            expectedMap = new HashMap<>();
            expectedMap.put("ts", "2016-03-03T18:15:00Z");
            expectedMap.put("value", 31.0);
            expectedMap.put("command", new HashMap<String, String>() {{
                put("type", "ButtonPressed");
            }});
            expectedMap.put("data", new HashMap<String, List<HashMap<String, String>>>() {{
                put("channels", new ArrayList<HashMap<String, String>>() {{
                    add(new HashMap<String, String>() {{
                        put("name", "channel_0");
                    }});
                }});
            }});
            expectedMap.put("time", 123);

            expectedJsonString = "{\"data\":{\"channels\":[{\"name\":\"channel_0\"}]}," +
                    "\"time\":123," +
                    "\"value\":31.0," +
                    "\"command\":{\"type\":\"ButtonPressed\"}," +
                    "\"ts\":\"2016-03-03T18:15:00Z\"}";
        }

        @Test
        public void shouldRaiseAnExceptionIfJsonIsNullOnToFlatMap() throws Exception {
            thrown.expect(IllegalArgumentException.class);
            thrown.expectMessage("JSON cannot be null or empty");

            service.toFlatMap(null);
        }

        @Test
        public void shouldRaiseAnExceptionIfJsonIsEmptyOnToFlatMap() throws Exception {
            thrown.expect(IllegalArgumentException.class);
            thrown.expectMessage("JSON cannot be null or empty");

            service.toFlatMap("");
        }

        @Test
        public void shouldRaiseAnExceptionIfJsonIsNotValidOnToFlatMap() throws Exception {
            thrown.expect(JsonProcessingException.class);

            service.toFlatMap(invalidJson);
        }

        @Test
        public void shouldRaiseAnExceptionIfJsonIsNullOnToMap() throws Exception {
            thrown.expect(IllegalArgumentException.class);
            thrown.expectMessage("JSON cannot be null or empty");

            service.toMap(null);
        }

        @Test
        public void shouldRaiseAnExceptionIfJsonIsEmptyOnToMap() throws Exception {
            thrown.expect(IllegalArgumentException.class);
            thrown.expectMessage("JSON cannot be null or empty");

            service.toMap("");
        }

        @Test
        public void shouldRaiseAnExceptionIfJsonIsNotValidOnToMap() throws Exception {
            thrown.expect(JsonProcessingException.class);

            service.toMap(invalidJson);
        }

        @Test
        public void shouldRaiseAnExceptionIfJsonIsNullOnToJsonString() throws Exception {
            thrown.expect(IllegalArgumentException.class);
            thrown.expectMessage("Map cannot be null or empty");

            service.toJsonString(null);
        }

        @Test
        public void shouldRaiseAnExceptionIfJsonIsEmptyOnToJsonString() throws Exception {
            thrown.expect(IllegalArgumentException.class);
            thrown.expectMessage("Map cannot be null or empty");

            service.toJsonString(new HashMap<String, Object>() {
            });
        }

        @Test
        public void shouldCreateAFlatMap() throws Exception {
            Map<String, Object> actual = service.toFlatMap(validJson);

            assertThat(actual, notNullValue());
            assertThat(actual, equalTo(expectedFlattenMap));
        }

        @Test
        public void shouldCreateAMap() throws Exception {
            Map<String, Object> actual = service.toMap(validJson);

            assertThat(actual, notNullValue());
            assertThat(actual, equalTo(expectedMap));
        }

        @Test
        public void shouldCreateAJsonString() throws Exception {
            String actual = service.toJsonString(expectedMap);

            assertThat(actual, notNullValue());
            assertThat(actual, equalTo(expectedJsonString));
        }
    }

}