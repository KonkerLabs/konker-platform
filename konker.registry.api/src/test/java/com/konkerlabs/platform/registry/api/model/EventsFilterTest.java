package com.konkerlabs.platform.registry.api.model;

import static org.junit.Assert.*;

import org.junit.Test;

import com.konkerlabs.platform.registry.api.exceptions.BadRequestResponseException;

public class EventsFilterTest {

    @Test
    public void shouldSetChannel() throws BadRequestResponseException {
        EventsFilter filter = new EventsFilter();
        filter.parse("channel:temperature");

        assertEquals("temperature", filter.getChannel());
        assertNull(filter.getDeviceGuid());
        assertNull(filter.getStartingTimestamp());
        assertNull(filter.getEndTimestamp());
    }

    @Test
    public void shouldSetGuid() throws BadRequestResponseException {
        EventsFilter filter = new EventsFilter();
        filter.parse("device:f8af08b3-4da8-4858-af5f-0a59b0cc68b9");

        assertEquals("f8af08b3-4da8-4858-af5f-0a59b0cc68b9", filter.getDeviceGuid());
        assertNull(filter.getChannel());
        assertNull(filter.getStartingTimestamp());
        assertNull(filter.getEndTimestamp());
    }

    @Test
    public void shouldSetGuidAndChannel() throws BadRequestResponseException {
        EventsFilter filter = new EventsFilter();
        filter.parse("device:f8af08b3-4da8-4858-af5f-0a59b0cc68b9 channel:temperature");

        assertEquals("f8af08b3-4da8-4858-af5f-0a59b0cc68b9", filter.getDeviceGuid());
        assertEquals("temperature", filter.getChannel());
        assertNull(filter.getStartingTimestamp());
        assertNull(filter.getEndTimestamp());
    }

    @Test
    public void shouldSetInvalidField() {

        try {
            EventsFilter filter = new EventsFilter();
            filter.parse("channnnel:luminosity");

            fail();
        } catch (final BadRequestResponseException e) {
            final String msg = "Not supported filter: channnnel";
            assertEquals(msg, e.getMessage());
        }

    }

    @Test
    public void shouldSetInvalidQuery() {

        try {
            EventsFilter filter = new EventsFilter();
            filter.parse("channnnel=luminosity");

            fail();
        } catch (final BadRequestResponseException e) {
            final String msg = "Invalid filter: channnnel=luminosity";
            assertEquals(msg, e.getMessage());
        }

    }

    @Test
    public void shouldSetInvalidDate() {

        try {
            EventsFilter filter = new EventsFilter();
            filter.parse("timestamp:>2007/01/14");

            fail();
        } catch (final BadRequestResponseException e) {
            final String msg = "Not ISO Date: 2007/01/14";
            assertEquals(msg, e.getMessage());
        }

    }

    @Test
    public void shouldSetISOTimeDate() throws BadRequestResponseException {

        EventsFilter filter = new EventsFilter();
        filter.parse("timestamp:>2007-01-14T20:34:22-03:00");

        assertEquals(1168817662L, filter.getStartingTimestamp().getEpochSecond());
        assertNull(filter.getDeviceGuid());
        assertNull(filter.getChannel());
        assertNull(filter.getEndTimestamp());

        filter.parse("timestamp:<2007-01-14T20:34:22.4835Z");
        assertEquals(1168806862L, filter.getEndTimestamp().getEpochSecond());

        filter.parse("timestamp:<2007-01-14T20:34:22.4835");
        assertEquals(1168806862L, filter.getEndTimestamp().getEpochSecond());

    }

    @Test
    public void shouldSetISODate() throws BadRequestResponseException {

        EventsFilter filter = new EventsFilter();
        filter.parse("timestamp:>2007-01-14-03:00");

        assertEquals(1168743600L, filter.getStartingTimestamp().getEpochSecond());
        assertNull(filter.getDeviceGuid());
        assertNull(filter.getChannel());
        assertNull(filter.getEndTimestamp());

        filter.parse("timestamp:<2007-01-14Z");
        assertEquals(1168732800L, filter.getEndTimestamp().getEpochSecond());

        filter.parse("timestamp:<2007-01-14");
        assertEquals(1168732800L, filter.getEndTimestamp().getEpochSecond());

    }

    @Test
    public void shouldSetPositiveTimeZone() throws BadRequestResponseException {

        EventsFilter filter = new EventsFilter();
        filter.parse("timestamp:>2007-01-14T20:34:22+03:00");
        assertEquals(1168796062L, filter.getStartingTimestamp().getEpochSecond());

        filter.parse("timestamp:>2007-01-14T20:34:22+03:00 timestamp:<2007-01-15T20:34:22+03:00 channel:temp");
        assertEquals(1168796062L, filter.getStartingTimestamp().getEpochSecond());
        assertEquals(1168882462L, filter.getEndTimestamp().getEpochSecond());
        assertEquals("temp", filter.getChannel());

    }

}
