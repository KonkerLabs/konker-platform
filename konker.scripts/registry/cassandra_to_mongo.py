#! /usr/bin/env python2
import argparse
import sys
# from dao.registry import find_incomingEvents_by_timestamp, find_outgoingEvents_by_timestamp
from dao.registryCassandra import find_incomingEvents_by_timestamp, find_outgoingEvents_by_timestamp
from bson.json_util import default
from __builtin__ import int


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("-t", "--timestamp", default=3600000, type=int, help="Timestamp to filter events")
    parser.add_argument("-o", "--tenant", default=None, help="Tenant to filter events")
    parser.add_argument("-m", "--ipmongo", default="localhost", help="IP of mongoDB")
    parser.add_argument("-c", "--ipcassandra", default="localhost", help="IP of cassandra")
    args = parser.parse_args()
    
    incomingEvents = find_incomingEvents_by_timestamp(args.timestamp, args.tenant, args.ipmongo)
#     outgoingEvents = find_outgoingEvents_by_timestamp(args.timestamp, args.tenant, args.ipmongo)
    
#     create_incoming_events_table(args.ipcassandra);
#     create_outgoing_events_table(args.ipcassandra);
#     
    for incoming in incomingEvents:
        print incoming
#         save_incoming_events(incoming, args.ipcassandra)
#         
#     for outgoing in outgoingEvents:
#         print outgoing
#         save_outgoing_events(outgoing, args.ipcassandra)


if __name__ == '__main__':
    main()
