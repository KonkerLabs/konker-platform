#!/bin/sh

set -e

## Force default ports
REDIS_PORT=6379
CASSANDRA_PORT=9042
MONGODB_PORT=27017
MONGODB_AUDIT_PORT=27017
MONGODB_PRIVATE_STORAGE_PORT=27017
PUB_SERVER_HTTP_PORT=80
PUB_SERVER_HTTPS_PORT=443
PUB_SERVER_MQTT_PORT=1883
PUB_SERVER_MQTTS_PORT=8883
PUB_SERVER_SSL_ENABLED=true
SMS_ENABLED=true
REQUEST_MAXSIZE=99900000
INTEGRATION_TIMEOUT_DEFAULT=3000
INTEGRATION_TIMEOUT_ENRICHMENT=3000
INTEGRATION_TIMEOUT_SMS=3000
EMAIL_PORT=587
AMAZON_KINESIS_ENABLED=true
EMAIL_ENABLED=true

echo ""
echo ""
echo "################################### Konker Open Platform - API ###################################"
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
echo "## MongoDB Private Storage"
echo "#### host: $MONGODB_PRIVATE_STORAGE_HOSTNAME"
echo "#### port: $MONGODB_PRIVATE_STORAGE_PORT"
echo "####user: ******"
echo "#### password: *****"
echo "## EventStorage: $EVENT_STORAGE_BEAN"
echo "## Cassandra"
echo "#### clustername: $CASSANDRA_CLUSTERNAME"
echo "#### host: $CASSANDRA_HOSTNAME"
echo "#### port: $CASSANDRA_PORT"
echo "#### keyspace: $CASSANDRA_KEYSPACE"
echo "#### username: *****"
echo "#### password: *****"
echo "## Redis"
echo "#### host: $REDIS_HOSTNAME"
echo "#### port: $REDIS_PORT"
echo "## PUB Server"
echo "### http hostname: $PUB_SERVER_HOSTNAME"
echo "### http port: $PUB_SERVER_HTTP_PORT"
echo "### https port: $PUB_SERVER_HTTPS_PORT"
echo "### mqtt hostname: $PUB_SERVER_MQTT_HOSTNAME"
echo "### mqtt port: $PUB_SERVER_MQTT_PORT"
echo "### mqtts port: $PUB_SERVER_MQTTS_PORT"
echo "### http ctx: $PUB_SERVER_HTTP_CTX"
echo "### ssl enabled: $PUB_SERVER_SSL_ENABLED"
echo "## Rabbit MQ"
echo "#### host: $RABBITMQ_HOSTNAME"
echo "#### vhost: $RABBITMQ_VHOST"
echo "#### user: *******"
echo "#### password: *******"
echo "## Integration"
echo "#### timeout default: $INTEGRATION_TIMEOUT_DEFAULT"
echo "#### timeout enrichment: $INTEGRATION_TIMEOUT_ENRICHMENT"
echo "#### timeout sms: $INTEGRATION_TIMEOUT_SMS"
echo "## Email"
echo "#### host: $EMAIL_HOST"
echo "#### port: $EMAIL_PORT"
echo "#### sender: $EMAIL_SENDER"
echo "#### protocol: $EMAIL_PROTOCOL"
echo "#### username: $EMAIL_USERNAME"
echo "#### password: $EMAIL_PASSWORD"
echo "#### base url: $EMAIL_BASE_URL"
echo "#### enabled: $EMAIL_ENABLED"
echo "## Amazon Kinesis"
echo "#### enabled: $AMAZON_KINESIS_ENABLED"
echo "## Swagger"
echo "#### hostname: $SWAGGER_HOSTNAME"
echo "#### protocol: $SWAGGER_PROTOCOL"
echo "## Pub Server Internal"
echo "#### url: $PUB_SERVER_INTERNAL_URL"

/filebeat/filebeat -e -c /filebeat/filebeat.yml &
/usr/local/sbin/nginx &
java -Dconfig.file=/var/lib/konker/application.conf \
    -Dmongo.hostname=$MONGODB_HOSTNAME \
    -Dmongo.port=$MONGODB_PORT \
    -Dmongo.username=$MONGODB_USERNAME \
    -Dmongo.password=$MONGODB_PASSWORD \
    -DmongoAudit.hostname=$MONGODB_AUDIT_HOSTNAME \
    -DmongoAudit.port=$MONGODB_AUDIT_PORT \
    -DmongoAudit.username=$MONGODB_AUDIT_USERNAME \
    -DmongoAudit.password=$MONGODB_AUDIT_PASSWORD \
    -DmongoPrivateStorage.hostname=$MONGODB_PRIVATE_STORAGE_HOSTNAME \
    -DmongoPrivateStorage.port=$MONGODB_PRIVATE_STORAGE_PORT \
    -DmongoPrivateStorage.username=$MONGODB_PRIVATE_STORAGE_USERNAME \
    -DmongoPrivateStorage.password=$MONGODB_PRIVATE_STORAGE_PASSWORD \
    -Deventstorage.bean=$EVENT_STORAGE_BEAN \
    -Dcassandra.clustername=$CASSANDRA_CLUSTERNAME \
    -Dcassandra.keyspace=$CASSANDRA_KEYSPACE \
    -Dcassandra.hostname=$CASSANDRA_HOSTNAME \
    -Dcassandra.port=$CASSANDRA_PORT \
    -Dcassandra.username=$CASSANDRA_USERNAME \
    -Dcassandra.password=$CASSANDRA_PASSWORD \
    -Dredis.master.host=$REDIS_HOSTNAME \
    -Dredis.master.port=$REDIS_PORT \
    -Drabbitmq.hostname=$RABBITMQ_HOSTNAME \
    -Drabbitmq.username=$RABBITMQ_USERNAME \
    -Drabbitmq.password=$RABBITMQ_PASSWORD \
    -Drabbitmq.virtualHost=$RABBITMQ_VHOST \
    -DpubServer.httpHostname=$PUB_SERVER_HOSTNAME \
    -DpubServer.httpPort=$PUB_SERVER_HTTP_PORT \
    -DpubServer.httpsPort=$PUB_SERVER_HTTPS_PORT \
    -DpubServer.mqttHostName=$PUB_SERVER_MQTT_HOSTNAME \
    -DpubServer.mqttPort=$PUB_SERVER_MQTT_PORT \
    -DpubServer.mqttTlsPort=$PUB_SERVER_MQTTS_PORT \
    -DpubServer.httpCtx=$PUB_SERVER_HTTP_CTX \
    -DpubServer.sslEnabled=$PUB_SERVER_SSL_ENABLED \
    -Demail.host=$EMAIL_HOST \
    -Demail.port=$EMAIL_PORT \
    -Demail.sender=$EMAIL_SENDER \
    -Demail.protocol=$EMAIL_PROTOCOL \
    -Demail.username=$EMAIL_USERNAME \
    -Demail.password=$EMAIL_PASSWORD \
    -Demail.baseurl=$EMAIL_BASE_URL \
    -Demail.enabled=$EMAIL_ENABLED \
    -Damazon.kinesisRouteEnabled=$AMAZON_KINESIS_ENABLED \
    -Dswagger.hostname=$SWAGGER_HOSTNAME \
    -Dswagger.protocol=$SWAGGER_PROTOCOL \
    -DpubServerInternal.url=$PUB_SERVER_INTERNAL_URL \
    -Dspringfox.documentation.swagger.v2.host=$SWAGGER_HOSTNAME:443 \
    -jar /var/lib/konker/registry-api.jar
exec "$@"