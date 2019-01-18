#!/bin/sh
MAXRETRY=10;
RETRYCOUNT=0;
ISRABBITREADY=`nc 127.0.0.1 25672 </dev/null; echo $?`;

while [ $ISRABBITREADY != 0 ]
do
  if [ "$ISRABBITREADY" -lt "$MAXRETRY" ]; then
    ISRABBITREADY=`nc 127.0.0.1 25672 </dev/null; echo $?`;
    if [ $ISRABBITREADY == 0 ]; then
      echo 'Konker Message Queue is ready!';
    else
      echo "Waiting for konker message queue..."$RETRYCOUNT" of "$MAXRETRY;
      sleep 10;
      RETRYCOUNT=$(($RETRYCOUNT+1));
    fi
  else
    sleep 5;
    ISRABBITREADY=0;
    echo "Max tries("$MAXRETRY") was reached, Konker message queue is unstable..stop container...";
    halt;
  fi
done
