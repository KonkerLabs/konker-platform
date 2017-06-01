CREATE TABLE outgoing_events (
    tenant_domain text,
    application_name text,
    timestamp bigint,
    channel text,
    device_guid text,
    device_id text,
    incoming_channel text,
    incoming_device_guid text,
    incoming_device_id text,
    payload text,
    PRIMARY KEY ((tenant_domain, application_name), timestamp)
) WITH CLUSTERING ORDER BY (timestamp DESC);

CREATE TABLE outgoing_events_deleted (
    tenant_domain text,
    application_name text,
    timestamp bigint,
    channel text,
    device_guid text,
    device_id text,
    incoming_channel text,
    incoming_device_guid text,
    incoming_device_id text,
    payload text,
    PRIMARY KEY ((tenant_domain, application_name, device_guid), timestamp)
) WITH CLUSTERING ORDER BY (timestamp DESC);

CREATE TABLE outgoing_events_device_guid (
    tenant_domain text,
    application_name text,
    timestamp bigint,
    channel text,
    device_guid text,
    device_id text,
    incoming_channel text,
    incoming_device_guid text,
    incoming_device_id text,
    payload text,
    PRIMARY KEY ((tenant_domain, application_name, device_guid), timestamp)
) WITH CLUSTERING ORDER BY (timestamp DESC);

CREATE TABLE outgoing_events_device_guid_channel (
    tenant_domain text,
    application_name text,
    timestamp bigint,
    channel text,
    device_guid text,
    device_id text,
    incoming_channel text,
    incoming_device_guid text,
    incoming_device_id text,
    payload text,
    PRIMARY KEY ((tenant_domain, application_name, device_guid, channel), timestamp)
) WITH CLUSTERING ORDER BY (timestamp DESC);

CREATE TABLE outgoing_events_channel (
    tenant_domain text,
    application_name text,
    timestamp bigint,
    channel text,
    device_guid text,
    device_id text,
    incoming_channel text,
    incoming_device_guid text,
    incoming_device_id text,
    payload text,
    PRIMARY KEY ((tenant_domain, application_name, channel), timestamp)
) WITH CLUSTERING ORDER BY (timestamp DESC);
