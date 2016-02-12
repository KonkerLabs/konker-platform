#!/usr/bin/python26
from pymongo import MongoClient
client = MongoClient("mongodb://<server>:<port>")
db = client.registry
eventRules_count = db.eventRules.count()
print "Removing %d Event Rules" % eventRules_count
db.eventRules.drop()
