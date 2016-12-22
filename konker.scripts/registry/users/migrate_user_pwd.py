#! /usr/bin/python27
from pymongo import MongoClient
from bson.dbref import DBRef
import base64
import hashlib
import binascii
import os

changed = False
client = MongoClient("mongodb://localhost:27017")
db = client.registry

qualifier = b'PBKDF2WithHmac'
prefixSeparator = b'$'
saltRounds = 10000
saltHashAlgorithim = "SHA256"
saltSize = 16
saltHashBytes = 32

def update_user_password(db):
    for user in db.users.find():
        if qualifier not in user[u'password']:
            salt = genSalt(saltHashBytes)
            hashed = normalize(encodeToHash(user[u'password'], salt), salt)
            print "New password for user " + user[u'_id'] + " from " + user[u'password'] + "to " + hashed
            db.users.save({
                "_id": user[u'_id'],
                "tenant": user[u'tenant'],
                "password": hashed,
                "language": "PT_BR",
                "dateformat": "DDMMYYYY",
                "zoneId": "AMERICA_SAO_PAULO"
            })
        else:
           continue

    return True

def genSalt(rounds):
    return binascii.hexlify(os.urandom(rounds))

def encodeToHash(plainPassword, salt):
    hashedPasswordBin = hashlib.pbkdf2_hmac(
        saltHashAlgorithim,
        plainPassword,
        salt,
        saltRounds
    )
    return hashedPasswordBin

def normalize(hashedPassword, salt):
    return qualifier + prefixSeparator + saltHashAlgorithim + \
           prefixSeparator + str(saltRounds) + prefixSeparator + \
           base64.b64encode(salt) + prefixSeparator +  \
           base64.b64encode(hashedPassword)

update_user_password(db)


