#! /usr/bin/env python2
from pymongo import MongoClient
from bson.dbref import DBRef
from bson.objectid import ObjectId

changed = False
client = MongoClient("mongodb://localhost:27017")
db = client.registry


def ingest_privileges():
    if "privileges" not in db.collection_names():
        try:
            db.privileges.insert_many([
                {
                    "_id": ObjectId("5858266439f11e61b1fab1e2"),
                    "name": "VIEW_USER_PROFILE"
                },
                {
                    "_id": ObjectId("5858266439f11e61b1fab1e3"),
                    "name": "EDIT_OWN_USER_PROFILE"
                },
                {
                    "_id": ObjectId("5858266439f11e61b1fab1e4"),
                    "name": "CHANGE_OWN_PASSWORD"
                },
                {
                    "_id": ObjectId("5858266439f11e61b1fab1e5"),
                    "name": "VIEW_USER_NOTIFICATION_MESSAGES"
                },
                {
                    "_id": ObjectId("5858266439f11e61b1fab1e6"),
                    "name": "MARK_USER_NOTIFICATION_AS_READ"
                },
                {
                    "_id": ObjectId("5858266439f11e61b1fab1e7"),
                    "name": "LOGIN"
                },
                {
                    "_id": ObjectId("5858266439f11e61b1fab1e8"),
                    "name": "LOGOUT"
                },
                {
                    "_id": ObjectId("5858266439f11e61b1fab1e9"),
                    "name": "LIST_DEVICES"
                },
                {
                    "_id": ObjectId("5858266439f11e61b1fab1ea"),
                    "name": "ADD_DEVICE"
                },
                {
                    "_id": ObjectId("5858266439f11e61b1fab1eb"),
                    "name": "EDIT_DEVICE"
                },
                {
                    "_id": ObjectId("5858266439f11e61b1fab1ec"),
                    "name": "REMOVE_DEVICE"
                },
                {
                    "_id": ObjectId("5858266439f11e61b1fab1ed"),
                    "name": "SHOW_DEVICE"
                },
                {
                    "_id": ObjectId("5858266439f11e61b1fab1ee"),
                    "name": "CREATE_DEVICE_KEYS"
                },
                {
                    "_id": ObjectId("5858266439f11e61b1fab1ef"),
                    "name": "VIEW_DEVICE_LOG"
                },
                {
                    "_id": ObjectId("5858266439f11e61b1fab1f0"),
                    "name": "VIEW_DEVICE_CHART"
                },
                {
                    "_id": ObjectId("5858266439f11e61b1fab1f1"),
                    "name": "EXPORT_DEVICE_CSV"
                },
                {
                    "_id": ObjectId("5858266439f11e61b1fab1f2"),
                    "name": "LIST_ROUTES"
                },
                {
                    "_id": ObjectId("5858266439f11e61b1fab1f3"),
                    "name": "CREATE_DEVICE_ROUTE"
                },
                {
                    "_id": ObjectId("5858266439f11e61b1fab1f4"),
                    "name": "EDIT_DEVICE_ROUTE"
                },
                {
                    "_id": ObjectId("5858266439f11e61b1fab1f5"),
                    "name": "SHOW_DEVICE_ROUTE"
                },
                {
                    "_id": ObjectId("5858266439f11e61b1fab1f6"),
                    "name": "REMOVE_DEVICE_ROUTE"
                },
                {
                    "_id": ObjectId("5858266439f11e61b1fab1f7"),
                    "name": "LIST_REST_DESTINATIONS"
                },
                {
                    "_id": ObjectId("5858266439f11e61b1fab1f8"),
                    "name": "CREATE_REST_DESTINATION"
                },
                {
                    "_id": ObjectId("5858266439f11e61b1fab1f9"),
                    "name": "REMOVE_REST_DESTINATION"
                },
                {
                    "_id": ObjectId("5858266439f11e61b1fab1fa"),
                    "name": "EDIT_REST_DESTINATION"
                },
                {
                    "_id": ObjectId("5858266439f11e61b1fab1fb"),
                    "name": "LIST_SMS_DESTINATIONS"
                },
                {
                    "_id": ObjectId("5858266439f11e61b1fab1fc"),
                    "name": "CREATE_SMS_DESTINATION"
                },
                {
                    "_id": ObjectId("5858266439f11e61b1fab1fd"),
                    "name": "REMOVE_SMS_DESTINATION"
                },
                {
                    "_id": ObjectId("5858266439f11e61b1fab1fe"),
                    "name": "SHOW_SMS_DESTINATION"
                },
                {
                    "_id": ObjectId("5858266439f11e61b1fab1ff"),
                    "name": "LIST_TRANSFORMATION"
                },
                {
                    "_id": ObjectId("5858266439f11e61b1fab200"),
                    "name": "CREATE_TRANSFORMATION"
                },
                {
                    "_id": ObjectId("5858266439f11e61b1fab201"),
                    "name": "REMOVE_TRANSFORMATION"
                },
                {
                    "_id": ObjectId("5858266439f11e61b1fab202"),
                    "name": "EDIT_TRANSFORMATION"
                },
                {
                    "_id": ObjectId("5858266439f11e61b1fab203"),
                    "name": "LIST_ENRICHMENT"
                },
                {
                    "_id": ObjectId("5858266439f11e61b1fab204"),
                    "name": "CREATE_ENRICHMENT"
                },
                {
                    "_id": ObjectId("5858266439f11e61b1fab205"),
                    "name": "REMOVE_ENRICHMENT"
                },
                {
                    "_id": ObjectId("5858266439f11e61b1fab206"),
                    "name": "SHOW_ENRICHMENT"
                },
                {
                    "_id": ObjectId("5858266439f11e61b1fab207"),
                    "name": "LIST_ANALYTICS_NOTEBOOKS"
                },
                {
                    "_id": ObjectId("5858266439f11e61b1fab208"),
                    "name": "CREATE_ANALYTICS_NOTEBOOKS"
                },
                {
                    "_id": ObjectId("5858266439f11e61b1fab209"),
                    "name": "MODIFY_ANALYTICS_NOTEBOOKS"
                },
                {
                    "_id": ObjectId("5858266439f11e61b1fab20a"),
                    "name": "REMOVE_ANALYTICS_NOTEBOOKS"
                },
                {
                    "_id": ObjectId("5858266439f11e61b1fab20b"),
                    "name": "EXPLORE_ANALYTICS_DATASOURCE"
                },
                {
                    "_id": ObjectId("5858266439f11e61b1fab20c"),
                    "name": "LIST_ANALYTICS_DASHBOARDS"
                },
                {
                    "_id": ObjectId("5858266439f11e61b1fab20d"),
                    "name": "CREATE_ANALYTICS_DASHBOARDS"
                },
                {
                    "_id": ObjectId("5858266439f11e61b1fab20e"),
                    "name": "MODIFY_ANALYTICS_DASHBOARDS"
                },
                {
                    "_id": ObjectId("5858266439f11e61b1fab20f"),
                    "name": "REMOVE_ANALYTICS_DASHBOARDS"
                },
                {
                    "_id": ObjectId("585bcc7840bce8c571ba0237"),
                    "name": "SHOW_REST_DESTINATION"
                },
                {
                    "_id": ObjectId("585bcc7840bce8c571ba0238"),
                    "name": "EDIT_SMS_DESTINATION"
                },
                {
                    "_id": ObjectId("585bcc7840bce8c571ba0239"),
                    "name": "SHOW_TRANSFORMATION"
                },
                {
                    "_id": ObjectId("585bcc7840bce8c571ba023a"),
                    "name": "EDIT_ENRICHMENT"
                }])
            return True
        except Exception as e:
            print(e)
            return False
    else:
        return False


