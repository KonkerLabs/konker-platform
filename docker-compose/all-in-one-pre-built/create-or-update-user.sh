#!/bin/env python3

import getpass
import binascii
import base64
import hashlib
import os
import time
import uuid

import pymongo
import bson

qualifier = b'PBKDF2WithHmac'
prefixSeparator = b'$'
saltRounds = 10000
saltHashAlgorithim = "SHA256"
saltSize = 16
saltHashBytes = 32
strn_qualifier = 'brsp01a'
strn_class_location = 'com.konkerlabs.platform.registry.business.model.Location'
strn_class_application = 'com.konkerlabs.platform.registry.business.model.Application'
strn_class_deviceModel = 'com.konkerlabs.platform.registry.business.model.DeviceModel'
strn_application_friendly_name = 'konker'
ls_strn_common_passwords = [
    '000000', '101010', '10203', '110110jp', '1111', '111222', '112233', '121212',
    '123', '12qwaszx', '131313', '147258', '147852', '159357', '159753', '1q2w3e',
    '1qaz2wsx', '1qazxsw2', '212121', '222222', '232323', '252525', '333333', '3601',
    '444444', '54321', '55555', '666666', '696969', '777777', '789456', '823477aa',
    '888888', '9136668099', '98765', '999999', 'd1lakiss', 'gizli', 'groupd2013',
    'indya123', 'liman1000', 'a1b2c3', 'aaa',
    'andrea', 'andrew', 'anthony', 'asdasd', 'asdf', 'azerty', 'baseball',
    'basketball', 'batman', 'charlie', 'computer', 'daniel', 'doudou', 'dragon',
    'eminem', 'football', 'freedom', 'fuckyou', 'gfhjkm', 'googledummy', 'guest',
    'hello', 'hunter', 'internet', 'jessica', 'jordan', 'junior', 'justin', 'juventus',
    'killer', 'loulou', 'love', 'luzit2000', 'mar20lt', 'marina', 'marseille', 'martin',
    'master', 'matrix', 'michael', 'michelle', 'monkey', 'naruto', 'nicolas', 'nicole',
    'nikita', 'pass', 'pokemon', 'princess', 'q1w2e3', 'qazwsx',
    'qqq', 'qweasd', 'qweqwe', 'qwer', 'robert', 'samsung', 'secret', 'shadow',
    'soccer', 'soleil', 'starwars', 'sunshine', 'superman', 'thomas', 'unknown', 'usr',
    'vip', 'welcome', 'xxx', 'zaq12wsx', 'zxcvbn', 'zzz']
client = pymongo.MongoClient('localhost:27017',
                             username='admin',
                             password='Admin@2023',
                             authSource='admin')
db = client.registry


def get_email_address_and_domain_name():
    flag_wronginp = False
    while True:
        if flag_wronginp:
            print("The given e-mail address is invalid.", end='\n\n')
            flag_wronginp = False
        strn_emailadd = input("Input e-mail address (or CTRL C to abort): ")
        ls_email_parts = strn_emailadd.split('@')
        if len(ls_email_parts) != 2:
            flag_wronginp = True
            continue
        if len(ls_email_parts[1].split('.')) < 2:
            flag_wronginp = True
            continue
        strn_dmainame = ''.join(ls_email_parts[1].split('.')[::-1]
                                 + ls_email_parts[0].split('.'))
        break
    return strn_emailadd, strn_dmainame


def get_password():
    strn_password = None
    while strn_password is None:
        while strn_password is None:
            strn_password = getpass.getpass('Input password, minimum of 8 characters'
                                            + ' (or CTRL C to abort): ')
            if (len(strn_password) < 8
                or len(set(strn_password)) < 5):
                strn_password = None
            else:
                for strn_cmmn in ls_strn_common_passwords:
                    if strn_cmmn in strn_password.lower():
                        strn_password = None
                        break
            if strn_password is not None:
                break
            print("The chosen password is too weak, try again.", end='\n\n')
        strn_verifctn = getpass.getpass('Repeat password (or CTRL C to abort): ')
        if strn_verifctn != strn_password:
            print("The two password entries don't match, try again.", end='\n\n')
            strn_password = None
        else:
            print()
            break
    return strn_password


