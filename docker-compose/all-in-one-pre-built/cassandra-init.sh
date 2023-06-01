#!/usr/bin/env bash

until printf "" 2>>/dev/null >>/dev/tcp/cassandra/9042; do 
    sleep 5;
    echo "Waiting for cassandra...";
done

echo "Creating keyspace and table..."


cqlsh cassandra -u cassandra -p cassandra -e "
CREATE KEYSPACE IF NOT EXISTS registry WITH replication = {
  'class': 'SimpleStrategy',
  'replication_factor': '1'
};

USE registry;

CREATE TABLE  IF NOT EXISTS incoming_events (
  tenant_domain text,
  application_name text,
  "timestamp" bigint,
  channel text,
  device_guid text,
  device_id text,
  geo_elev double,
  geo_hdop bigint,
  geo_lat double,
  geo_lon double,
  ingested_timestamp bigint,
  location_guid text,
  payload text,
  PRIMARY KEY ((tenant_domain, application_name), "timestamp")
) WITH CLUSTERING ORDER BY ("timestamp" DESC) AND
  bloom_filter_fp_chance=0.010000 AND
  caching='KEYS_ONLY' AND
  comment='' AND
  dclocal_read_repair_chance=0.100000 AND
  gc_grace_seconds=864000 AND
  index_interval=128 AND
  read_repair_chance=0.000000 AND
  replicate_on_write='true' AND
  populate_io_cache_on_flush='false' AND
  default_time_to_live=0 AND
  speculative_retry='99.0PERCENTILE' AND
  memtable_flush_period_in_ms=0 AND
  compaction={'class': 'SizeTieredCompactionStrategy'} AND
  compression={'sstable_compression': 'LZ4Compressor'};

CREATE TABLE  IF NOT EXISTS incoming_events_channel (
  tenant_domain text,
  application_name text,
  channel text,
  "timestamp" bigint,
  device_guid text,
  device_id text,
  geo_elev double,
  geo_hdop bigint,
  geo_lat double,
  geo_lon double,
  ingested_timestamp bigint,
  location_guid text,
  payload text,
  PRIMARY KEY ((tenant_domain, application_name, channel), "timestamp")
) WITH CLUSTERING ORDER BY ("timestamp" DESC) AND
  bloom_filter_fp_chance=0.010000 AND
  caching='KEYS_ONLY' AND
  comment='' AND
  dclocal_read_repair_chance=0.100000 AND
  gc_grace_seconds=864000 AND
  index_interval=128 AND
  read_repair_chance=0.000000 AND
  replicate_on_write='true' AND
  populate_io_cache_on_flush='false' AND
  default_time_to_live=0 AND
  speculative_retry='99.0PERCENTILE' AND
  memtable_flush_period_in_ms=0 AND
  compaction={'class': 'SizeTieredCompactionStrategy'} AND
  compression={'sstable_compression': 'LZ4Compressor'};

CREATE TABLE  IF NOT EXISTS incoming_events_device_guid (
  tenant_domain text,
  application_name text,
  device_guid text,
  "timestamp" bigint,
  channel text,
  device_id text,
  geo_elev double,
  geo_hdop bigint,
  geo_lat double,
  geo_lon double,
  ingested_timestamp bigint,
  location_guid text,
  payload text,
  PRIMARY KEY ((tenant_domain, application_name, device_guid), "timestamp")
) WITH CLUSTERING ORDER BY ("timestamp" DESC) AND
  bloom_filter_fp_chance=0.010000 AND
  caching='KEYS_ONLY' AND
  comment='' AND
  dclocal_read_repair_chance=0.100000 AND
  gc_grace_seconds=864000 AND
  index_interval=128 AND
  read_repair_chance=0.000000 AND
  replicate_on_write='true' AND
  populate_io_cache_on_flush='false' AND
  default_time_to_live=0 AND
  speculative_retry='99.0PERCENTILE' AND
  memtable_flush_period_in_ms=0 AND
  compaction={'class': 'SizeTieredCompactionStrategy'} AND
  compression={'sstable_compression': 'LZ4Compressor'};

