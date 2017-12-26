#! /usr/bin/env python2
from pymongo import MongoClient
from bson.objectid import ObjectId
from bson.dbref import DBRef

import pymongo
import calendar
import time
import bson.dbref
import uuid
import argparse
from _cffi_backend import string

client = MongoClient("mongodb://localhost:27017")
db = client.registry
db.authenticate("", "")

def main():
    total = 0
    for deviceModel in db.devicesModel.find({
            "$and": [
                {"contentType" : { "$exists" : False} } ]}):
        deviceModel.update({'contentType': 'APPLICATION_MSGPACK' })
        db.deviceModelinations.update({"_id": deviceModel['_id']}, deviceModel)
        total = total+1
     
    print "Finish - Total devices model changed " + str(total)

if __name__ == '__main__':
    main()
