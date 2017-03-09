#! /usr/bin/env python2
import argparse
import sys
from dao.registry import find_incomingEvents_by_timestamp, find_outgoingEvents_by_timestamp
from dao.registryCassandra import save_incoming_events, save_outgoing_events, create_incoming_events_table, create_outgoing_events_table


def main(timestamp, tenant):
    incomingEvents = find_incomingEvents_by_timestamp(timestamp, tenant)
    outgoingEvents = find_outgoingEvents_by_timestamp(timestamp, tenant)
    
    create_incoming_events_table();
    create_outgoing_events_table();
    
    for incoming in incomingEvents:
        print incoming
        save_incoming_events(incoming)
        
    for outgoing in outgoingEvents:
        print outgoing
        save_outgoing_events(outgoing)


if __name__ == '__main__':
    main(long(sys.argv[1]), None if len(sys.argv) < 3 else sys.argv[2])
