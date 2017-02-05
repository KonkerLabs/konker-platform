#! /bin/sh
konker credentials > /tmp/mqtt_sub_credentials.txt
USR_SUB=`cat /tmp/mqtt_sub_credentials.txt | grep "user:"  | awk -F: '{print $2}'`
PWD_SUB=`cat /tmp/mqtt_sub_credentials.txt | grep "password:"  | awk -F: '{print $2}'`
HSH_SUB=`cat /tmp/mqtt_sub_credentials.txt | grep "hash:"  | awk -F: '{print $2}'`


konker credentials > /tmp/mqtt_pub_credentials.txt
USR_PUB=`cat /tmp/mqtt_pub_credentials.txt | grep "user:"  | awk -F: '{print $2}'`
PWD_PUB=`cat /tmp/mqtt_pub_credentials.txt | grep "password:"  | awk -F: '{print $2}'`
HSH_PUB=`cat /tmp/mqtt_pub_credentials.txt | grep "hash:"  | awk -F: '{print $2}'`



cat >> /var/lib/jetty/resources/application.conf << EOF


mqtt {
  subcribe {
    uris = [ "tcp://127.0.0.1:1883" ]
    # yes, we subscribe to a topic named pub. "pub" is from the device perspective
    topics = [ "pub/+/+" ]
    username = "${USR_SUB}"
    password = "${PWD_SUB}"
  }
  publish {
    uris = [ "tcp://127.0.0.1:1883" ]
    username = "${USR_PUB}"
    password = "${PWD_PUB}"
  }
}


EOF


cat >> /etc/mosquitto/konker-mosquitto-auth-plugin.conf << EOF
#username for the superuser
superuser.sub.username ${USR_SUB}
superuser.sub.secret ${HSH_SUB}
superuser.pub.username ${USR_PUB}
superuser.pub.secret ${HSH_PUB}
EOF
