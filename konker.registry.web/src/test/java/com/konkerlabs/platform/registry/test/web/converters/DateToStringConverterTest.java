package com.konkerlabs.platform.registry.test.web.converters;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.model.enumerations.DateFormat;
import com.konkerlabs.platform.registry.business.model.enumerations.TimeZone;
import com.konkerlabs.platform.registry.config.EmailConfig;
import com.konkerlabs.platform.registry.config.WebConfig;
import com.konkerlabs.platform.registry.config.WebMvcConfig;
import com.konkerlabs.platform.registry.security.UserContextResolver;
import com.konkerlabs.platform.registry.test.base.WebTestConfiguration;
import com.konkerlabs.platform.registry.web.converters.DateToStringConverter;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = { WebMvcConfig.class, WebTestConfiguration.class, WebConfig.class, EmailConfig.class })
public class DateToStringConverterTest {

	@Autowired
	private UserContextResolver userContextResolver;

	@Autowired
	private DateToStringConverter converter;

	Date date;

	@Before
	public void setUp() {
		date = new Date(1484235702700L);
	}

	@After
	public void tearDown() {
		Mockito.reset(userContextResolver);
	}

	@Test
	public void shouldConvertDateToDDMMYYYY_BR() throws Exception {

		User mockUser = User.builder().dateFormat(DateFormat.DDMMYYYY).zoneId(TimeZone.AMERICA_SAO_PAULO).build();

		when(userContextResolver.getObject()).thenReturn(mockUser);

		String formattedDate = converter.convert(date);
		assertThat(formattedDate, equalTo("12/01/2017 13:41:42"));

	}

	@Test
	public void shouldConvertDateToDDMMYYYY_LA() throws Exception {

		User mockUser = User.builder().dateFormat(DateFormat.DDMMYYYY).zoneId(TimeZone.AMERICA_LOS_ANGELES).build();

		when(userContextResolver.getObject()).thenReturn(mockUser);

		String formattedDate = converter.convert(date);
		assertThat(formattedDate, equalTo("12/01/2017 07:41:42"));

	}

	@Test
	public void shouldConvertDateToYYYYMMDD_BR() throws Exception {

		User mockUser = User.builder().dateFormat(DateFormat.YYYYMMDD).zoneId(TimeZone.AMERICA_SAO_PAULO).build();

		when(userContextResolver.getObject()).thenReturn(mockUser);

		String formattedDate = converter.convert(date);
		assertThat(formattedDate, equalTo("2017/01/12 13:41:42"));

	}

	@Test
	public void shouldConvertDateToMMDDYYYY_LA() throws Exception {

		User mockUser = User.builder().dateFormat(DateFormat.MMDDYYYY).zoneId(TimeZone.AMERICA_LOS_ANGELES).build();

		when(userContextResolver.getObject()).thenReturn(mockUser);

		String formattedDate = converter.convert(date);
		assertThat(formattedDate, equalTo("01/12/2017 07:41:42"));

	}

	@Configuration
	public static class InstantToStringConverterTestContextConfig {
		@Bean
		public UserContextResolver converterUtils() {
			return mock(UserContextResolver.class);
		}
	}

}