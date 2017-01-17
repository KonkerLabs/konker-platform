#! /usr/bin/env python2
import sys
from bson import DBRef
from pymongo import MongoClient

from users.migrate_user_pwd import get_hashed_password
from users.migrate_user_roles import update_user_roles

db_version = "0.1"
versioning_collection_name = "version"


def db_connect(host='localhost', port=27017):
    try:
        client = MongoClient("mongodb://%s:%d" % (host, port))
    except Exception as e:
        print(e)
        sys.exit(1)
    return client.registry


def user_find(username):
    db = db_connect()
    try:
        user = db.users.find_one({"_id": username})
    except Exception as e:
        print(e)
        sys.exit(1)
    return user


def tenant_find(tenant_name):
    db = db_connect()
    try:
        tenant = db.tenants.find_one({"name": tenant_name})
    except Exception as e:
        print(e)
        sys.exit(1)
    return tenant


def create_tenant(args, name):
    db = db_connect()
    tenant = tenant_find(name)
    if tenant is None:
        if not args.org:
            org = name
        else:
            org = args.org
        try:
            inserted_id = db.tenants.insert_one({"name": org, "domainName": org}).inserted_id
            return inserted_id
        except Exception as e:
            print(e)
            sys.exit(1)
    else:
        return tenant


def create_user(args):
    db = db_connect()
    user = user_find(args.user)
    if user is None:
        print("Info: The konker username will be used as organization name")
        username = args.user
        tenant_id = create_tenant(args, username)

        new_user = {
            "_id": username,
            "language": "PT_BR",
            "dateFormat": "DDMMYYYY",
            "zoneId": "AMERICA_SAO_PAULO",
            "password": get_hashed_password(args.password.encode(encoding='UTF-8')).decode(),
            "tenant": DBRef("tenants", tenant_id)
        }

        try:
            db.users.insert_one(new_user)
            update_user_roles()
            print("Konker user created")
        except Exception as e:
            print(e)
            sys.exit(1)
    else:
        print("This konker user already exists")
        sys.exit(0)


def update_user(args):
    db = db_connect()
    user = user_find(args.user)
    if user is not None:
        try:
            db.users.update_one(
                {
                    '_id': user['_id']
                },
                {
                    '$set':
                        {
                            'password': get_hashed_password(args.password.encode(encoding='UTF-8')).decode()
                        }
                }, upsert=False)
            print("Konker user password updated")
        except Exception as e:
            print(e)
            sys.exit(1)
    else:
        print("Konker username not found")
        sys.exit(0)


def create_versioning_collection():
    db = db_connect()
    if versioning_collection_name not in db.collection_names():
        db.create_collection(versioning_collection_name)
        db.version.insert(
            {
                "version": db_version
            }
        )
        print("Database version: " + db_version)


def upgrade_version(args):
    create_versioning_collection()

    db = db_connect()
    version = db.version.find_one()
    if float(version['version']) < float(args.version):
        try:
            db.version.update_one(
                {
                    '_id': version['_id']
                },
                {
                    '$set':
                        {
                            'version': args.version
                        }
                }, upsert=False)
            print("Database version upgraded to " + args.version)
        except Exception as e:
            print(e)
            sys.exit(1)
    else:
        print("Database already upgraded")
        sys.exit(0)



