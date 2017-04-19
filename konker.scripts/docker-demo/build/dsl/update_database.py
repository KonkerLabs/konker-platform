#! /usr/bin/env python2
import sys

from pymongo import MongoClient
from dao.patches.v0_2_0_create_applications import create_applications


def db_connect(host='localhost', port=27017):
    try:
        client = MongoClient("mongodb://%s:%d" % (host, port))
    except Exception as e:
        print(e)
        sys.exit(1)
    return client.registry


def get_database_version():
    db = db_connect()
    if "konkerVersion" not in db.collection_names():
        db.create_collection("konkerVersion")
        db.konkerVersion.insert(
            {
                "version": "0.1"
            }
        )

    document = db.konkerVersion.find_one({"version": { "$exists": True }})
    if document is None:
        db.konkerVersion.insert(
            {
                "version": "0.1"
            }
        )

    document = db.konkerVersion.find_one({"version": { "$exists": True }})

    return document['version']


def update_version(version):
    db = db_connect()

    document = db.konkerVersion.find_one({"version": { "$exists": True }})
    if document is None:
        db.konkerVersion.update(
            {
                "version": str(version)
            }
        )


def upgrade_version():
    db = db_connect()
    version = get_database_version()

    if float(version) < 0.1:
        update_version(0.1)
    if float(version) < 0.2:
        create_applications()
        update_version(0.2)


def main():
    upgrade_version()


if __name__ == '__main__':
    main()
