CREATE TABLE incoming_events_location_guid (
    tenant_domain text,
    application_name text,
    timestamp bigint,
    channel text,
    device_guid text,
    device_id text,
    payload text,
    location_guid text,
    geo_lat double,
    geo_lon double,
    geo_hdop bigint,
    geo_elev double,
    ingested_timestamp bigint,
    PRIMARY KEY ((tenant_domain, application_name, location_guid), timestamp)
) WITH CLUSTERING ORDER BY (timestamp DESC);

CREATE TABLE incoming_events_location_guid_device_guid (
    tenant_domain text,
    application_name text,
    timestamp bigint,
    channel text,
    device_guid text,
    device_id text,
    payload text,
    location_guid text,
    geo_lat double,
    geo_lon double,
    geo_hdop bigint,
    geo_elev double,
    ingested_timestamp bigint,
    PRIMARY KEY ((tenant_domain, application_name, location_guid, device_guid), timestamp)
) WITH CLUSTERING ORDER BY (timestamp DESC);

CREATE TABLE incoming_events_location_guid_channel (
    tenant_domain text,
    application_name text,
    timestamp bigint,
    channel text,
    device_guid text,
    device_id text,
    payload text,
    location_guid text,
    geo_lat double,
    geo_lon double,
    geo_hdop bigint,
    geo_elev double,
    ingested_timestamp bigint,
    PRIMARY KEY ((tenant_domain, application_name, location_guid, channel), timestamp)
) WITH CLUSTERING ORDER BY (timestamp DESC);

CREATE TABLE incoming_events_location_guid_deviceguid_channel (
    tenant_domain text,
    application_name text,
    timestamp bigint,
    channel text,
    device_guid text,
    device_id text,
    payload text,
    location_guid text,
    geo_lat double,
    geo_lon double,
    geo_hdop bigint,
    geo_elev double,
    ingested_timestamp bigint,
    PRIMARY KEY ((tenant_domain, application_name, location_guid, device_guid, channel), timestamp)
) WITH CLUSTERING ORDER BY (timestamp DESC);

ALTER TABLE incoming_events ADD location_guid text;
ALTER TABLE incoming_events_deleted ADD location_guid text;
ALTER TABLE incoming_events_device_guid ADD location_guid text;
ALTER TABLE incoming_events_device_guid_channel ADD location_guid text;
ALTER TABLE incoming_events_channel ADD location_guid text;