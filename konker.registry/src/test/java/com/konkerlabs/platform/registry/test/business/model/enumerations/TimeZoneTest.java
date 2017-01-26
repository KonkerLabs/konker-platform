package com.konkerlabs.platform.registry.test.business.model.enumerations;

import org.junit.Test;

import com.konkerlabs.platform.registry.business.model.enumerations.TimeZone;

public class TimeZoneTest {

	@Test
	public void shouldAllTimeZonesBeValid() {

		for (TimeZone tz : TimeZone.values()) {
			tz.getZoneId().getId().equals(tz.getId());
		}

	}

}
