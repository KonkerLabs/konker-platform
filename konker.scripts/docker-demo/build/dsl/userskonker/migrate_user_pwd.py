#! /usr/bin/python27
from pymongo import MongoClient
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


def update_user_password():
    for user in db.users.find():
        if qualifier not in user.get(u'password', ""):
            hashed = get_hashed_password(user.get(u'password', ""))
            print("New password for user " + user[u'_id'] + " from ********** to " + hashed)
            db.users.update({"_id": user[u'_id']},
                            {"$set": {"password" : hashed}},
                            multi=False)
        else:
            continue
    return True


def get_hashed_password(password):
    salt = gen_salt(saltHashBytes)
    hashed = normalize(encode_to_hash(password, salt), salt)
    return hashed


def gen_salt(rounds):
    return binascii.hexlify(os.urandom(rounds))


def encode_to_hash(plain_password, salt):
    hashed_password_bin = hashlib.pbkdf2_hmac(
        saltHashAlgorithim,
        plain_password,
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
    update_user_password()


if __name__ == "__main__":
    main()