def ingest_roles():
    uid = db.privileges.find_one({"_id": ObjectId("585bcc7840bce8c571ba023a")})[u'_id']
    if "roles" not in db.collection_names():
        try:
            db.roles.insert_many([
                {
                    "_id": ObjectId("58542d56861bd736c42a0204"),
                    "name": "ROLE_SUPER_USER",
                    "privileges": [
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1e2")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1e3")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1e4")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1e5")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1e6")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1e7")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1e8")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1e9")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1ea")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1eb")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1ec")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1ed")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1ee")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1ef")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1f0")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1f1")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1f2")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1f3")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1f4")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1f5")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1f6")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1f7")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1f8")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1f9")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1fa")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1fb")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1fc")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1fd")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1fe")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1ff")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab200")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab201")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab202")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab203")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab204")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab205")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab206")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab207")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab208")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab209")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab20a")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab20b")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab20c")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab20d")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab20e")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab20f")),
                        DBRef("privileges", ObjectId("585bcc7840bce8c571ba0237")),
                        DBRef("privileges", ObjectId("585bcc7840bce8c571ba0238")),
                        DBRef("privileges", ObjectId("585bcc7840bce8c571ba0239")),
                        DBRef("privileges", ObjectId("585bcc7840bce8c571ba023a"))
                    ]
                },
                {
                    "_id": ObjectId("58542d56861bd736c42a0203"),
                    "name": "ROLE_ANALYTICS_USER",
                    "privileges": [
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1e2")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1e3")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1e4")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1e5")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1e6")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1e7")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1e8")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab207")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab208")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab209")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab20a")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab20b")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab20c")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab20d")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab20e")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab20f"))

                    ]
                },
                {
                    "_id": ObjectId("58542d56861bd736c42a0202"),
                    "name": "ROLE_IOT_USER",
                    "privileges": [
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1e2")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1e3")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1e4")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1e5")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1e6")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1e7")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1e8")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1e9")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1ea")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1eb")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1ec")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1ed")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1ee")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1ef")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1f0")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1f1")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1f2")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1f3")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1f4")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1f5")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1f6")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1f7")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1f8")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1f9")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1fa")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab1ff")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab200")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab201")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab202")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab203")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab204")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab205")),
                        DBRef("privileges", ObjectId("5858266439f11e61b1fab206")),
                        DBRef("privileges", ObjectId("585bcc7840bce8c571ba0237")),
                        DBRef("privileges", ObjectId("585bcc7840bce8c571ba0239")),
                        DBRef("privileges", ObjectId("585bcc7840bce8c571ba023a"))

                    ]
                }
            ])
            return True
        except Exception as e:
            print(e)
            return False
    else:
        return False


def update_user_roles():
    for user in db.users.find():
        db.users.save({
            "_id": user[u'_id'],
            "tenant": user[u'tenant'],
            "password": user[u'password'],
			"phone": user[u'phone'],
            "name": user[u'name'],
            "notificationViaEmail": user[u'notificationViaEmail'],
            "language": "PT_BR",
            "dateformat": "DDMMYYYY",
            "zoneId": "AMERICA_SAO_PAULO",
            "roles": [
                DBRef("roles", ObjectId("58542d56861bd736c42a0202"))
            ]
        })
    return True


def main():
    ingest_privileges()
    ingest_roles()
    update_user_roles()


def update_to_version_0_1():
    main()

if __name__ == "__main__":
    main()
