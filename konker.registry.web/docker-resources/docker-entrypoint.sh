#!/bin/sh

set -e

## Force default ports
REDIS_PORT=6379
CASSANDRA_PORT=9042
MONGODB_PORT=27017
MONGODB_AUDIT_PORT=27017
PUB_SERVER_HTTP_PORT=80
PUB_SERVER_HTTPS_PORT=443
PUB_SERVER_SSL_ENABLED=true
SMS_ENABLED=true
REQUEST_MAXSIZE=99900000

echo ""
echo ""
echo "################################### Konker Open Platform - WEB ###################################"
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
echo "## Redis"
echo "#### host: $REDIS_HOSTNAME"
echo "#### port: $REDIS_PORT"

/usr/local/sbin/nginx &
java \ 
    -Dorg.eclipse.jetty.server.Request.maxFormContentSize=$REQUEST_MAXSIZE \
    -Dconfig.file=/var/lib/jetty/resources/application.conf \
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
    -jar /usr/local/jetty/start.jar
exec "$@"