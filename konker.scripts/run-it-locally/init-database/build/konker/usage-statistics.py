#!/usr/bin/python
import os
import time
import requests
import json
import uuid
import sys

from pymongo import MongoClient

def db_connect(host='localhost', port=27017):
    client = MongoClient("mongodb://%s:%d" % (host, port))
    return client.registry

def get_instalation_id():
    db = db_connect()
    if "dockerPamameters" not in db.collection_names():
        db.create_collection("dockerPamameters")
        db.dockerPamameters.insert(
            {
                "instalationId": str(uuid.uuid4())
            }
        )

    document = db.dockerPamameters.find_one({"instalationId": { "$exists": True }})
    if document is None:
        db.dockerPamameters.insert(
            {
                "instalationId": str(uuid.uuid4())
            }
        )

    document = db.dockerPamameters.find_one({"instalationId": { "$exists": True }})

    return document['instalationId']

def get_total_devices():
    db = db_connect()
    count = db.devices.count()

    return count

def get_last_device():
    db = db_connect()
    last = db.devices.find_one({}, sort=[('$natural', -1)])

    if last is None:
        return 0;

    return last["registrationDate"]

def get_total_incoming_messages():
    db = db_connect()
    count = db.incomingEvents.count()

    return count

def get_last_incoming_message():
    db = db_connect()
    last = db.incomingEvents.find_one({}, sort=[('$natural', -1)])

    if last is None:
        return 0;

    return last["ts"]

def send_statistics(devicesCount, lastRegisteredEvent, incomingEventsCount, lastIncomingReceivedEvent):
    url = 'https://umc.konkerlabs.net/stats'
    data = {"timestamp" : int(time.time()),
            "instalationId" : instalationId,
            "devicesCount" : devicesCount,
            "lastRegisteredEvent": lastRegisteredEvent,
            "incomingEventsCount": incomingEventsCount,
            "lastIncomingReceivedEvent": lastIncomingReceivedEvent}
    data_json = json.dumps(data)
    headers = {'Content-type': 'application/json'}

    response = requests.post(url, data=data_json, headers=headers)
    return response

###########################################################################

usageStatsParam = ''

if os.environ.get('USAGE_STATS') is not None:
    usageStatsParam = os.environ.get('USAGE_STATS')

if usageStatsParam.lower() == 'disabled' or usageStatsParam.lower() == 'false':
    sys.stdout.write("usage statistics disabled...\n")
    sys.stdout.flush()
    exit(0);

sys.stdout.write("usage statistics enabled...\n")
sys.stdout.flush()

try:
    # Get instalation id
    instalationId = get_instalation_id()

    # Get current usage
    devicesCount = get_total_devices()
    lastRegisteredEvent = get_last_device()
    incomingEventsCount = get_total_incoming_messages()
    lastIncomingReceivedEvent = get_last_incoming_message()

    send_statistics(devicesCount, lastRegisteredEvent, incomingEventsCount, lastIncomingReceivedEvent)

except Exception as e:
    print e

# Main Loop
while True:
    try:
        newDevicesCount = get_total_devices()
        newLastRegisteredEvent = get_last_device()
        newIncomingEventsCount = get_total_incoming_messages()
        newIncomingLastReceivedEvent = get_last_incoming_message()

        changed = False

        # Check changes
        if devicesCount != newDevicesCount: changed = True
        if lastRegisteredEvent != newLastRegisteredEvent: changed = True
        if incomingEventsCount != newIncomingEventsCount: changed = True
        if lastIncomingReceivedEvent != newIncomingLastReceivedEvent: changed = True

        if changed:
            send_statistics(newDevicesCount, newLastRegisteredEvent, newIncomingEventsCount, newIncomingLastReceivedEvent)

        devicesCount = newDevicesCount
        lastRegisteredEvent = newLastRegisteredEvent
        incomingEventsCount = newIncomingEventsCount
        lastIncomingReceivedEvent = newIncomingLastReceivedEvent

    except Exception as e:
        print e

    # Sleep for 15 minutes
    time.sleep(15 * 60)
