FROM alpine:edge

MAINTAINER Andre Rocha <andre@konkerlabs.com>

#java

RUN echo http://dl-4.alpinelinux.org/alpine/edge/community >> /etc/apk/repositories
RUN apk add --no-cache openjdk8-jre

#Jetty
# add our user and group first to make sure their IDs get assigned consistently, regardless of whatever dependencies get added
RUN addgroup -S jetty && adduser -D -S -H -G jetty jetty && rm -rf /etc/group- /etc/passwd- /etc/shadow-
RUN addgroup -S nginx && adduser -D -S -H -G nginx nginx && rm -rf /etc/group- /etc/passwd- /etc/shadow-

ENV JETTY_HOME /usr/local/jetty
ENV PATH $JETTY_HOME/bin:$PATH
RUN mkdir -p "$JETTY_HOME"
WORKDIR $JETTY_HOME

ENV JETTY_BASE /var/lib/jetty
RUN mkdir -p "$JETTY_BASE"

ENV JETTY_VERSION 9.3.12.v20160915
ENV JETTY_TGZ_URL https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-distribution/$JETTY_VERSION/jetty-distribution-$JETTY_VERSION.tar.gz

# GPG Keys are personal keys of Jetty committers (see https://dev.eclipse.org/mhonarc/lists/jetty-users/msg05220.html)
ENV JETTY_GPG_KEYS \
	# 1024D/8FB67BAC 2006-12-10 Joakim Erdfelt <joakime@apache.org>
	B59B67FD7904984367F931800818D9D68FB67BAC \
	# 1024D/D7C58886 2010-03-09 Jesse McConnell (signing key) <jesse.mcconnell@gmail.com>
	5DE533CB43DAF8BC3E372283E7AE839CD7C58886

