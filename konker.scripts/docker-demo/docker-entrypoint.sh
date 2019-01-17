#!/bin/sh

set -e

if [ "$1" = jetty.sh ]; then
	if ! command -v bash >/dev/null 2>&1 ; then
		cat >&2 <<- 'EOWARN'
			********************************************************************
			ERROR: bash not found. Use of jetty.sh requires bash.
			********************************************************************
		EOWARN
		exit 1
	fi
	cat >&2 <<- 'EOWARN'
		********************************************************************
		WARNING: Use of jetty.sh from this image is deprecated and may
			 be removed at some point in the future.

			 See the documentation for guidance on extending this image:
			 https://github.com/docker-library/docs/tree/master/jetty
		********************************************************************
	EOWARN
fi

if ! command -v -- "$1" >/dev/null 2>&1 ; then
	set -- java -jar "$JETTY_HOME/start.jar" "$@"
fi

if [ -n "$TMPDIR" ] ; then
	case "$JAVA_OPTIONS" in
		*-Djava.io.tmpdir=*) ;;
		*) JAVA_OPTIONS="-Djava.io.tmpdir=$TMPDIR $JAVA_OPTIONS" ;;
	esac
fi

if [ "$1" = "java" -a -n "$JAVA_OPTIONS" ] ; then
	shift
	set -- java $JAVA_OPTIONS "$@"
fi

echo ""
echo ""
echo "##################################### Konker Open Platform #######################################"
echo "##                                       Version: 0.9.1                                         ##"
echo "##                                  Release date: 2019-01-17                                    ##"
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

echo "adding host"
echo "127.0.0.1  dev-server" >> /etc/hosts

echo "securing konker mqtt service..."
generate_mosquitto_credentials.sh

echo "starting and recovering konker database..."
mongod -f /etc/default/mongod.conf &

/etc/mongo/sleepstart.sh

#Set database version
/usr/bin/update_database.py

#Set default user
echo "populating konker database..."
populate_users

echo "starting konker mqtt service..."
mosquitto -c /etc/mosquitto/mosquitto.conf &

echo "starting RabbitMQ..."
/usr/sbin/rabbitmq-server &
service rabbitmq-server status &

/etc/rabbitmq/sleepstart.sh

echo "starting konker registry data ingestion..."
java -Dconfig.file=/var/lib/jetty/resources/application.conf -jar /var/lib/konker/registry-data.jar --server.port=9090 &

echo "starting konker registry router..."
java -Dconfig.file=/var/lib/jetty/resources/application.conf -jar /var/lib/konker/registry-router.jar --server.port=9091 &

echo "starting konker registry api..."
java -Dconfig.file=/var/lib/jetty/resources/application.conf -jar /var/lib/konker/registry-api.jar --server.port=8080 &

echo "starting konker mqtt rabbit bridge..."
java -Dconfig.file=/var/lib/jetty/resources/application.conf -jar /var/lib/konker/mosquitto-rabbitmq-bridge.jar | tee /var/log/konker/mosquitto-rabbitmq-bridge.log &

#Usage statistics feature
/var/lib/konker/usage-statistics.py &

echo "starting konker registry app..."
redis-server &
/usr/sbin/nginx &
exec "$@"
