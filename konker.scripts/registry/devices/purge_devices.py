#!/usr/bin/python26
from pymongo import MongoClient
client = MongoClient("mongodb://<server>:<port>")
db = client.registry
device_count = db.devices.count()
print "Removing %d devices" % device_count
db.devices.drop()
