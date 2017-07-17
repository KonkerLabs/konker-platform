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
    for restDest in db.restDestinations.find({
            "$and": [
                {"type" : { "$exists" : False} } ]}):
        restDest.update({'type': 'FORWARD_MESSAGE' })
        db.restDestinations.update({"_id": restDest['_id']}, restDest)
        total = total+1
            
     
    print "Finish - Total rest destination changed " + str(total)

if __name__ == '__main__':
    main()
