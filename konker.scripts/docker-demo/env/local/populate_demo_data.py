#! /usr/bin/python26
from pymongo import MongoClient
from bson.dbref import DBRef

changed=False
client = MongoClient("mongodb://localhost:27017")
db = client.registry

def provision_tenant(name,domainName,db):
    tenant = db.tenants.find_one({"domainName": domainName})
    if not tenant:
        db.tenants.insert({"name": name, "domainName" : domainName})
        tenant = db.tenants.find_one({"name": name})
    return tenant[u'_id']

def provision_user(tenant_id,email,db):
    user = db.users.find_one({"_id" : email})
    if not user:
        db.users.insert({"_id" : email, "tenant" : DBRef("tenants", tenant_id), "password" : "konker123++" })
        return True

tenants = []

tenant_id = provision_tenant("Konker","konker",db)
if tenant_id:
    tenants.append(tenant_id)
    if provision_user(tenant_id,"konker@konkerlabs.com",db):
        changed = True
tenant_id = provision_tenant("InMetrics","inm",db)
if tenant_id:
    tenants.append(tenant_id)
    if provision_user(tenant_id,"inm@konkerlabs.com",db):
        changed = True

print 'changed=%s comment="Provisioned tenant IDs: %s"' % (changed, tenants)
