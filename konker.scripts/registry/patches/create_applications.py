#! /usr/bin/python27
from pymongo import MongoClient

import pymongo
import calendar
import time
import bson.dbref

client = MongoClient("mongodb://localhost:27017")
db = client.registry

def create_applications():
    for tenant in db.tenants.find():
        print("Processing application for " + tenant['name'] + ". Domain name: " + tenant['domainName'])
        create_application(tenant['name'], tenant['domainName'], tenant['_id']);
        migrate_devices(tenant['_id'], tenant['domainName'])
        migrate_event_routes(tenant['_id'], tenant['domainName'])
        migrate_rest_destinations(tenant['_id'], tenant['domainName'])
        migrate_transformations(tenant['_id'], tenant['domainName'])
        migrate_incoming_events(tenant['domainName'], tenant['domainName'])
        migrate_outgoing_events(tenant['domainName'], tenant['domainName'])

def create_application(name, domainName, tenantId):
    if db.applications.find_one({"_id": domainName}):
        return

    db.applications.save({
        "_id": domainName,
        "friendlyName": name,
        "qualifier": "konker",
        "registrationDate": calendar.timegm(time.gmtime()) * 1000,
        "tenant": bson.dbref.DBRef('tenants', tenantId)
    })

def migrate_devices(tenantId, domainName):
    result = db.devices.update_many(
            {'tenant.$id': tenantId},
            {'$set' : {"application": bson.dbref.DBRef('applications', domainName)}}
        )
    print ("\tMigrated devices: " + str(result.modified_count))

def migrate_event_routes(tenantId, domainName):
    result = db.eventRoutes.update_many(
            {'tenant.$id': tenantId},
            {'$set' : {"application": bson.dbref.DBRef('applications', domainName)}}
        )
    print ("\tMigrated routes: " + str(result.modified_count))

def migrate_rest_destinations(tenantId, domainName):
    result = db.restDestinations.update_many(
            {'tenant.$id': tenantId},
            {'$set' : {"application": bson.dbref.DBRef('applications', domainName)}}
        )
    print ("\tMigrated rest destinations: " + str(result.modified_count))

def migrate_transformations(tenantId, domainName):
    result = db.transformations.update_many(
            {'tenant.$id': tenantId},
            {'$set' : {"application": bson.dbref.DBRef('applications', domainName)}}
        )
    print ("\tMigrated transformations: " + str(result.modified_count))

def migrate_incoming_events(tenantDomain, applicationName):
    result = db.incomingEvents.update_many(
            {'incoming.tenantDomain': tenantDomain},
            {'$set' : {"incoming.applicationName": applicationName}}
        )
    print ("\tMigrated incoming events: " + str(result.modified_count))

def migrate_outgoing_events(tenantDomain, applicationName):
    resultIn = db.outgoingEvents.update_many(
            {'incoming.tenantDomain': tenantDomain},
            {'$set' : {"incoming.applicationName": applicationName}}
        )
    resultOut = db.outgoingEvents.update_many(
            {'outgoing.tenantDomain': tenantDomain},
            {'$set' : {"outgoing.applicationName": applicationName}}
        )
    print ("\tMigrated outgoing events: " + str(resultIn.modified_count + resultOut.modified_count))

def main():
    create_applications()

if __name__ == "__main__":
    main()