CREATE TABLE  IF NOT EXISTS incoming_events_device_guid_channel (
  tenant_domain text,
  application_name text,
  device_guid text,
  channel text,
  "timestamp" bigint,
  device_id text,
  geo_elev double,
  geo_hdop bigint,
  geo_lat double,
  geo_lon double,
  ingested_timestamp bigint,
  location_guid text,
  payload text,
  PRIMARY KEY ((tenant_domain, application_name, device_guid, channel), "timestamp")
) WITH CLUSTERING ORDER BY ("timestamp" DESC) AND
  bloom_filter_fp_chance=0.010000 AND
  caching='KEYS_ONLY' AND
  comment='' AND
  dclocal_read_repair_chance=0.100000 AND
  gc_grace_seconds=864000 AND
  index_interval=128 AND
  read_repair_chance=0.000000 AND
  replicate_on_write='true' AND
  populate_io_cache_on_flush='false' AND
  default_time_to_live=0 AND
  speculative_retry='99.0PERCENTILE' AND
  memtable_flush_period_in_ms=0 AND
  compaction={'class': 'SizeTieredCompactionStrategy'} AND
  compression={'sstable_compression': 'LZ4Compressor'};

CREATE TABLE  IF NOT EXISTS incoming_events_location_guid (
  tenant_domain text,
  application_name text,
  location_guid text,
  "timestamp" bigint,
  channel text,
  device_guid text,
  device_id text,
  geo_elev double,
  geo_hdop bigint,
  geo_lat double,
  geo_lon double,
  ingested_timestamp bigint,
  payload text,
  PRIMARY KEY ((tenant_domain, application_name, location_guid), "timestamp")
) WITH CLUSTERING ORDER BY ("timestamp" DESC) AND
  bloom_filter_fp_chance=0.010000 AND
  caching='KEYS_ONLY' AND
  comment='' AND
  dclocal_read_repair_chance=0.100000 AND
  gc_grace_seconds=864000 AND
  index_interval=128 AND
  read_repair_chance=0.000000 AND
  replicate_on_write='true' AND
  populate_io_cache_on_flush='false' AND
  default_time_to_live=0 AND
  speculative_retry='99.0PERCENTILE' AND
  memtable_flush_period_in_ms=0 AND
  compaction={'class': 'SizeTieredCompactionStrategy'} AND
  compression={'sstable_compression': 'LZ4Compressor'};

CREATE TABLE  IF NOT EXISTS incoming_events_location_guid_channel (
  tenant_domain text,
  application_name text,
  location_guid text,
  channel text,
  "timestamp" bigint,
  device_guid text,
  device_id text,
  geo_elev double,
  geo_hdop bigint,
  geo_lat double,
  geo_lon double,
  ingested_timestamp bigint,
  payload text,
  PRIMARY KEY ((tenant_domain, application_name, location_guid, channel), "timestamp")
) WITH CLUSTERING ORDER BY ("timestamp" DESC) AND
  bloom_filter_fp_chance=0.010000 AND
  caching='KEYS_ONLY' AND
  comment='' AND
  dclocal_read_repair_chance=0.100000 AND
  gc_grace_seconds=864000 AND
  index_interval=128 AND
  read_repair_chance=0.000000 AND
  replicate_on_write='true' AND
  populate_io_cache_on_flush='false' AND
  default_time_to_live=0 AND
  speculative_retry='99.0PERCENTILE' AND
  memtable_flush_period_in_ms=0 AND
  compaction={'class': 'SizeTieredCompactionStrategy'} AND
  compression={'sstable_compression': 'LZ4Compressor'};

CREATE TABLE  IF NOT EXISTS incoming_events_location_guid_device_guid (
  tenant_domain text,
  application_name text,
  location_guid text,
  device_guid text,
  "timestamp" bigint,
  channel text,
  device_id text,
  geo_elev double,
  geo_hdop bigint,
  geo_lat double,
  geo_lon double,
  ingested_timestamp bigint,
  payload text,
  PRIMARY KEY ((tenant_domain, application_name, location_guid, device_guid), "timestamp")
) WITH CLUSTERING ORDER BY ("timestamp" DESC) AND
  bloom_filter_fp_chance=0.010000 AND
  caching='KEYS_ONLY' AND
  comment='' AND
  dclocal_read_repair_chance=0.100000 AND
  gc_grace_seconds=864000 AND
  index_interval=128 AND
  read_repair_chance=0.000000 AND
  replicate_on_write='true' AND
  populate_io_cache_on_flush='false' AND
  default_time_to_live=0 AND
  speculative_retry='99.0PERCENTILE' AND
  memtable_flush_period_in_ms=0 AND
  compaction={'class': 'SizeTieredCompactionStrategy'} AND
  compression={'sstable_compression': 'LZ4Compressor'};

