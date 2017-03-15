#! /usr/bin/env python2
import sys
import string
import random
import json

from cassandra.cluster import Cluster

from users.migrate_user_pwd import get_hashed_password
from users.migrate_user_roles import update_user_roles, update_to_version_0_1

db_version = "0.1"


def db_connect(host="localhost", port=9042, keyspace="registrykeyspace"):
    try:
        cluster = Cluster([host])
        session = cluster.connect(keyspace)
    except Exception as e:
        print(e)
        sys.exit(1)
    return session

def save_incoming_events(incomingEvents, host):
    db = db_connect(host=host)

    for event in incomingEvents:
        try:
            db.execute_async(
                """
                INSERT INTO incoming_events (device_guid, timestamp, channel, device_id, payload, tenant_domain)
                VALUES (%s, %s, %s, %s, %s, %s)
                """, (event['incoming']['deviceGuid'],
                      event['ts'],
                      event['incoming']['channel'],
                      event['incoming']['deviceId'],
                      event['payload'],
                      event['incoming']['tenantDomain'])
            )
            
        except Exception as e:
            print(e)
            sys.exit(1)
        
def save_outgoing_events(outgoingEvents, host):
    db = db_connect(host=host)
    
    for event in outgoingEvents:
        try:
            db.execute_async(
                """
                INSERT INTO outgoing_events (device_guid, timestamp, channel, device_id, payload, tenant_domain, incoming)
                VALUES (%s, %s, %s, %s, %s, %s, %s)
                """, (event['outgoing']['deviceGuid'], 
                      event['ts'], 
                      event['outgoing']['channel'],
                      event['outgoing']['deviceId'], 
                      event['payload'], 
                      event['outgoing']['tenantDomain'],
                      json.dumps(event['incoming'], ensure_ascii=False))
            )
            
        except Exception as e:
            print(e)
            sys.exit(1)
    
def find_incomingEvents_by_timestamp(timestamp, tenant, host):
    db = db_connect(host=host)
    try:
        if tenant is None:
            query = """
                SELECT device_guid, timestamp, channel, device_id, payload, tenant_domain FROM 
                    incoming_events WHERE timestamp >= {param1} ALLOW FILTERING
            """
            incomingEvents = db.execute(query.format(param1=str(timestamp)))
            
        else :
            query = """
                SELECT device_guid, timestamp, channel, device_id, payload, tenant_domain FROM 
                    incoming_events WHERE timestamp >= {param1} and tenant_domain = '{param2}' ALLOW FILTERING
            """
            incomingEvents = db.execute(query.format(param1=str(timestamp), param2=str(tenant)))
            
    except Exception as e:
        print(e)
        sys.exit(1)
    return incomingEvents

def find_outgoingEvents_by_timestamp(timestamp, tenant, host):
    db = db_connect(host=host)
    try:
        if tenant is None:
            query = """
                SELECT device_guid, timestamp, channel, device_id, payload, tenant_domain, incoming FROM 
                    outgoing_events WHERE timestamp >= {param1} ALLOW FILTERING
            """
            outgoingEvents = db.execute(query.format(param1=str(timestamp)))
            
        else :
            query = """
                SELECT device_guid, timestamp, channel, device_id, payload, tenant_domain, incoming FROM 
                    outgoing_events WHERE timestamp >= {param1} and tenant_domain = '{param2}' ALLOW FILTERING
            """
            outgoingEvents = db.execute(query.format(param1=str(timestamp), param2=str(tenant)))
             
    except Exception as e:
        print(e)
        sys.exit(1)
    return outgoingEvents
    
def create_incoming_events_table(host):
    db = db_connect(host=host)
    
    try:
        db.execute(
            """
            CREATE TABLE IF NOT EXISTS registrykeyspace.incoming_events (
                device_guid text,
                tenant_domain text,
                channel text,
                timestamp bigint,
                device_id text,
                payload text,
                PRIMARY KEY (device_guid, tenant_domain, channel, timestamp)
            ) WITH CLUSTERING ORDER BY ( tenant_domain ASC, channel ASC, timestamp DESC )
            """
        )
        
    except Exception as e:
        print(e)
        sys.exit(1)

def create_outgoing_events_table(host):
    db = db_connect(host=host)
    
    try:
        db.execute(
            """
            CREATE TABLE IF NOT EXISTS registrykeyspace.outgoing_events (
                device_guid text,
                tenant_domain text,
                channel text,
                timestamp bigint,
                device_id text,
                payload text,
                incoming text,
                PRIMARY KEY (device_guid, tenant_domain, channel, timestamp)
            ) WITH CLUSTERING ORDER BY ( tenant_domain ASC, channel ASC, timestamp DESC )
            """
        )
        
    except Exception as e:
        print(e)
        sys.exit(1)