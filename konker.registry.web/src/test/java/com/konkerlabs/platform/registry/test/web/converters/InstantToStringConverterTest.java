package com.konkerlabs.platform.registry.test.web.converters;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.konkerlabs.platform.registry.config.WebConfig;
import com.konkerlabs.platform.registry.config.WebMvcConfig;
import com.konkerlabs.platform.registry.test.base.WebTestConfiguration;
import com.konkerlabs.platform.registry.web.converters.InstantToStringConverter;
import com.konkerlabs.platform.registry.web.converters.utils.ConverterUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = { WebMvcConfig.class, WebTestConfiguration.class,
		InstantToStringConverterTest.InstantToStringConverterTestContextConfig.class,
		WebConfig.class})
public class InstantToStringConverterTest {

	@Autowired
	private ConverterUtils utils;

	@Autowired
	private InstantToStringConverter converter;

	Instant date;
	String dateInput = "2010-10-12T10:10:10Z";

	@Before
	public void setUp() {
		date = Instant.parse(dateInput);
	}

	@Test
	public void shouldConvertInstantToUsTimezoneAndLocale() throws Exception {
		when(utils.getCurrentLocale()).thenReturn(Locale.US);
		when(utils.getDateTimeFormatPattern()).thenReturn("MM/dd/yyyy HH:mm:ss.SSS zzz");
		when(utils.getUserZoneID()).thenReturn("America/Sao_Paulo");
		String formattedDate = converter.convert(date);
		// America/Sao_Paulo Timezone is UTC - 3 (without DS)
		String expected = "10/12/2010 07:10:10.000 BRT";
		assertThat(formattedDate, equalTo(expected));
	}

	@Test
	public void shouldConvertInstantToPtBrTimezoneAndLocale() throws Exception {
		when(utils.getCurrentLocale()).thenReturn(new Locale.Builder().setLanguage("pt").setRegion("BR").build());
		when(utils.getDateTimeFormatPattern()).thenReturn("dd/MM/yyyy HH:mm:ss.SSS zzz");
		when(utils.getUserZoneID()).thenReturn("America/Sao_Paulo");
		String formattedDate = converter.convert(date);
		// America/Sao_Paulo Timezone is UTC - 3 (without DS)
		String expected = "12/10/2010 07:10:10.000 BRT";
		assertThat(formattedDate, equalTo(expected));
	}

	@Configuration
	public static class InstantToStringConverterTestContextConfig {
		@Bean
		public ConverterUtils converterUtils() {
			return mock(ConverterUtils.class);
		}

	}

}