#!/bin/sh

set -e

## Force default ports
CASSANDRA_PORT=9042
MONGODB_PORT=27017
MONGODB_AUDIT_PORT=27017
MONGODB_PRIVATE_STORAGE_PORT=27017

echo ""
echo ""
echo "################################### Konker Open Platform - CQL ETL ###############################"
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
echo "#### Datacenter: $CASSANDRA_DATACENTER"
echo "#### clustername: $CASSANDRA_CLUSTERNAME"
echo "#### host: $CASSANDRA_HOSTNAME"
echo "#### port: $CASSANDRA_PORT"
echo "#### keyspace: $CASSANDRA_KEYSPACE"
echo "#### username: *****"
echo "#### password: *****"

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
    -Dcassandra.datacenter=$CASSANDRA_DATACENTER \
    -DtimestampStart=$TIMESTAMP_START \
    -DtimestampEnd=$TIMESTAMP_END \
    -Dtenant=$TENANT \
    -Dapplication=$APPLICATION \
    -Ddevice=$DEVICE \
    -Dlocation=$LOCATION \
    -Dchannel=$CHANNEL \
    -jar /var/lib/konker/cassandra-etl.jar --equilizeCassandra
exec "$@"