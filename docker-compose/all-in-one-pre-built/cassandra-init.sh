#!/usr/bin/env bash

until printf "" 2>>/dev/null >>/dev/tcp/cassandra/9042; do 
    sleep 5;
    echo "Waiting for cassandra...";
done

echo "Creating keyspace and table..."
cqlsh cassandra -u cassandra -p cassandra -e "CREATE KEYSPACE IF NOT EXISTS registry WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'};"
cqlsh cassandra -u cassandra -p cassandra -e "CREATE TABLE IF NOT EXISTS registry.test (sensor_id uuid, registered_at timestamp, temperature int, PRIMARY KEY ((sensor_id), registered_at));"