#! /usr/bin/env python2
from pymongo import MongoClient
from bson.objectid import ObjectId

import pymongo
import calendar
import time
import bson.dbref
import uuid
from bson.dbref import DBRef

client = MongoClient("mongodb://localhost:27017")
db = client.registry
# db.authenticate("", "")

def add_device_model():
    for device in db.devices.find():
        default = None
        
        cursor = db.devicesModel.find({
            "$and": [
                {"tenant" : { "$eq" : device['tenant']} },
                {"application" : { "$eq" : device['application']} },
                {"defaultModel" : { "$eq" : True}} ]})
        
        if cursor.count() <= 0:
            default = db.devicesModel.save({
                "guid": str(uuid.uuid4()),
                "name": "default",
                "description": "Default device model",
                "defaultModel": True,
                "tenant": device['tenant'],
                "application": device['application']
            })
        else:
            default = cursor.__getitem__(0)['_id']
        
        device.update({'deviceModel': DBRef('devicesModel', default) })
        db.devices.update({"_id": device['_id']}, device)
        
def add_location():
    for device in db.devices.find():
        default = None
        
        cursor = db.locations.find({
            "$and": [
                {"tenant" : { "$eq" : device['tenant']} },
                {"application" : { "$eq" : device['application']} },
                {"defaultLocation" : { "$eq" : True}} ]})
        
        if cursor.count() <= 0:
            default = db.locations.save({
                "guid": str(uuid.uuid4()),
                "name": "default",
                "defaultLocation": True,
                "tenant": device['tenant'],
                "application": device['application']
            })
        else:
            default = cursor.__getitem__(0)['_id']
        
        device.update({'location': DBRef('locations', default) })
        db.devices.update({"_id": device['_id']}, device)
            

def main():
    add_device_model()
    add_location()
    print "Finish"

if __name__ == '__main__':
    main()