CREATE TABLE  IF NOT EXISTS incoming_events_location_guid_deviceguid_channel (
  tenant_domain text,
  application_name text,
  location_guid text,
  device_guid text,
  channel text,
  "timestamp" bigint,
  device_id text,
  geo_elev double,
  geo_hdop bigint,
  geo_lat double,
  geo_lon double,
  ingested_timestamp bigint,
  payload text,
  PRIMARY KEY ((tenant_domain, application_name, location_guid, device_guid, channel), "timestamp")
) WITH CLUSTERING ORDER BY ("timestamp" DESC) AND
  bloom_filter_fp_chance=0.010000 AND
  caching='KEYS_ONLY' AND
  comment='' AND
  dclocal_read_repair_chance=0.100000 AND
  gc_grace_seconds=864000 AND
  index_interval=128 AND
  read_repair_chance=0.000000 AND
  replicate_on_write='true' AND
  populate_io_cache_on_flush='false' AND
  default_time_to_live=0 AND
  speculative_retry='99.0PERCENTILE' AND
  memtable_flush_period_in_ms=0 AND
  compaction={'class': 'SizeTieredCompactionStrategy'} AND
  compression={'sstable_compression': 'LZ4Compressor'};

CREATE TABLE  IF NOT EXISTS outgoing_events (
  tenant_domain text,
  application_name text,
  "timestamp" bigint,
  channel text,
  device_guid text,
  device_id text,
  geo_elev double,
  geo_hdop bigint,
  geo_lat double,
  geo_lon double,
  incoming_channel text,
  incoming_device_guid text,
  incoming_device_id text,
  ingested_timestamp bigint,
  location_guid text,
  payload text,
  PRIMARY KEY ((tenant_domain, application_name), "timestamp")
) WITH CLUSTERING ORDER BY ("timestamp" DESC) AND
  bloom_filter_fp_chance=0.010000 AND
  caching='KEYS_ONLY' AND
  comment='' AND
  dclocal_read_repair_chance=0.100000 AND
  gc_grace_seconds=864000 AND
  index_interval=128 AND
  read_repair_chance=0.000000 AND
  replicate_on_write='true' AND
  populate_io_cache_on_flush='false' AND
  default_time_to_live=0 AND
  speculative_retry='99.0PERCENTILE' AND
  memtable_flush_period_in_ms=0 AND
  compaction={'class': 'SizeTieredCompactionStrategy'} AND
  compression={'sstable_compression': 'LZ4Compressor'};

CREATE TABLE  IF NOT EXISTS outgoing_events_channel (
  tenant_domain text,
  application_name text,
  channel text,
  "timestamp" bigint,
  device_guid text,
  device_id text,
  geo_elev double,
  geo_hdop bigint,
  geo_lat double,
  geo_lon double,
  incoming_channel text,
  incoming_device_guid text,
  incoming_device_id text,
  ingested_timestamp bigint,
  location_guid text,
  payload text,
  PRIMARY KEY ((tenant_domain, application_name, channel), "timestamp")
) WITH CLUSTERING ORDER BY ("timestamp" DESC) AND
  bloom_filter_fp_chance=0.010000 AND
  caching='KEYS_ONLY' AND
  comment='' AND
  dclocal_read_repair_chance=0.100000 AND
  gc_grace_seconds=864000 AND
  index_interval=128 AND
  read_repair_chance=0.000000 AND
  replicate_on_write='true' AND
  populate_io_cache_on_flush='false' AND
  default_time_to_live=0 AND
  speculative_retry='99.0PERCENTILE' AND
  memtable_flush_period_in_ms=0 AND
  compaction={'class': 'SizeTieredCompactionStrategy'} AND
  compression={'sstable_compression': 'LZ4Compressor'};

