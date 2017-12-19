#! /usr/bin/env python2
from pymongo import MongoClient

client = MongoClient("mongodb://localhost:27017")
db = client.registry
#db.authenticate("", "")

def main():
    total = 0
    for alertTrigger in db.alertTriggers.find({
            "$and": [
                {"name" : { "$exists" : False} },
                {"type" : "SILENCE" }
            ]}):
        alertTrigger.update({'name': 'silence' })
        db.alertTriggers.update({"_id": alertTrigger['_id']}, alertTrigger)
        total = total + 1

    print "Finish - Total changed: " + str(total)

if __name__ == '__main__':
    main()
