#! /usr/bin/env python2
from pymongo import MongoClient
from bson.dbref import DBRef
import getopt
import sys

changed=False

def db_connect(host='localhost', port=27017):
    client = MongoClient("mongodb://%s:%d" % (host, port))
    db = client.registry
    return db

def tenant_find(domain_name, db):
    tenant = db.tenants.find_one({"domainName": domain_name})
    if tenant is not None:
        return tenant[u'_id']
    return None

def tenant_provision(name,domain_name,db):
    tenant_id = tenant_find(domain_name, db)
    if not tenant_id:
        db.tenants.insert({"name": name, "domainName" : domain_name})
        tenant_id = tenant_find(domain_name, db)
    return tenant_id

def user_find(username, db):
    user = db.users.find_one({"_id" : username})
    if user is not None:
        return user[u'_id']
    return None

def user_provision(tenant_id,username,password,db):
    user_id = user_find(username, db)
    if not user_id:
        db.users.insert({"_id" : username, "tenant" : DBRef("tenants", tenant_id), "password" : password })
        user_id = user_find(username, db)
    return user_id

def parse_args():
   params = {'port': 27017, 'tenant_domain': None, 'tenant_name': None, 'username': None, 'password': None}
   try:
       opts, args = getopt.getopt(sys.argv[1:],"hP:d:t:u:p:F")
   except getopt.GetoptError:
      print 'manage_tenant_and_user [-P port] -d <tenant domain> -t <tenant name> -u <username> -p <password>'
      sys.exit(2)
   for opt, arg in opts:
      if opt == '-h':
          print 'manage_tenant_and_user [-P port] -d <tenant domain> -t <tenant name> -u <username> -p <password>'
          sys.exit()
      elif opt in ("-P"):
         params['port'] = int(arg)
      elif opt in ("-d"):
         params['tenant_domain'] = arg
      elif opt in ("-t"):
         params['tenant_name'] = arg
      elif opt in ("-u"):
         params['username'] = arg
      elif opt in ("-p"):
         params['password'] = arg
   if params['tenant_domain'] is None:
       print 'tenant_domain is mandatory'
       sys.exit(2)
   if params['tenant_name'] is None:
       print 'tenant_name is mandatory'
       sys.exit(2)
   if params['username'] is None:
       print 'username is mandatory'
       sys.exit(2)
   if params['password'] is None:
       print 'password is mandatory'
       sys.exit(2)
   return params


if __name__ == "__main__":
    params = parse_args()
    db = db_connect(port=params['port'])
    tenant_id = tenant_find(domain_name=params['tenant_domain'], db=db)
    if tenant_id is None:
        tenant_id = tenant_provision(name=params['tenant_name'], domain_name=params['tenant_domain'], db=db)
        if tenant_id is None:
            print "Error: Tenant could no be created"
            sys.exit(2)
        changed = True
    user_id = user_find(username=params['username'], db=db)
    if user_id is None:
        user_id = user_provision(tenant_id=tenant_id, username=params['username'], password=params['password'], db=db)
        if user_id is None:
            print "Error: User could no be created"
            sys.exit(2)
        changed = True
    print 'changed=%s comment="Tenant ID:%s User ID:%s"' % (changed, tenant_id, user_id)
