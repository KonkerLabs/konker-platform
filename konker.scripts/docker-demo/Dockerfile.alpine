FROM alpine:edge

MAINTAINER Andre Rocha <andre@konkerlabs.com>

# General structure: start installing components that
# do not have dependencies and en up installing the web
# app. This will optimize building time, since the
# first layers, with independent components are very
# rarrely update and the layers will already be cached



######################## INSTALL BASIC COMPONENTS #######################################
## install java
RUN echo http://dl-4.alpinelinux.org/alpine/edge/community >> /etc/apk/repositories && \
    apk add --no-cache openjdk8-jre

# install python
RUN apk add --update python py2-pip && \
    pip install pymongo


#Jetty
# add our user and group first to make sure their IDs get assigned consistently, regardless of whatever dependencies get added
RUN addgroup -S jetty && adduser -D -S -H -G jetty jetty && rm -rf /etc/group- /etc/passwd- /etc/shadow-

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
	B59B67FD7904984367F931800818D9D68FB67BAC \
	5DE533CB43DAF8BC3E372283E7AE839CD7C58886

RUN set -xe \
	# Install required packages for build time. Will be removed when build finishes.
	&& apk add --no-cache --virtual .build-deps gnupg coreutils curl \

	&& curl -SL "$JETTY_TGZ_URL" -o jetty.tar.gz \
	&& curl -SL "$JETTY_TGZ_URL.asc" -o jetty.tar.gz.asc \
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


# Install MongoDB.
RUN \
echo http://dl-4.alpinelinux.org/alpine/edge/testing >> /etc/apk/repositories && \
apk add --no-cache mongodb && \
rm /usr/bin/mongosniff /usr/bin/mongoperf

# Install mosquitto

RUN apk add --update mosquitto libcrypto1.0 libssl1.0 && \
    mkdir /work && chown nobody /work


# Install nginx
ENV NGINX_VERSION nginx-1.7.11

RUN addgroup -S nginx && adduser -D -S -H -G nginx nginx && rm -rf /etc/group- /etc/passwd- /etc/shadow- && \
    apk --update add openssl-dev pcre-dev zlib-dev wget build-base && \
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


# Install redis

# grab su-exec for easy step-down from root
RUN apk add --no-cache 'su-exec>=0.2'

ENV REDIS_VERSION 3.2.6
ENV REDIS_DOWNLOAD_URL http://download.redis.io/releases/redis-3.2.6.tar.gz
ENV REDIS_DOWNLOAD_SHA1 0c7bc5c751bdbc6fabed178db9cdbdd948915d1b

# for redis-sentinel see: http://redis.io/topics/sentinel
RUN set -ex \
	\
	&& apk add --no-cache --virtual .build-deps \
		gcc \
		linux-headers \
		make \
		musl-dev \
		tar \
	\
	&& wget -O redis.tar.gz "$REDIS_DOWNLOAD_URL" \
	&& echo "$REDIS_DOWNLOAD_SHA1 *redis.tar.gz" | sha1sum -c - \
	&& mkdir -p /usr/src/redis \
	&& tar -xzf redis.tar.gz -C /usr/src/redis --strip-components=1 \
	&& rm redis.tar.gz \
	\
# Disable Redis protected mode [1] as it is unnecessary in context
# of Docker. Ports are not automatically exposed when running inside
# Docker, but rather explicitely by specifying -p / -P.
# [1] https://github.com/antirez/redis/commit/edd4d555df57dc84265fdfb4ef59a4678832f6da
	&& grep -q '^#define CONFIG_DEFAULT_PROTECTED_MODE 1$' /usr/src/redis/src/server.h \
	&& sed -ri 's!^(#define CONFIG_DEFAULT_PROTECTED_MODE) 1$!\1 0!' /usr/src/redis/src/server.h \
	&& grep -q '^#define CONFIG_DEFAULT_PROTECTED_MODE 0$' /usr/src/redis/src/server.h \
# for future reference, we modify this directly in the source instead of just supplying a default configuration flag because apparently "if you specify any argument to redis-server, [it assumes] you are going to specify everything"
# see also https://github.com/docker-library/redis/issues/4#issuecomment-50780840
# (more exactly, this makes sure the default behavior of "save on SIGTERM" stays functional by default)
	\
	&& make -C /usr/src/redis \
	&& make -C /usr/src/redis install \
	\
	&& rm -r /usr/src/redis \
	\
	&& apk del .build-deps



################### CONFIGURE COMPONENTS ###########################
## Configure nginx

COPY build/nginx.conf /etc/nginx/nginx.conf
COPY build/nginx.conf /etc/nginx/conf/nginx.conf
COPY build/mime.types /etc/nginx/mime.types
COPY build/conf.d /etc/nginx/conf.d
COPY build/error_page/* /usr/share/nginx/html/


## Configure mosquitto
RUN mkdir /var/log/mosquitto && chmod -R 777 /var/log/mosquitto && \
    mkdir /var/lib/mosquitto && chmod -R 777 /var/lib/mosquitto && \
    apk add --update mongo-c-driver
COPY build/plugin/konker-mosquitto-auth-plugin-ld.conf /etc/ld.so.conf.d/konker-mosquitto-auth-plugin-ld.conf
COPY build/plugin/lib/*.so /usr/local/lib/konker-mosquitto-auth-plugin/
COPY build/mosquitto.conf /etc/mosquitto/mosquitto.conf
COPY build/konker-mosquitto-auth-plugin.conf /etc/mosquitto/konker-mosquitto-auth-plugin.conf
COPY build/konker-mqtt.conf /etc/mosquitto/conf.d/konker-mqtt.conf
RUN  ln -s /usr/lib/libcrypto.so.1.0.0 /usr/lib/libcrypto.so.10 && \
     ln -s /usr/lib/libssl.so.1.0.0 /usr/lib/libssl.so.10


#DSL for Instance Administration
COPY build/__init__.py /usr/bin
COPY build/populate_users.py /usr/bin
COPY build/dsl.py /usr/bin
COPY build/users/ /usr/bin/users
COPY build/dao/ /usr/bin/dao
COPY build/setup.py /usr/bin
COPY build/generate_mosquitto_credentials.sh /usr/bin

RUN chmod 777 /usr/bin/setup.py
RUN chmod 777 /usr/bin/generate_mosquitto_credentials.sh
RUN python /usr/bin/setup.py install
RUN rm /usr/bin/setup.py
RUN ln -s /usr/bin/dsl.py /usr/bin/konker
RUN ln -s /usr/bin/populate_users.py /usr/bin/populate_users


## Configure and deploy web app
COPY build/registry.war /var/lib/jetty/webapps/
COPY build/application.conf /var/lib/jetty/resources/
COPY build/logback.xml /var/lib/jetty/webapps/resources/
COPY build/logback.xml /var/lib/jetty/resources/
COPY build/mail /var/lib/jetty/webapps/resources/mail/
COPY build/mail /var/lib/jetty/resources/mail/


## Set entrypoint
COPY docker-entrypoint.sh /
RUN chmod 777 /docker-entrypoint.sh

#RUN mkdir /data && chown redis:redis /data

#start
EXPOSE 8080 80 443 6379 27017 28017 1883

VOLUME /data/db

ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["java","-jar","/usr/local/jetty/start.jar"]