RUN set -xe \
	# Install required packages for build time. Will be removed when build finishes.
	&& apk add --no-cache --virtual .build-deps gnupg coreutils curl \

	&& curl -SL "$JETTY_TGZ_URL" -o jetty.tar.gz \
	&& curl -SL "$JETTY_TGZ_URL.asc" -o jetty.tar.gz.asc \
	&& export GNUPGHOME="$(mktemp -d)" \
	&& for key in $JETTY_GPG_KEYS; do \
		gpg --keyserver ha.pool.sks-keyservers.net --recv-keys "$key"; done \
	&& gpg --batch --verify jetty.tar.gz.asc jetty.tar.gz \
	&& rm -r "$GNUPGHOME" \
	&& tar -xvzf jetty.tar.gz \
	&& mv jetty-distribution-$JETTY_VERSION/* ./ \
	&& sed -i '/jetty-logging/d' etc/jetty.conf \
	&& rm -fr demo-base javadoc \
	&& rm jetty.tar.gz* \
	&& rm -fr jetty-distribution-$JETTY_VERSION/ \

	# Get the list of modules in the default start.ini and build new base with those modules, then add setuid
	&& cd $JETTY_BASE \
	&& modules="$(grep -- ^--module= "$JETTY_HOME/start.ini" | cut -d= -f2 | paste -d, -s)" \
	&& java -jar "$JETTY_HOME/start.jar" --add-to-startd="$modules,setuid" \

	# Remove installed packages and various cleanup
	&& apk del .build-deps \
	&& rm -fr .build-deps \
	&& rm -rf /tmp/hsperfdata_root

WORKDIR $JETTY_BASE

ENV TMPDIR /tmp/jetty
RUN set -xe \
	&& mkdir -p "$TMPDIR" \
	&& chown -R jetty:jetty "$TMPDIR" "$JETTY_BASE"

COPY env/local/registry.war /var/lib/jetty/webapps/
COPY env/local/application.conf /var/lib/jetty/resources/
COPY env/local/logback.xml /var/lib/jetty/webapps/resources/


#nginx
ENV NGINX_VERSION nginx-1.7.11

RUN apk --update add openssl-dev pcre-dev zlib-dev wget build-base && \
    mkdir -p /tmp/src && \
    cd /tmp/src && \
    wget http://nginx.org/download/${NGINX_VERSION}.tar.gz && \
    tar -zxvf ${NGINX_VERSION}.tar.gz && \
    cd /tmp/src/${NGINX_VERSION} && \
    ./configure \
        --with-http_ssl_module \
        --with-http_gzip_static_module \
        --prefix=/etc/nginx \
        --http-log-path=/var/log/nginx/access.log \
        --error-log-path=/var/log/nginx/error.log \
        --sbin-path=/usr/local/sbin/nginx && \
    make && \
    make install && \
    apk del build-base && \
    rm -rf /tmp/src && \
    rm -rf /var/cache/apk/*

# forward request and error logs to docker log collector
RUN ln -sf /dev/stdout /var/log/nginx/access.log
RUN ln -sf /dev/stderr /var/log/nginx/error.log

RUN chmod 755 /usr/local/sbin/nginx

COPY env/local/nginx.conf /etc/nginx/nginx.conf
COPY env/local/nginx.conf /etc/nginx/conf/nginx.conf
COPY env/local/mime.types /etc/nginx/mime.types
COPY env/local/sites /var/www/
COPY env/local/conf.d /etc/nginx/conf.d


# Install Redis.
# add our user and group first to make sure their IDs get assigned consistently, regardless of whatever dependencies get added
RUN addgroup -S redis && adduser -S -G redis redis

# grab gosu for easy step-down from root
RUN apk add --update ca-certificates curl gnupg && \
    gpg --keyserver pool.sks-keyservers.net --recv-keys B42F6819007F00F88E364FD4036A9C25BF357DD4 && \
    ARCH=`uname -m`; if [ $ARCH == "x86_64" ]; then export ARCH="amd64"; else export ARCH="i386"; fi && \
    curl -o /usr/local/bin/gosu -SL "https://github.com/tianon/gosu/releases/download/1.6/gosu-$ARCH" && \
    curl -o /usr/local/bin/gosu.asc -SL "https://github.com/tianon/gosu/releases/download/1.6/gosu-$ARCH.asc" && \
    gpg --verify /usr/local/bin/gosu.asc && \
	rm /usr/local/bin/gosu.asc && \
	chmod +x /usr/local/bin/gosu && \
    apk del ca-certificates curl gnupg && \
    rm -rf /var/cache/apk/*

ENV REDIS_VERSION 3.0.5
ENV REDIS_DOWNLOAD_URL http://download.redis.io/releases/redis-3.0.5.tar.gz
ENV REDIS_DOWNLOAD_SHA1 ad3ee178c42bfcfd310c72bbddffbbe35db9b4a6

# for redis-sentinel see: http://redis.io/topics/sentinel
RUN apk add --update ca-certificates curl gcc libc-dev make tar linux-headers && \
    mkdir -p /usr/src/redis && \
    curl -sSL "$REDIS_DOWNLOAD_URL" -o redis.tar.gz && \
    echo "$REDIS_DOWNLOAD_SHA1 *redis.tar.gz" | sha1sum -c - && \
    tar -xzf redis.tar.gz -C /usr/src/redis --strip-components=1 && \
    rm redis.tar.gz && \
    make -C /usr/src/redis MALLOC=libc && \
    make -C /usr/src/redis install && \
    rm -r /usr/src/redis && \
    apk del ca-certificates curl gcc libc-dev make tar linux-headers


# Install MongoDB.
RUN \
echo http://dl-4.alpinelinux.org/alpine/edge/testing >> /etc/apk/repositories && \
apk add --no-cache mongodb && \
rm /usr/bin/mongosniff /usr/bin/mongoperf

# Install mosquitto
ENV DEBIAN_FRONTEND noninteractive

RUN apk add --update mosquitto mosquitto-clients && \
mkdir /work && chown nobody /work && mkdir -p /data/db

COPY env/local/plugin/konker-mosquitto-auth-plugin.conf /etc/ld.so.conf.d/konker-mosquitto-auth-plugin.conf
COPY env/local/plugin/konker-mosquitto-auth-plugin /usr/local/lib/konker-mosquitto-auth-plugin
COPY env/local/plugin/konker-mosquitto-auth-plugin.conf /etc/ld.so.conf.d/konker-mosquitto-auth-plugin.conf
COPY env/local/mosquitto.conf /etc/mosquitto/mosquitto.conf
COPY env/local/passwords.pwd /etc/mosquitto/passwords.pwd

#populate demo data
RUN apk add --update python && \
 apk add py2-pip && \
 pip install pymongo

COPY env/local/populate_demo_data.py /root/populate_demo_data.py
RUN chmod 777 ~/populate_demo_data.py

COPY docker-entrypoint.sh /
RUN chmod 777 /docker-entrypoint.sh

#start
EXPOSE 8080 80 443 6379 27017 28017 1883

VOLUME /data/db

ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["java","-jar","/usr/local/jetty/start.jar"]
