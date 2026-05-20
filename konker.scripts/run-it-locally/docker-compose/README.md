## HOW to use docker-compose

Before you can run, we need to create the databases

Plase start by running `docker-compose up-d mongodb`

Now we need to access the container and create the databases

```
docker exec -it mongodb bash
mongo -u root -p mongo --authenticationDatabase=admin

use registry;
db.createUser(
{
    user: "registry",
    pwd: "registry",
    roles: [
              { role: "dbOwner", db: "registry" }
           ]
});

use logs;
db.createUser(
{
    user: "registry",
    pwd: "registry",
    roles: [
              { role: "dbOwner", db: "logs" }
           ]
});

use billing;
db.createUser(
{
    user: "registry",
    pwd: "registry",
    roles: [
              { role: "dbOwner", db: "billing" }
           ]
});

use private-storage;
db.createUser(
{
    user: "registry",
    pwd: "registry",
    roles: [
              { role: "dbOwner", db: "private-storage" }
           ]
});

exit
exit
```
We also need to populate roles and create initial admin user
```
sudo docker-compose up -d init-database
```

Now we can start the event database
```
sudo docker-compose up -d cassandra
```
And create the keyspace
```
docker exec -it mongodb bash

cqlsh -u cassandra -p cassandra
CREATE KEYSPACE registry  WITH REPLICATION = {     'class' : 'SimpleStrategy', 'replication_factor': 1 } ;
CREATE TABLE incoming_events (
    tenant_domain text,
    application_name text,
    timestamp bigint,
    channel text,
    device_guid text,
    device_id text,
    payload text,
    PRIMARY KEY ((tenant_domain, application_name), timestamp)
) WITH CLUSTERING ORDER BY (timestamp DESC);

CREATE TABLE incoming_events_deleted (
    tenant_domain text,
    application_name text,
    timestamp bigint,
    channel text,
    device_guid text,
    device_id text,
    payload text,
    PRIMARY KEY ((tenant_domain, application_name, device_guid), timestamp)
) WITH CLUSTERING ORDER BY (timestamp DESC);

CREATE TABLE incoming_events_device_guid (
    tenant_domain text,
    application_name text,
    timestamp bigint,
    channel text,
    device_guid text,
    device_id text,
    payload text,
    PRIMARY KEY ((tenant_domain, application_name, device_guid), timestamp)
) WITH CLUSTERING ORDER BY (timestamp DESC);

CREATE TABLE incoming_events_device_guid_channel (
    tenant_domain text,
    application_name text,
    timestamp bigint,
    channel text,
    device_guid text,
    device_id text,
    payload text,
    PRIMARY KEY ((tenant_domain, application_name, device_guid, channel), timestamp)
) WITH CLUSTERING ORDER BY (timestamp DESC);

CREATE TABLE incoming_events_channel (
    tenant_domain text,
    application_name text,
    timestamp bigint,
    channel text,
    device_guid text,
    device_id text,
    payload text,
    PRIMARY KEY ((tenant_domain, application_name, channel), timestamp)
) WITH CLUSTERING ORDER BY (timestamp DESC);


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


ALTER TABLE incoming_events ADD geo_lat double;
ALTER TABLE incoming_events ADD	geo_lon double;
ALTER TABLE incoming_events ADD	geo_hdop bigint;
ALTER TABLE incoming_events ADD	geo_elev double;
ALTER TABLE incoming_events ADD ingested_timestamp bigint;

ALTER TABLE incoming_events_deleted ADD geo_lat double;
ALTER TABLE incoming_events_deleted ADD geo_lon double;
ALTER TABLE incoming_events_deleted ADD geo_hdop bigint;
ALTER TABLE incoming_events_deleted ADD geo_elev double;
ALTER TABLE incoming_events_deleted ADD ingested_timestamp bigint;

ALTER TABLE incoming_events_device_guid ADD geo_lat double;
ALTER TABLE incoming_events_device_guid ADD geo_lon double;
ALTER TABLE incoming_events_device_guid ADD geo_hdop bigint;
ALTER TABLE incoming_events_device_guid ADD geo_elev double;
ALTER TABLE incoming_events_device_guid ADD ingested_timestamp bigint;

ALTER TABLE incoming_events_device_guid_channel ADD geo_lat double;
ALTER TABLE incoming_events_device_guid_channel ADD geo_lon double;
ALTER TABLE incoming_events_device_guid_channel ADD geo_hdop bigint;
ALTER TABLE incoming_events_device_guid_channel ADD geo_elev double;
ALTER TABLE incoming_events_device_guid_channel ADD ingested_timestamp bigint;

ALTER TABLE incoming_events_channel ADD geo_lat double;
ALTER TABLE incoming_events_channel ADD geo_lon double;
ALTER TABLE incoming_events_channel ADD geo_hdop bigint;
ALTER TABLE incoming_events_channel ADD geo_elev double;
ALTER TABLE incoming_events_channel ADD ingested_timestamp bigint;


ALTER TABLE outgoing_events ADD geo_lat double;
ALTER TABLE outgoing_events ADD	geo_lon double;
ALTER TABLE outgoing_events ADD	geo_hdop bigint;
ALTER TABLE outgoing_events ADD	geo_elev double;
ALTER TABLE outgoing_events ADD ingested_timestamp bigint;

ALTER TABLE outgoing_events_deleted ADD geo_lat double;
ALTER TABLE outgoing_events_deleted ADD geo_lon double;
ALTER TABLE outgoing_events_deleted ADD geo_hdop bigint;
ALTER TABLE outgoing_events_deleted ADD geo_elev double;
ALTER TABLE outgoing_events_deleted ADD ingested_timestamp bigint;

ALTER TABLE outgoing_events_device_guid ADD geo_lat double;
ALTER TABLE outgoing_events_device_guid ADD geo_lon double;
ALTER TABLE outgoing_events_device_guid ADD geo_hdop bigint;
ALTER TABLE outgoing_events_device_guid ADD geo_elev double;
ALTER TABLE outgoing_events_device_guid ADD ingested_timestamp bigint;

ALTER TABLE outgoing_events_device_guid_channel ADD geo_lat double;
ALTER TABLE outgoing_events_device_guid_channel ADD geo_lon double;
ALTER TABLE outgoing_events_device_guid_channel ADD geo_hdop bigint;
ALTER TABLE outgoing_events_device_guid_channel ADD geo_elev double;
ALTER TABLE outgoing_events_device_guid_channel ADD ingested_timestamp bigint;

ALTER TABLE outgoing_events_channel ADD geo_lat double;
ALTER TABLE outgoing_events_channel ADD geo_lon double;
ALTER TABLE outgoing_events_channel ADD geo_hdop bigint;
ALTER TABLE outgoing_events_channel ADD geo_elev double;
ALTER TABLE outgoing_events_channel ADD ingested_timestamp bigint;


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

CREATE TABLE outgoing_events_location_guid (
    tenant_domain text,
    application_name text,
    timestamp bigint,
    channel text,
    device_guid text,
    device_id text,
    payload text,
    location_guid text,
    incoming_channel text,
    incoming_device_guid text,
    incoming_device_id text,
    geo_lat double,
    geo_lon double,
    geo_hdop bigint,
    geo_elev double,
    ingested_timestamp bigint,
    PRIMARY KEY ((tenant_domain, application_name, location_guid), timestamp)
) WITH CLUSTERING ORDER BY (timestamp DESC);

CREATE TABLE outgoing_events_location_guid_device_guid (
    tenant_domain text,
    application_name text,
    timestamp bigint,
    channel text,
    device_guid text,
    device_id text,
    payload text,
    location_guid text,
    incoming_channel text,
    incoming_device_guid text,
    incoming_device_id text,
    geo_lat double,
    geo_lon double,
    geo_hdop bigint,
    geo_elev double,
    ingested_timestamp bigint,
    PRIMARY KEY ((tenant_domain, application_name, location_guid, device_guid), timestamp)
) WITH CLUSTERING ORDER BY (timestamp DESC);

CREATE TABLE outgoing_events_location_guid_channel (
    tenant_domain text,
    application_name text,
    timestamp bigint,
    channel text,
    device_guid text,
    device_id text,
    payload text,
    location_guid text,
    incoming_channel text,
    incoming_device_guid text,
    incoming_device_id text,
    geo_lat double,
    geo_lon double,
    geo_hdop bigint,
    geo_elev double,
    ingested_timestamp bigint,
    PRIMARY KEY ((tenant_domain, application_name, location_guid, channel), timestamp)
) WITH CLUSTERING ORDER BY (timestamp DESC);

CREATE TABLE outgoing_events_location_guid_deviceguid_channel (
    tenant_domain text,
    application_name text,
    timestamp bigint,
    channel text,
    device_guid text,
    device_id text,
    payload text,
    location_guid text,
    incoming_channel text,
    incoming_device_guid text,
    incoming_device_id text,
    geo_lat double,
    geo_lon double,
    geo_hdop bigint,
    geo_elev double,
    ingested_timestamp bigint,
    PRIMARY KEY ((tenant_domain, application_name, location_guid, device_guid, channel), timestamp)
) WITH CLUSTERING ORDER BY (timestamp DESC);

ALTER TABLE outgoing_events ADD location_guid text;
ALTER TABLE outgoing_events_deleted ADD location_guid text;
ALTER TABLE outgoing_events_device_guid ADD location_guid text;
ALTER TABLE outgoing_events_device_guid_channel ADD location_guid text;
ALTER TABLE outgoing_events_channel ADD location_guid text;


exit
exit
```
Now go to konker.registry.services.storage.cassandra and run all the sql files usin cqlsh


Now we can start other containers
```
sudo docker-compose up -d rabbitmq redis mosquitto web api data data-processor bridge router
```





Services will be available at
- API http://localhost:8080
- API docs http://localhost:8080/v1/swagger-ui.html
- WEB http://localhost:8484/registry/login
- DATA http://localhost:8181

The initial password will be:
user = admin@localhost
pwd = changeme


## Optional
You can get rid of one database by using Mongodb as the eventstorage, for that:
- Comment out the block for cassandra database in the docker-compose.yml
- Change the configs for `EVENT_STORAGE_BEAN` from "cassandraEvents" to "mongoEvents" (default is mongoEvents if not informed) 