def create_tenant(strn_emailadd, strn_dmainame):
    rslt_insertone = db.tenants.insert_one(document={'_id': strn_emailadd,
                                                     'name': strn_emailadd,
                                                     'domainName': strn_dmainame,})
    if rslt_insertone.acknowledged:
        print("Tenant created:\n"
              + "  id          \"{:s}\"\n".format(str(rslt_insertone.inserted_id))
              + "  name        \"{:s}\"\n".format(str(rslt_insertone.inserted_id))
              + "  domain name \"{:s}\"\n".format(strn_dmainame))
    else:
        print("Tenant creation error.")
        exit()


def create_application(unixtime_ms, strn_emailadd, strn_dmainame):
    # strn_application_id = strn_dmainame + '@' + strn_application_friendly_name
    strn_application_id = strn_dmainame + '@' + strn_dmainame
    rslt_insertone = db.applications.insert_one(
        document={'_id': strn_application_id,
                  '_class': strn_class_application,
                  'friendlyName': 'konker devices application',
                  'description': ("This application join devices and routes "
                                  + "of the konker application"),
                  'qualifier': strn_qualifier,
                  'registrationDate': unixtime_ms,
                  'tenant': bson.dbref.DBRef(collection='tenants', id=strn_emailadd),
                  })
    if rslt_insertone.acknowledged:
        print("Application created:\n"
              + "  id           \"{:s}\"\n".format(strn_application_id)
              + "  friendlyName \"{:s}\"\n".format(strn_application_friendly_name))
    else:
        print("Application creation error.")
        exit()


# def create_devicesModel(strn_emailadd, strn_dmainame):
#     strn_application_id = strn_dmainame + '@' + strn_application_friendly_name
#     strn_devicesModel_id = strn_emailadd
#     rslt_insertone = db.devicesModel.insert_one(
#         document={'_id': strn_devicesModel_id,
#                   '_class': strn_class_deviceModel,
#                   'guid': str(uuid.uuid4()),
#                   'name': 'default',
#                   'description': 'default model',
#                   'contentType': 'APPLICATION_JSON',
#                   'defaultModel': True,
#                   'tenant': bson.dbref.DBRef(collection='tenants', id=strn_emailadd),
#                   'application': bson.dbref.DBRef(collection='applications',
#                                                   id=strn_application_id),
#                   })
#     if rslt_insertone.acknowledged:
#         print("DevicesModel created:\n"
#               + "  id           \"{:s}\"\n".format(strn_devicesModel_id))
#     else:
#         print("DevicesModel creation error.")
#         exit()


# def create_location(strn_emailadd, strn_dmainame):
#     strn_application_id = strn_dmainame + '@' + strn_application_friendly_name
#     strn_location_id = strn_emailadd
#     rslt_insertone = db.locations.insert_one(
#         document={'_id': strn_location_id,
#                   '_class': strn_class_location,
#                   'name': 'default',
#                   'guid': str(uuid.uuid4()),
#                   'defaultLocation': True,
#                   'tenant': bson.dbref.DBRef(collection='tenants', id=strn_emailadd),
#                   'application': bson.dbref.DBRef(collection='applications',
#                                                   id=strn_application_id),
#                   })
#     if rslt_insertone.acknowledged:
#         print("Location created:\n"
#               + "  id           \"{:s}\"\n".format(strn_location_id))
#     else:
#         print("Location creation error.")
#         exit()


def create_user(unixtime_ms, strn_emailadd, hashed):
    rslt_insertone = db.users.insert_one(
        document={'_id': strn_emailadd,
                  'password': hashed,
                  'tenant': bson.dbref.DBRef(collection='tenants', id=strn_emailadd,
                                             database='registry'),
                  'zoneId': 'AMERICA_SAO_PAULO',
                  'language': 'PT_BR',
                  'roles': [bson.dbref.DBRef(collection='roles',
                                             id=bson.objectid.ObjectId(
                                                 '58542d56861bd736c42a0202')),],
                  'dateformat': 'DDMMYYYY',
                  'registrationDate': unixtime_ms,
                  'active': True })
    if rslt_insertone.acknowledged:
        print("Account created:\n  id \"{:s}\"".format(str(rslt_insertone.inserted_id)))
    else:
        print("Account creation error.")
        exit()