CREATE TABLE  IF NOT EXISTS outgoing_events_deleted (
  tenant_domain text,
  application_name text,
  device_guid text,
  "timestamp" bigint,
  channel text,
  device_id text,
  geo_elev double,
  geo_hdop bigint,
  geo_lat double,
  geo_lon double,
  incoming_channel text,
  incoming_device_guid text,
  incoming_device_id text,
  ingested_timestamp bigint,
  location_guid text,
  payload text,
  PRIMARY KEY ((tenant_domain, application_name, device_guid), "timestamp")
) WITH CLUSTERING ORDER BY ("timestamp" DESC) AND
  bloom_filter_fp_chance=0.010000 AND
  caching='KEYS_ONLY' AND
  comment='' AND
  dclocal_read_repair_chance=0.100000 AND
  gc_grace_seconds=864000 AND
  index_interval=128 AND
  read_repair_chance=0.000000 AND
  replicate_on_write='true' AND
  populate_io_cache_on_flush='false' AND
  default_time_to_live=0 AND
  speculative_retry='99.0PERCENTILE' AND
  memtable_flush_period_in_ms=0 AND
  compaction={'class': 'SizeTieredCompactionStrategy'} AND
  compression={'sstable_compression': 'LZ4Compressor'};

CREATE TABLE  IF NOT EXISTS outgoing_events_device_guid (
  tenant_domain text,
  application_name text,
  device_guid text,
  "timestamp" bigint,
  channel text,
  device_id text,
  geo_elev double,
  geo_hdop bigint,
  geo_lat double,
  geo_lon double,
  incoming_channel text,
  incoming_device_guid text,
  incoming_device_id text,
  ingested_timestamp bigint,
  location_guid text,
  payload text,
  PRIMARY KEY ((tenant_domain, application_name, device_guid), "timestamp")
) WITH CLUSTERING ORDER BY ("timestamp" DESC) AND
  bloom_filter_fp_chance=0.010000 AND
  caching='KEYS_ONLY' AND
  comment='' AND
  dclocal_read_repair_chance=0.100000 AND
  gc_grace_seconds=864000 AND
  index_interval=128 AND
  read_repair_chance=0.000000 AND
  replicate_on_write='true' AND
  populate_io_cache_on_flush='false' AND
  default_time_to_live=0 AND
  speculative_retry='99.0PERCENTILE' AND
  memtable_flush_period_in_ms=0 AND
  compaction={'class': 'SizeTieredCompactionStrategy'} AND
  compression={'sstable_compression': 'LZ4Compressor'};

CREATE TABLE  IF NOT EXISTS outgoing_events_device_guid_channel (
  tenant_domain text,
  application_name text,
  device_guid text,
  channel text,
  "timestamp" bigint,
  device_id text,
  geo_elev double,
  geo_hdop bigint,
  geo_lat double,
  geo_lon double,
  incoming_channel text,
  incoming_device_guid text,
  incoming_device_id text,
  ingested_timestamp bigint,
  location_guid text,
  payload text,
  PRIMARY KEY ((tenant_domain, application_name, device_guid, channel), "timestamp")
) WITH CLUSTERING ORDER BY ("timestamp" DESC) AND
  bloom_filter_fp_chance=0.010000 AND
  caching='KEYS_ONLY' AND
  comment='' AND
  dclocal_read_repair_chance=0.100000 AND
  gc_grace_seconds=864000 AND
  index_interval=128 AND
  read_repair_chance=0.000000 AND
  replicate_on_write='true' AND
  populate_io_cache_on_flush='false' AND
  default_time_to_live=0 AND
  speculative_retry='99.0PERCENTILE' AND
  memtable_flush_period_in_ms=0 AND
  compaction={'class': 'SizeTieredCompactionStrategy'} AND
  compression={'sstable_compression': 'LZ4Compressor'};

CREATE TABLE  IF NOT EXISTS outgoing_events_location_guid (
  tenant_domain text,
  application_name text,
  location_guid text,
  "timestamp" bigint,
  channel text,
  device_guid text,
  device_id text,
  geo_elev double,
  geo_hdop bigint,
  geo_lat double,
  geo_lon double,
  incoming_channel text,
  incoming_device_guid text,
  incoming_device_id text,
  ingested_timestamp bigint,
  payload text,
  PRIMARY KEY ((tenant_domain, application_name, location_guid), "timestamp")
) WITH CLUSTERING ORDER BY ("timestamp" DESC) AND
  bloom_filter_fp_chance=0.010000 AND
  caching='KEYS_ONLY' AND
  comment='' AND
  dclocal_read_repair_chance=0.100000 AND
  gc_grace_seconds=864000 AND
  index_interval=128 AND
  read_repair_chance=0.000000 AND
  replicate_on_write='true' AND
  populate_io_cache_on_flush='false' AND
  default_time_to_live=0 AND
  speculative_retry='99.0PERCENTILE' AND
  memtable_flush_period_in_ms=0 AND
  compaction={'class': 'SizeTieredCompactionStrategy'} AND
  compression={'sstable_compression': 'LZ4Compressor'};

