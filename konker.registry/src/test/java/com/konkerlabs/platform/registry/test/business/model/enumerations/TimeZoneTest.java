package com.konkerlabs.platform.registry.test.business.model.enumerations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import java.time.zone.ZoneRulesException;

import org.junit.Test;

import com.konkerlabs.platform.registry.business.model.enumerations.TimeZone;

public class TimeZoneTest {

	@Test
	public void shouldAllTimeZonesBeValid() {

		boolean allValid = true;

		for (TimeZone tz : TimeZone.values()) {
			try {
				tz.getZoneId().getId().equals(tz.getId());
			} catch (ZoneRulesException e) {
				allValid = false;
			}
		}

		assertThat(allValid, org.hamcrest.Matchers.is(true));

	}

}
