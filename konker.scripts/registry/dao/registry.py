#! /usr/bin/env python2
import sys
from bson import DBRef
from pymongo import MongoClient

from users.migrate_user_pwd import get_hashed_password
from users.migrate_user_roles import update_user_roles

db_version = "0.1"


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
    if not args.org:
        print("Info: The konker username will be used as organization name")
        org = name
    else:
        org = args.org
    tenant = tenant_find(org)
    if tenant is None:
        try:
            inserted_id = db.tenants.insert_one({"name": org, "domainName": org}).inserted_id
            return inserted_id
        except Exception as e:
            print(e)
            sys.exit(1)
    else:
        return tenant['_id']


def create_user(args):
    db = db_connect()
    user = user_find(args.user)
    if user is None:
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
        sys.exit(1)


def create_versioning_collection():
    db = db_connect()
    if "konkerVersion" not in db.collection_names():
        db.create_collection("konkerVersion")
        db.konkerVersion.insert(
            {
                "version": db_version
            }
        )
        print("Database version: " + db_version)


def upgrade_version(args):
    create_versioning_collection()

    db = db_connect()
    version = db.konkerVersion.find_one()

    if not args.version:
        v = db_version
    else:
        v = args.version

    if float(version['version']) < float(v):
        try:
            db.konkerVersion.update_one(
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


def create_users_collection():
    db = db_connect()
    if "users" not in db.collection_names():
        db.create_collection("users")


def users_count():
    create_users_collection()
    db = db_connect()

    try:
        number_of_users = db.users.find({}).count()
    except Exception as e:
        print(e)
        sys.exit(1)

    return number_of_users

