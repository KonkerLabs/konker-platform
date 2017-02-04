#!/bin/sh
MAXRETRY=10;
RETRYCOUNT=0;
MONGOISREADY=`nc 127.0.0.1 27017 </dev/null; echo $?`;

while [ $MONGOISREADY != 0 ]
do
  if [ "$RETRYCOUNT" -lt "$MAXRETRY" ]; then
    MONGOISREADY=`nc 127.0.0.1 27017 </dev/null; echo $?`;
    if [ $MONGOISREADY == 0 ]; then
      echo 'Konker Database is ready!';
    else
      echo "Waiting for konker database..."$RETRYCOUNT" of "$MAXRETRY;
      sleep 10;
      RETRYCOUNT=$(($RETRYCOUNT+1));
    fi
  else
    sleep 5;
    MONGOISREADY=0;
    echo "Max tries("$MAXRETRY") was reached, Konker database is unstable..stop container...";
    halt;
  fi
done
