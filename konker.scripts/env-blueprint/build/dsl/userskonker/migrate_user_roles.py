#! /usr/bin/python27
from pymongo import MongoClient
from bson.dbref import DBRef
from bson.objectid import ObjectId

changed = False
client = MongoClient("mongodb://root:mongo@%s:%d" % ("mongodb", 27017))
db = client.registry


def ingest_privileges():
    if "privileges" not in db.collection_names():
        try:
            db.privileges.insert_many([
                 {
                    "_id": ObjectId("67b0d308cf7701194761a11c"),
                    "name": "LIST_DEVICE_CONFIGS"
                },
                 {
                    "_id": ObjectId("67b0d30e38a6b60f64b4dd0a"),
                    "name": "SHOW_DEVICE_CONFIG"
                },
                 {
                    "_id": ObjectId("67b0d315bad4a47b25b1d94a"),
                    "name": "CREATE_DEVICE_CONFIG"
                },
                 {
                    "_id": ObjectId("67b0d318d05fdf9817a79d9e"),
                    "name": "EDIT_DEVICE_CONFIG"
                },
                 {
                    "_id": ObjectId("67b0d31ba65e632c4c43fb17"),
                    "name": "REMOVE_DEVICE_CONFIG"
                },
                 {
                    "_id": ObjectId("67b0d31e8a0ddcdbe32d434e"),
                    "name": "SHOW_DEVICE_CONFIG"
                },
                {
                    "_id": ObjectId("67b0d32057510f71eb575f3d"),
                    "name": "CREATE_DEVICE_MODEL"
                },
                {
                    "_id": ObjectId("67b0d323d5dda94eb49a36e4"),
                    "name": "LIST_DEVICE_MODEL"
                },
                {
                    "_id": ObjectId("67b0d329229b55c360986a00"),
                    "name": "SHOW_DEVICE_MODEL"
                },
                {
                    "_id": ObjectId("67b0d32d3d9dd166fe0fb901"),
                    "name": "EDIT_DEVICE_MODEL"
                },
                {
                    "_id": ObjectId("67b0d330aff76cb63fbfe327"),
                    "name": "REMOVE_DEVICE_MODEL"
                },
                {
                    "_id": ObjectId("67b0d336c4c369e6d7530af8"),
                    "name": "LIST_GATEWAYS"
                },
                {
                    "_id": ObjectId("67b0d3f0cf272a02b1545347"),
                    "name": "SHOW_GATEWAY"
                },
                {
                    "_id": ObjectId("67b0d3f85209d3d06945eb25"),
                    "name": "EDIT_GATEWAY"
                },
                {
                    "_id": ObjectId("67b0d3fcca83b5c2a65f5ee8"),
                    "name": "CREATE_GATEWAY"
                },
                {
                    "_id": ObjectId("67b0d3ff4d5a80b25f622607"),
                    "name": "REMOVE_GATEWAY"
                },
                {
                    "_id": ObjectId("67b0d4021a6bea1dd96cebcd"),
                    "name": "LIST_LOCATIONS"
                },
                {
                    "_id": ObjectId("67b0d40456789cfacb58651d"),
                    "name": "SHOW_LOCATION"
                },
                {
                    "_id": ObjectId("67b0d40a39d700260811d48f"),
                    "name": "CREATE_LOCATION"
                },
                {
                    "_id": ObjectId("67b0d40d8d86a0876770451c"),
                    "name": "EDIT_LOCATION"
                },
                {
                    "_id": ObjectId("67b0d410ce70ec105ad1fe09"),
                    "name": "REMOVE_LOCATION"
                },
                {
                    "_id": ObjectId("67b0d4687a4e4b3323487899"),
                    "name": "SHOW_PRIVATE_STORAGE"
                },
                {
                    "_id": ObjectId("67b0d46ad5bba720fad00a81"),
                    "name": "ADD_PRIVATE_STORAGE"
                },
                {
                    "_id": ObjectId("67b0d46df0006ef14a3ac434"),
                    "name": "EDIT_PRIVATE_STORAGE"
                },
                {
                    "_id": ObjectId("67b0d46f892ea491a55699fe"),
                    "name": "REMOVE_PRIVATE_STORAGE"
                },
                {
                    "_id": ObjectId("67b0d2c3cec47fb11fb65317"),
                    "name": "LIST_ALERT_TRIGGERS"
                },
                {
                    "_id": ObjectId("67b0d2cfe18c35e4048289af"),
                    "name": "CREATE_ALERT_TRIGGER"
                },
                {
                    "_id": ObjectId("67b0d2d3ed5cd47804d57ac0"),
                    "name": "REMOVE_ALERT_TRIGGER"
                },
                {
                    "_id": ObjectId("67b0d2d643ff32ffe02d77de"),
                    "name": "EDIT_ALERT_TRIGGER"
                },

                {
                    "_id": ObjectId("67b0d1b58d008fecd536089e"),
                    "name": "LIST_APPLICATION"
                },
                {
                    "_id": ObjectId("67b0d1bc1f9a30fb07e450ae"),
                    "name": "SHOW_APPLICATION"
                },
                {
                    "_id": ObjectId("67b0d1c2a276b0f9316a6577"),
                    "name": "ADD_APPLICATION"
                },
                {
                    "_id": ObjectId("67b0d1c6ec9a985e7ca150d7"),
                    "name": "EDIT_APPLICATION"
                },
                {
                    "_id": ObjectId("67b0d1c954686c33fef12513"),
                    "name": "REMOVE_APPLICATION"
                },
                {
                    "_id": ObjectId("67b0cfc969d359b61b72b950"),
                    "name": "LIST_USER"
                },
                {
                    "_id": ObjectId("67b0cfdea593e77dfbce18cb"),
                    "name": "SHOW_USER"
                },
                {
                    "_id": ObjectId("67b0cff65f20b917accd4a04"),
                    "name": "ADD_USER"
                },
                {
                    "_id": ObjectId("67b0d0075260f0f1ca88226d"),
                    "name": "EDIT_USER"
                },
                {
                    "_id": ObjectId("67b0d019c20cd0bf64513d81"),
                    "name": "REMOVE_USER"
                },
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
                        DBRef("privileges", ObjectId("585bcc7840bce8c571ba023a")),
                        DBRef("privileges", ObjectId("67b0cfc969d359b61b72b950")),
                        DBRef("privileges", ObjectId("67b0cfdea593e77dfbce18cb")),
                        DBRef("privileges", ObjectId("67b0cfc969d359b61b72b950")),
                        DBRef("privileges", ObjectId("67b0cff65f20b917accd4a04")),
                        DBRef("privileges", ObjectId("67b0d0075260f0f1ca88226d")),
                        DBRef("privileges", ObjectId("67b0d019c20cd0bf64513d81")),
                        DBRef("privileges", ObjectId("67b0d1b58d008fecd536089e")),

                        DBRef("privileges", ObjectId("67b0d2c3cec47fb11fb65317")),
                        DBRef("privileges", ObjectId("67b0d2cfe18c35e4048289af")),
                        DBRef("privileges", ObjectId("67b0d2d3ed5cd47804d57ac0")),
                        DBRef("privileges", ObjectId("67b0d2d643ff32ffe02d77de")),
                        DBRef("privileges", ObjectId("67b0d308cf7701194761a11c")),
                        DBRef("privileges", ObjectId("67b0d30e38a6b60f64b4dd0a")),
                        DBRef("privileges", ObjectId("67b0d315bad4a47b25b1d94a")),
                        DBRef("privileges", ObjectId("67b0d318d05fdf9817a79d9e")),
                        DBRef("privileges", ObjectId("67b0d31ba65e632c4c43fb17")),
                        DBRef("privileges", ObjectId("67b0d31e8a0ddcdbe32d434e")),
                        DBRef("privileges", ObjectId("67b0d32057510f71eb575f3d")),
                        DBRef("privileges", ObjectId("67b0d323d5dda94eb49a36e4")),
                        DBRef("privileges", ObjectId("67b0d329229b55c360986a00")),
                        DBRef("privileges", ObjectId("67b0d32b0e96e5354553802c")),
                        DBRef("privileges", ObjectId("67b0d32d3d9dd166fe0fb901")),
                        DBRef("privileges", ObjectId("67b0d330aff76cb63fbfe327")),
                        DBRef("privileges", ObjectId("67b0d336c4c369e6d7530af8")),
                        DBRef("privileges", ObjectId("67b0d3f0cf272a02b1545347")),
                        DBRef("privileges", ObjectId("67b0d3f85209d3d06945eb25")),
                        DBRef("privileges", ObjectId("67b0d3fcca83b5c2a65f5ee8")),
                        DBRef("privileges", ObjectId("67b0d3ff4d5a80b25f622607")),
                        DBRef("privileges", ObjectId("67b0d4021a6bea1dd96cebcd")),
                        DBRef("privileges", ObjectId("67b0d40456789cfacb58651d")),
                        DBRef("privileges", ObjectId("67b0d40a39d700260811d48f")),
                        DBRef("privileges", ObjectId("67b0d40d8d86a0876770451c")),
                        DBRef("privileges", ObjectId("67b0d410ce70ec105ad1fe09")),
                        DBRef("privileges", ObjectId("67b0d4687a4e4b3323487899")),
                        DBRef("privileges", ObjectId("67b0d46ad5bba720fad00a81")),
                        DBRef("privileges", ObjectId("67b0d46df0006ef14a3ac434")),
                        DBRef("privileges", ObjectId("67b0d46f892ea491a55699fe")),
                        DBRef("privileges", ObjectId("67b0d1bc1f9a30fb07e450ae")),
                        DBRef("privileges", ObjectId("67b0d1c2a276b0f9316a6577")),
                        DBRef("privileges", ObjectId("67b0d1c6ec9a985e7ca150d7")),
                        DBRef("privileges", ObjectId("67b0d1c954686c33fef12513"))                               
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
                        DBRef("privileges", ObjectId("585bcc7840bce8c571ba023a")),
                        DBRef("privileges", ObjectId("67b0d1b58d008fecd536089e")),
                        DBRef("privileges", ObjectId("67b0d1bc1f9a30fb07e450ae")),
                        DBRef("privileges", ObjectId("67b0d1c2a276b0f9316a6577")),
                        DBRef("privileges", ObjectId("67b0d1c6ec9a985e7ca150d7")),
                        DBRef("privileges", ObjectId("67b0d1c954686c33fef12513")),
                        DBRef("privileges", ObjectId("67b0d2c3cec47fb11fb65317")),
                        DBRef("privileges", ObjectId("67b0d2cfe18c35e4048289af")),
                        DBRef("privileges", ObjectId("67b0d2d3ed5cd47804d57ac0")),
                        DBRef("privileges", ObjectId("67b0d2d643ff32ffe02d77de")),
                        DBRef("privileges", ObjectId("67b0d308cf7701194761a11c")),
                        DBRef("privileges", ObjectId("67b0d30e38a6b60f64b4dd0a")),
                        DBRef("privileges", ObjectId("67b0d315bad4a47b25b1d94a")),
                        DBRef("privileges", ObjectId("67b0d318d05fdf9817a79d9e")),
                        DBRef("privileges", ObjectId("67b0d31ba65e632c4c43fb17")),
                        DBRef("privileges", ObjectId("67b0d31e8a0ddcdbe32d434e")),
                        DBRef("privileges", ObjectId("67b0d32057510f71eb575f3d")),
                        DBRef("privileges", ObjectId("67b0d323d5dda94eb49a36e4")),
                        DBRef("privileges", ObjectId("67b0d329229b55c360986a00")),
                        DBRef("privileges", ObjectId("67b0d32b0e96e5354553802c")),
                        DBRef("privileges", ObjectId("67b0d32d3d9dd166fe0fb901")),
                        DBRef("privileges", ObjectId("67b0d330aff76cb63fbfe327")),
                        DBRef("privileges", ObjectId("67b0d336c4c369e6d7530af8")),
                        DBRef("privileges", ObjectId("67b0d3f0cf272a02b1545347")),
                        DBRef("privileges", ObjectId("67b0d3f85209d3d06945eb25")),
                        DBRef("privileges", ObjectId("67b0d3fcca83b5c2a65f5ee8")),
                        DBRef("privileges", ObjectId("67b0d3ff4d5a80b25f622607")),
                        DBRef("privileges", ObjectId("67b0d4021a6bea1dd96cebcd")),
                        DBRef("privileges", ObjectId("67b0d40456789cfacb58651d")),
                        DBRef("privileges", ObjectId("67b0d40a39d700260811d48f")),
                        DBRef("privileges", ObjectId("67b0d40d8d86a0876770451c")),
                        DBRef("privileges", ObjectId("67b0d410ce70ec105ad1fe09")),
                        DBRef("privileges", ObjectId("67b0d4687a4e4b3323487899")),
                        DBRef("privileges", ObjectId("67b0d46ad5bba720fad00a81")),
                        DBRef("privileges", ObjectId("67b0d46df0006ef14a3ac434")),
                        DBRef("privileges", ObjectId("67b0d46f892ea491a55699fe"))  


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
        changes = {}
        if not "roles" in user:
            changes["roles"] = [DBRef("roles", ObjectId("58542d56861bd736c42a0202"))]
        if not "language" in user:
            changes["language"] = "PT_BR"
        if not "dateformat" in user:
            changes["dateformat"] = "DDMMYYYY"
        if not "zoneId" in user:
            changes["zoneId"] = "AMERICA_SAO_PAULO"

        if changes:
            print "Updating roles for {}: {}".format( user[u'_id'], str(changes))
            db.users.update({"_id": user[u'_id']},
                            {"$set": changes},
                            multi=False)
    return True


def main():
    ingest_privileges()
    ingest_roles()
    update_user_roles()


def update_to_version_0_1():
    main()

if __name__ == "__main__":
    main()
