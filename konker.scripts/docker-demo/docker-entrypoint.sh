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
echo "################################ Konker Open Platform ############################################"
echo "##                               Version: 0.2.0-RC2                                             ##"
echo "##                            Release date: 2017-01-03                                          ##"
echo "##          Licence: Apache V2 (http://www.apache.org/licenses/LICENSE-2.0)                     ##"
echo "##                         Need Support?: support@konkerlabs.com                                ##"
echo "##################################################################################################"
echo ""
echo ""

echo "securing mosquitto..."
generate_mosquitto_credentials.sh

echo "starting mongo..."
mongod -f /etc/default/mongod.conf &

/etc/mongo/sleepstart.sh

echo "populating database..."
#Set database version
konker database upgrade &
#Set default user
populate_users &

echo "starting mosquitto..."
mosquitto -c /etc/mosquitto/mosquitto.conf &

echo "starting registry app..."
redis-server &
/usr/sbin/nginx &
exec "$@"
