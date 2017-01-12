import argparse
import sys

from bson import DBRef
from pymongo import MongoClient

from users.migrate_user_pwd import get_hashed_password
from users.migrate_user_roles import update_user_roles


def db_connect(host='localhost', port=27017):
    try:
        client = MongoClient("mongodb://%s:%d" % (host, port))
    except Exception as e:
        print(e)
        sys.exit(1)
    return client.registry


def user_find(username, db):
    try:
        user = db.users.find_one({"_id": username})
    except Exception as e:
        print(e)
        sys.exit(1)
    if user is not None:
        return user
    return None


def tenant_find(tenant_name, db):
    try:
        tenant = db.tenants.find_one({"name": tenant_name})
    except Exception as e:
        print(e)
        sys.exit(1)
    if tenant is not None:
        return tenant
    return None


def create_tenant(args, name, db):
    tenant = tenant_find(name, db)
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


def create_user(args, db):
    user = user_find(args.user, db)
    if user is None:
        print("Info: The konker username will be used as organization name")
        username = args.user
        tenant_id = create_tenant(args, username, db)

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


def update(args, db):
    user = user_find(args.user, db)
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


def arguments(db):
    # Receiving arguments.
    parser = argparse.ArgumentParser(prog='konker user')

    sub_parser = parser.add_subparsers(title='subcommands', description='valid subcommands', help='Additional help')

    sub_parser_create = sub_parser.add_parser('create', description='create command', help='Create account')
    sub_parser_create.add_argument('user', help='Username', type=str)
    sub_parser_create.add_argument('password', help='Password', type=str)
    sub_parser_create.add_argument('--org', help='Organization name, account username used as default', type=str)
    sub_parser_create.set_defaults(func=create_user)

    sub_parser_update = sub_parser.add_parser('update', description='update command', help='Update account')
    sub_parser_update.add_argument('user', help='Specify the account username', type=str)
    sub_parser_update.add_argument('password', help='Specify the new account password', type=str)
    sub_parser_update.add_argument('--org', help='Organization name, account username used as default', type=str)
    sub_parser_update.set_defaults(func=update)

    args = parser.parse_args()
    args.func(args, db)


def main():
    registry = db_connect()
    arguments(registry)


if __name__ == "__main__":
    main()

