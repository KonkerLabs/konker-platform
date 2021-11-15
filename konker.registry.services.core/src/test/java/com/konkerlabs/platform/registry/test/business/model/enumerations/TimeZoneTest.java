package com.konkerlabs.platform.registry.test.business.model.enumerations;

import com.konkerlabs.platform.registry.business.model.enumerations.TimeZone;
import org.junit.Test;

import java.time.zone.ZoneRulesException;

import static org.hamcrest.MatcherAssert.assertThat;

public class TimeZoneTest {

	public void shouldAllTimeZonesBeValid() {

		boolean allValid = true;

		for (TimeZone tz : TimeZone.values()) {
			try {
				tz.getZoneId().getId().equals(tz.getId());
			} catch (ZoneRulesException e) {
				e.printStackTrace();
				allValid = false;
			}
		}

		assertThat(allValid, org.hamcrest.Matchers.is(true));

	}

}
