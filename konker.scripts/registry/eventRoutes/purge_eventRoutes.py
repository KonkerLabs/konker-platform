#!/usr/bin/python26
from pymongo import MongoClient
client = MongoClient("mongodb://<server>:<port>")
db = client.registry
eventRoutes_count = db.eventRoutes.count()
print "Removing %d Event Routes" % eventRoutes_count
db.eventRoutes.drop()