CREATE TABLE  IF NOT EXISTS outgoing_events_location_guid_channel (
  tenant_domain text,
  application_name text,
  location_guid text,
  channel text,
  "timestamp" bigint,
  device_guid text,
  device_id text,
  geo_elev double,
  geo_hdop bigint,
  geo_lat double,
  geo_lon double,
  incoming_channel text,
  incoming_device_guid text,
  incoming_device_id text,
  ingested_timestamp bigint,
  payload text,
  PRIMARY KEY ((tenant_domain, application_name, location_guid, channel), "timestamp")
) WITH CLUSTERING ORDER BY ("timestamp" DESC) AND
  bloom_filter_fp_chance=0.010000 AND
  caching='KEYS_ONLY' AND
  comment='' AND
  dclocal_read_repair_chance=0.100000 AND
  gc_grace_seconds=864000 AND
  index_interval=128 AND
  read_repair_chance=0.000000 AND
  replicate_on_write='true' AND
  populate_io_cache_on_flush='false' AND
  default_time_to_live=0 AND
  speculative_retry='99.0PERCENTILE' AND
  memtable_flush_period_in_ms=0 AND
  compaction={'class': 'SizeTieredCompactionStrategy'} AND
  compression={'sstable_compression': 'LZ4Compressor'};

CREATE TABLE  IF NOT EXISTS outgoing_events_location_guid_device_guid (
  tenant_domain text,
  application_name text,
  location_guid text,
  device_guid text,
  "timestamp" bigint,
  channel text,
  device_id text,
  geo_elev double,
  geo_hdop bigint,
  geo_lat double,
  geo_lon double,
  incoming_channel text,
  incoming_device_guid text,
  incoming_device_id text,
  ingested_timestamp bigint,
  payload text,
  PRIMARY KEY ((tenant_domain, application_name, location_guid, device_guid), "timestamp")
) WITH CLUSTERING ORDER BY ("timestamp" DESC) AND
  bloom_filter_fp_chance=0.010000 AND
  caching='KEYS_ONLY' AND
  comment='' AND
  dclocal_read_repair_chance=0.100000 AND
  gc_grace_seconds=864000 AND
  index_interval=128 AND
  read_repair_chance=0.000000 AND
  replicate_on_write='true' AND
  populate_io_cache_on_flush='false' AND
  default_time_to_live=0 AND
  speculative_retry='99.0PERCENTILE' AND
  memtable_flush_period_in_ms=0 AND
  compaction={'class': 'SizeTieredCompactionStrategy'} AND
  compression={'sstable_compression': 'LZ4Compressor'};

CREATE TABLE  IF NOT EXISTS outgoing_events_location_guid_deviceguid_channel (
  tenant_domain text,
  application_name text,
  location_guid text,
  device_guid text,
  channel text,
  "timestamp" bigint,
  device_id text,
  geo_elev double,
  geo_hdop bigint,
  geo_lat double,
  geo_lon double,
  incoming_channel text,
  incoming_device_guid text,
  incoming_device_id text,
  ingested_timestamp bigint,
  payload text,
  PRIMARY KEY ((tenant_domain, application_name, location_guid, device_guid, channel), "timestamp")
) WITH CLUSTERING ORDER BY ("timestamp" DESC) AND
  bloom_filter_fp_chance=0.010000 AND
  caching='KEYS_ONLY' AND
  comment='' AND
  dclocal_read_repair_chance=0.100000 AND
  gc_grace_seconds=864000 AND
  index_interval=128 AND
  read_repair_chance=0.000000 AND
  replicate_on_write='true' AND
  populate_io_cache_on_flush='false' AND
  default_time_to_live=0 AND
  speculative_retry='99.0PERCENTILE' AND
  memtable_flush_period_in_ms=0 AND
  compaction={'class': 'SizeTieredCompactionStrategy'} AND
  compression={'sstable_compression': 'LZ4Compressor'};"
