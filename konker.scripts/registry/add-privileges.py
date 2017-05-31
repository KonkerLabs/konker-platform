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
# db.authenticate("", "")


def main():
          
    parser = argparse.ArgumentParser()
    parser.add_argument('-p', type=str, dest='privileges', help='Add privileges common to all roles')
    parser.add_argument('-sp', type=str, dest='superPrivileges', help='Add privileges just to super user role')
     
    results = parser.parse_args()
    if results.privileges is not None:
        for priv in results.privileges.split(','):
            save_privilege(priv)
            update_role('ROLE_IOT_USER', priv)
            update_role('ROLE_SUPER_USER', priv)
             
            if priv.startswith('LIST') or priv.startswith('SHOW') or priv.startswith('VIEW') :
                update_role('ROLE_IOT_READ_ONLY', priv) 
     
    if results.superPrivileges is not None:     
        for superPriv in results.superPrivileges.split(','):
            save_privilege(superPriv)
            update_role('ROLE_SUPER_USER', superPriv)
        
    
     
    print "Finish"
    
def update_role(roleName, privilege):
    privi = db.privileges.find({
            "$and": [
                {"name" : { "$eq" : privilege} } ]})
    
    cursor = db.roles.find({
            "$and": [
                {"name" : { "$eq" : roleName} } ]})
    
    role = cursor.__getitem__(0)
    refPrivilege = DBRef('privileges', privi.__getitem__(0)['_id'])
    
    if refPrivilege not in role['privileges']:
        role['privileges'].insert(len(role['privileges']), refPrivilege)
        db.roles.update({"_id": role['_id']}, role)
    
    
def save_privilege(privilege):
    privi = db.privileges.find({
            "$and": [
                {"name" : { "$eq" : privilege} } ]})
    
    if privi.count() <= 0:
        db.privileges.save({'name' : privilege})

if __name__ == '__main__':
    main()
