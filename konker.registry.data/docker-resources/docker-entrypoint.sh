#!/bin/sh

set -e

echo ""
echo ""
echo "################################### Konker Open Platform - DATA ###################################"
echo "##                                       Version: 1.0.0                                         ##"
echo "##                                  Release date: 2017-04-24                                    ##"
echo "##              Licence: Apache V2 (http://www.apache.org/licenses/LICENSE-2.0)                 ##"
echo "##                           Need Support?: support@konkerlabs.com                              ##"
echo "##################################################################################################"
echo ""
echo ""
echo "hhhhhhhhhhhhhhhhhhhhhyyyyyys/' "
echo "hhhhhhhhhhhhhhhhhhhyyyyyys+.  "
echo "hhhhhhhhhhhhhhhhhhyyyyyyo-   "
echo "hhhhhhhhhhhhhhhhyyyyyys:'    +hy                         sho"
echo "hhhhhhhhhhhhhhyyyyyys+'      +hy '/o+'./oooo/. .oo:+os+- sho '+o/'-+ooo+- -o/:os"
echo "hhhhhhhhhhhhhyyyyyyo-        +hy.sh+'-yh/..:yh/-hho..+hh'sho-yh/'/hs---yh::hho:-"
echo "hhhhhhhhhhhhyyyyyyo'         +hyyh+  ohy    ohy-hh.  -hh.shyhh/  yhsoooss+:hh'"
echo "hhhhhhhhhdddhyyyyyhs-        +hy.sho.-yh/..:hh/-hh'  -hh.sho-yh+./hy-..-:':hh"
echo "hhhhhhhdddddddhyhhhhy+.      :o+  :o+-./oooo/. .oo'  .oo'/o: '/o+.-+oooo+ -oo"
echo "hhhhhdddddddddddhhhhhhy/'"
echo "hhhhddddddddddddddhhhhhhs-"
echo "hhdddddddddddddddddhhhhhhyo."
echo "dddddddddddddddddddddhhhhhhy/'                                                  "
echo ""
echo ""
echo "Loaded env parameters:"
echo "###################################################################################################"
echo "## MongoDB"
echo "#### host: $MONGODB_HOSTNAME"
echo "#### port: $MONGODB_PORT"
echo "#### user: ******"
echo "#### password: *****"
echo "## MongoDB Autdit"
echo "#### host: $MONGODB_AUDIT_HOSTNAME"
echo "#### port: $MONGODB_AUDIT_PORT"
echo "####user: ******"
echo "#### password: *****"
echo "## EventStorage: $EVENT_STORAGE"
echo "## Cassandra"
echo "#### clustername: $CASSANDRA_CLUSTERNAME"
echo "#### host: $CASSANDRA_HOSTNAME"
echo "#### port: $CASSANDRA_PORT"
echo "#### keyspace: $CASSANDRA_KEYSPACE"
echo "#### username: *****"
echo "#### password: *****"
echo "#### host: $REDIS_HOSTNAME"
echo "#### port: $REDIS_PORT"
echo "## Redis"
echo "#### host: $REDIS_HOSTNAME"
echo "#### port: $REDIS_PORT"
echo "#### password: ******"
echo "## Rabbit MQ"
echo "#### host: $RABBITMQ_HOSTNAME"
echo "#### vhost: $RABBITMQ_VHOST"
echo "#### user: *******"
echo "#### password: *******"
echo "## SMS"
echo "#### enabled: $SMS_ENABLED"
echo "#### uri: $SMS_URI"
echo "#### username: *******"
echo "#### password: *******"
echo "#### from: $SMS_FROM"

/filebeat/filebeat -e -c /filebeat/filebeat.yml &
/usr/local/sbin/nginx &

java \ 
    -Dconfig.file=/var/lib/konker/application.conf \
    -Dmongo.hostname=$MONGODB_HOSTNAME \
    -Dmongo.port=$MONGODB_PORT \
    -Dmongo.username=$MONGODB_USERNAME \
    -Dmongo.password=$MONGODB_PASSWORD \
    -DmongoAudit.hostname=$MONGODB_AUDIT_HOSTNAME \
    -DmongoAudit.port=$MONGODB_AUDIT_PORT \
    -DmongoAudit.username=$MONGODB_AUDIR_USERNAME \
    -DmongoAudit.password=$MONGODB_AUDIT_PASSWORD \
    -Deventstorage.bean=$EVENT_STORAGE \
    -Dcassandra.clustername=$CASSANDRA_CLUSTERNAME \
    -Dcassandra.keyspace=$CASSANDRA_KEYSPACE \
    -Dcassandra.hostname=$CASSANDRA_HOSTNAME \
    -Dcassandra.port=$CASSANDRA_PORT \
    -Dcassandra.username=$CASSANDRA_USERNAME \
    -Dcassandra.password=$CASSANDRA_PASSWORD \
    -Dredis.master.host=$REDIS_HOSTNAME \
    -Dredis.master.port=$REDIS_PORT \
    -Dredis.master.password=$REDIS_PASSWORD \
    -Drabbitmq.hostname=$RABBITMQ_HOSTNAME \
    -Drabbitmq.username=$RABBITMQ_USERNAME \
    -Drabbitmq.password=$RABBITMQ_PASSWORD \
    -Drabbitmq.virtualHost=$RABBITMQ_VHOST \
    -Dsms.username=$SMS_ENABLED \
    -Dsms.uri=$SMS_URI \
    -Dsms.username=$SMS_USERNAME \
    -Dsms.password=$SMS_PASSWORD \
    -Dsms.from=$SMS_FROM \
    -DpubServer.httpHostname=$PUB_SERVER_HTTP_HOSTNAME \
    -DpubServer.httpPort=$PUB_SERVER_HTTP_PORT \
    -DpubServer.httpsPort=$PUB_SERVER_HTTPS_PORT \
    -DpubServer.mqttHostName=$PUB_SERVER_MQTT_HOSTNAME \
    -DpubServer.mqttPort=$PUB_SERVER_MQTT_PORT \
    -DpubServer.mqttTlsPort=$PUB_SERVER_MQTT_TLS_PORT \
    -DpubServer.httpCtx=$PUB_SERVER_HTTP_CTX \
    -DpubServer.sslEnabled=$PUB_SERVER_SSL_ENABLED \
    -jar /var/lib/konker/registry-data.jar

exec "$@"