def create_or_update_account():
    try:
        strn_emailadd, strn_dmainame = get_email_address_and_domain_name()

        docm_user = db.users.find_one(filter={'_id': strn_emailadd})
        if docm_user is not None:
            while True:
                strn_rplc = input("The account identified by the e-mail address"
                                  + "\"{:s}\" already ".format(strn_emailadd)
                                  + " exists, do you want to update the account's password?"
                                  + " (yes/no/CTRL C to abort) ")
                if strn_rplc not in ('yes', 'no',):
                    print("Chosen option invalid.", end='\n\n')
                else:
                    break
            if strn_rplc == 'no':
                print("Operation aborted.")
                exit()

        strn_password = get_password()
    except KeyboardInterrupt:
        print('\n' + 'Operation aborted.', end='\n\n')
        exit()

    unixtime_ms = bson.Int64(time.time() * 1.e3)
    # hashed = get_hashed_password(bytes(strn_password, encoding='utf8'))
    hashed = get_hashed_password(strn_password)
    if docm_user is not None:
        db.users.update_one(filter={"_id": docm_user[u'_id']},
                            update={"$set": {"password" : hashed}},
                            upsert=False)
        print("Account updated:\n  id          \"{:s}\"".format(str(docm_user['_id'])))
    else:
        docm_tenant = db.tenants.find_one(filter={'_id': strn_emailadd})
        if docm_tenant != None:
            print("The tenant under the e-mail address \"{:s}\"".format(strn_emailadd)
                  + " already exists but the associated user account doesn't;"
                  + " the user creation will proceed.", end='\n\n')
        else:
            create_tenant(strn_emailadd, strn_dmainame)

        docm_application = db.applications.find_one(filter={'_id': strn_emailadd,})
        if docm_application != None:
            print("The application under the e-mail address"
                  + " \"{:s}\"".format(strn_emailadd)
                  + " already exists but the associated user account doesn't;"
                  + " the user creation will proceed.", end='\n\n')
        else:
            create_application(unixtime_ms, strn_emailadd, strn_dmainame)

        # docm_devicesModel = db.devicesModel.find_one(filter={'_id': strn_emailadd,})
        # if docm_devicesModel != None:
        #     print("The devicesModel under the e-mail address"
        #           + " \"{:s}\"".format(strn_emailadd)
        #           + " already exists but the associated user account doesn't;"
        #           + " the user creation will proceed.", end='\n\n')
        # else:
        #     create_devicesModel(strn_emailadd, strn_dmainame)

        # docm_location = db.locations.find_one(filter={'_id': strn_emailadd,})
        # if docm_location != None:
        #     print("The location under the e-mail address"
        #           + " \"{:s}\"".format(strn_emailadd)
        #           + " already exists but the associated user account doesn't;"
        #           + " the user creation will proceed.", end='\n\n')
        # else:
        #     create_location(strn_emailadd, strn_dmainame)

        create_user(unixtime_ms, strn_emailadd, hashed)


def get_hashed_password(password):
    salt = gen_salt(saltHashBytes)
    hashed = normalize(encode_to_hash(password, salt), salt).decode(encoding="utf-8")
    return hashed


def gen_salt(rounds):
    return binascii.hexlify(os.urandom(rounds))


def encode_to_hash(plain_password, salt):
    hashed_password_bin = hashlib.pbkdf2_hmac(
        saltHashAlgorithim,
        bytes(plain_password, encoding='utf8'),
        salt,
        saltRounds
    )
    return hashed_password_bin


def normalize(hashed_password, salt):
    return qualifier + prefixSeparator + saltHashAlgorithim.encode(encoding="utf-8") + \
           prefixSeparator + str(saltRounds).encode(encoding="utf-8") + prefixSeparator + \
           base64.b64encode(salt) + prefixSeparator + \
           base64.b64encode(hashed_password)


def main():
    create_or_update_account()


if __name__ == "__main__":
    main()
