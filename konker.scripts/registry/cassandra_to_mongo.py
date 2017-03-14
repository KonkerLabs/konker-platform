#! /usr/bin/env python2
import argparse
import sys
import datetime

from dao.registry import save_incoming_events, save_outgoing_events
from dao.registryCassandra import find_incomingEvents_by_timestamp, find_outgoingEvents_by_timestamp
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
    
    incomingEvents = find_incomingEvents_by_timestamp(args.timestamp, args.tenant, args.ipcassandra)
    outgoingEvents = find_outgoingEvents_by_timestamp(args.timestamp, args.tenant, args.ipcassandra)
    
    save_incoming_events(incomingEvents, args.ipmongo)
    save_outgoing_events(outgoingEvents, args.ipmongo)

    print "Migration finished. "

if __name__ == '__main__':
    main()
