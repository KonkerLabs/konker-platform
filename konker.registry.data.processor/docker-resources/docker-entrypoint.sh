#!/bin/sh

set -e

## Force default ports
REDIS_PORT=6379
CASSANDRA_PORT=9042
MONGODB_PORT=27017
MONGODB_AUDIT_PORT=27017
PUB_SERVER_HTTP_PORT=80
PUB_SERVER_HTTPS_PORT=443
PUB_SERVER_MQTT_PORT=1883
PUB_SERVER_MQTTS_PORT=8883
PUB_SERVER_SSL_ENABLED=true
SMS_ENABLED=true
REQUEST_MAXSIZE=99900000
HOTJAR_ID=307642
HOTJAR_ENABLE=false
INTEGRATION_TIMEOUT_DEFAULT=3000
INTEGRATION_TIMEOUT_ENRICHMENT=3000
INTEGRATION_TIMEOUT_SMS=3000
ANALYTICS_ENABLED=false
EMAIL_PORT=587
AMAZON_KINESIS_ENABLED=true
CDN_MAX_SIZE=500000
CDN_ENABLED=true
EMAIL_ENABLED=true
S3BUCKET_MAX_SIZE=500000
S3BUCKET_ENABLED=true
RABBITMQ_API_PORT=15672


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
echo "## SMS"
echo "#### enabled: $SMS_ENABLED"
echo "#### uri: $SMS_URI"
echo "#### username: *******"
echo "#### password: *******"
echo "#### from: $SMS_FROM"
echo "## Security"
echo "#### login page: $SECURITY_LOGIN_PAGE"
echo "#### success login url: $SECURITY_SUCCESS_LOGIN_URL"
echo "## Hotjar"
echo "#### id: $HOTJAR_ID"
echo "#### enable: $HOTJAR_ENABLE"
echo "## Integration"
echo "#### timeout default: $INTEGRATION_TIMEOUT_DEFAULT"
echo "#### timeout enrichment: $INTEGRATION_TIMEOUT_ENRICHMENT"
echo "#### timeout sms: $INTEGRATION_TIMEOUT_SMS"
echo "## Analytics"
echo "#### enabled: $ANALYTICS_ENABLED"
echo "## CDN"
echo "#### name: $CDN_NAME"
echo "#### prefix: $CDN_PREFIX"
echo "#### key: *******"
echo "#### secret: *******"
echo "#### max size: $CDN_MAX_SIZE"
echo "#### file types: $CDN_FILE_TYPES"
echo "#### enabled: $CDN_ENABLED"
echo "#### default avatar: $CDN_DEFAULT_AVATAR"
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
echo "## S3 Bucket"
echo "#### name: $S3BUCKET_NAME"
echo "#### prefix: $S3BUCKET_PREFIX"
echo "#### key: *******"
echo "#### secret: *******"
echo "#### max size: $S3BUCKET_MAX_SIZE"
echo "#### file types: $S3BUCKET_FILE_TYPES"
echo "#### enabled: $S3BUCKET_ENABLED"


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
    -Drabbitmq.apihost=$RABBITMQ_API_HOST \
    -Drabbitmq.apiport=$RABBITMQ_API_PORT \
    -Drabbitmq.username=$RABBITMQ_USERNAME \
    -Drabbitmq.password=$RABBITMQ_PASSWORD \
    -Drabbitmq.virtualHost=$RABBITMQ_VHOST \
    -Dsms.enabled=$SMS_ENABLED \
    -Dsms.uri=$SMS_URI \
    -Dsms.username=$SMS_USERNAME \
    -Dsms.password=$SMS_PASSWORD \
    -Dsms.from=$SMS_FROM \
    -DpubServer.httpHostname=$PUB_SERVER_HOSTNAME \
    -DpubServer.httpPort=$PUB_SERVER_HTTP_PORT \
    -DpubServer.httpsPort=$PUB_SERVER_HTTPS_PORT \
    -DpubServer.mqttHostName=$PUB_SERVER_MQTT_HOSTNAME \
    -DpubServer.mqttPort=$PUB_SERVER_MQTT_PORT \
    -DpubServer.mqttTlsPort=$PUB_SERVER_MQTTS_PORT \
    -DpubServer.httpCtx=$PUB_SERVER_HTTP_CTX \
    -DpubServer.sslEnabled=$PUB_SERVER_SSL_ENABLED \
    -Dsecurity.loginPage=$SECURITY_LOGIN_PAGE \
    -Dsecurity.successLoginUrl=$SECURITY_SUCCESS_LOGIN_URL \
    -Dhotjar.id=$HOTJAR_ID \
    -Dhotjar.enable=$HOTJAR_ENABLE \
    -DkonkerAnalytics.enabled=$ANALYTICS_ENABLED \
    -Dcdn.name=$CDN_NAME \
    -Dcdn.prefix=$CDN_PREFIX \
    -Dcdn.key=$CDN_KEY \
    -Dcdn.secret=$CDN_SECRET \
    -Dcdn.max-size=$CDN_MAX_SIZE \
    -Dcdn.file-types=$CDN_FILE_TYPES \
    -Dcdn.enabled=$CDN_ENABLED \
    -Dcdn.defaultavatar=$CDN_DEFAULT_AVATAR \
    -Demail.host=$EMAIL_HOST \
    -Demail.port=$EMAIL_PORT \
    -Demail.sender=$EMAIL_SENDER \
    -Demail.protocol=$EMAIL_PROTOCOL \
    -Demail.username=$EMAIL_USERNAME \
    -Demail.password=$EMAIL_PASSWORD \
    -Demail.baseurl=$EMAIL_BASE_URL \
    -Demail.enabled=$EMAIL_ENABLED \
    -Damazon.kinesisRouteEnabled=$AMAZON_KINESIS_ENABLED \
    -Ds3bucket.name=$S3BUCKET_NAME \
    -Ds3bucket.prefix=$S3BUCKET_PREFIX \
    -Ds3bucket.key=$S3BUCKET_KEY \
    -Ds3bucket.secret=$S3BUCKET_SECRET \
    -Ds3bucket.max-size=$S3BUCKET_MAX_SIZE \
    -Ds3bucket.file-types=$S3BUCKET_FILE_TYPES \
    -Ds3bucket.enabled=$S3BUCKET_ENABLED \
    -jar /var/lib/konker/registry-data-processor.jar
exec "$@"