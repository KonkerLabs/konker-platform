#! /usr/bin/env python2
import sys
import string
import random

from cassandra.cluster import Cluster

from users.migrate_user_pwd import get_hashed_password
from users.migrate_user_roles import update_user_roles, update_to_version_0_1

db_version = "0.1"


def db_connect(host="localhost", port=9042, keyspace="registrykeyspace"):
    try:
        cluster = Cluster()
        session = cluster.connect(keyspace)
    except Exception as e:
        print(e)
        sys.exit(1)
    return session

def save_incoming_events(event):
    db = db_connect()
    
    try:
        db.execute(
            """
            INSERT INTO incoming_events (device_guid, timestamp, channel, device_id, payload, tenant_domain)
            VALUES (%s, %s, %s, %s, %s, %s)
            """,
            (event['incoming']['deviceGuid'], event['ts'], event['incoming']['channel'],
             event['incoming']['deviceId'], event['payload'], event['incoming']['tenantDomain'])
        )
        
    except Exception as e:
        print(e)
        sys.exit(1)
        
def save_outgoing_events(event):
    db = db_connect()
    
    try:
        db.execute(
            """
            INSERT INTO outgoing_events (device_guid, timestamp, channel, device_id, payload, tenant_domain,
                        incoming_device_guid, incoming_tenant_domain, incoming_channel, incoming_device_id)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
            """,
            (event['outgoing']['deviceGuid'], event['ts'], event['outgoing']['channel'],
             event['outgoing']['deviceId'], event['payload'], event['outgoing']['tenantDomain'],
             event['incoming']['deviceGuid'], event['incoming']['tenantDomain'], 
             event['incoming']['channel'], event['incoming']['deviceId'])
        )
        
    except Exception as e:
        print(e)
        sys.exit(1)
    
def create_incoming_events_table():
    db = db_connect()
    
    try:
        db.execute(
            """
            CREATE TABLE IF NOT EXISTS registrykeyspace.incoming_events (
                device_guid text,
                timestamp bigint,
                channel text,
                device_id text,
                payload text,
                tenant_domain text,
                PRIMARY KEY (device_guid, timestamp)
            ) WITH CLUSTERING ORDER BY ( timestamp DESC )
            """
        )
        
    except Exception as e:
        print(e)
        sys.exit(1)

def create_outgoing_events_table():
    db = db_connect()
    
    try:
        db.execute(
            """
            CREATE TABLE IF NOT EXISTS registrykeyspace.outgoing_events (
                device_guid text,
                timestamp bigint,
                channel text,
                device_id text,
                payload text,
                tenant_domain text,
                incoming_device_guid text,
                incoming_tenant_domain text,
                incoming_channel text,
                incoming_device_id text,
                PRIMARY KEY (device_guid, timestamp)
            ) WITH CLUSTERING ORDER BY ( timestamp DESC )
            """
        )
        
    except Exception as e:
        print(e)
        sys.exit(1)