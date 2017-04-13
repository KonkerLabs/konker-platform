#! /usr/bin/env python2
import argparse
import sys
from datetime import datetime

from dao.registry import find_incomingEvents_by_timestamp, find_outgoingEvents_by_timestamp
from dao.registryCassandra import save_incoming_events, save_outgoing_events, create_incoming_events_table, create_outgoing_events_table
from bson.json_util import default
from __builtin__ import int


def main():
    print "Migration started, wait... "
    parser = argparse.ArgumentParser()
    parser.add_argument("-t", "--timestamp", default=3600000, type=int, help="Timestamp to filter events")
    parser.add_argument("-o", "--tenant", default=None, help="Tenant to filter events")
    parser.add_argument("-m", "--ipmongo", default="localhost", help="IP of mongoDB")
    parser.add_argument("-c", "--ipcassandra", default="localhost", help="IP of cassandra")
    args = parser.parse_args()
    
    start = datetime.now()
    
    incomingEvents = find_incomingEvents_by_timestamp(args.timestamp, args.tenant, args.ipmongo)
    outgoingEvents = find_outgoingEvents_by_timestamp(args.timestamp, args.tenant, args.ipmongo)
     
    create_incoming_events_table(args.ipcassandra);
    create_outgoing_events_table(args.ipcassandra);
     
    save_incoming_events(incomingEvents, args.ipcassandra)
    save_outgoing_events(outgoingEvents, args.ipcassandra)
    
    end = datetime.now()
    diff = end - start
    print "Migration finished. " + str(diff)

if __name__ == '__main__':
    main()
