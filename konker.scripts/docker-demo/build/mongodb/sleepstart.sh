#!/bin/sh
MONGOISREADY=`nc 127.0.0.1 27017 </dev/null; echo $?`
echo $MONGOISREADY
while [ $MONGOISREADY != 0 ]
do
  echo "waiting for mongo..."$MONGOISREADY
  MONGOISREADY=`nc 127.0.0.1 27017 </dev/null; echo $?`
  sleep 10;
done
echo 'mongo is ready!'
