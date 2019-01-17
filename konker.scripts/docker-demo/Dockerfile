FROM centos:centos7

MAINTAINER Andre Rocha <andre@konkerlabs.com>

#PACKAGE SUPPORT
RUN yum install -y wget nc

#JAVA
RUN yum -y install java-1.8.0-openjdk-devel

#PYTHON
RUN rpm -iUvh http://dl.fedoraproject.org/pub/epel/7/x86_64/Packages/e/epel-release-7-11.noarch.rpm  && \
yum update -y && \
yum install -y python python-pip && \
    pip install --upgrade pip && \
	pip install pymongo

#MOSQUITTO
COPY build/mosquitto/mqtt.repo /etc/yum.repos.d/mqtt.repo
RUN yum update -y && yum install -y \
initscripts libwrap0-dev libssl-dev python-distutils-extra libc-ares-dev uuid-dev
RUN yum install -y http://rpms.remirepo.net/enterprise/remi-release-7.rpm
RUN yum update -y
RUN mkdir /var/log/mosquitto && chmod -R 777 /var/log/mosquitto
RUN yum install -y mongo-c-driver && \
yum install -y mosquitto

#MONGODB
RUN yum install -y mongodb mongodb-server

#REDIS
RUN yum install -y redis

#JETTY
ENV JETTY_HOME /opt/jetty
ENV PATH $JETTY_HOME/bin:$PATH
ENV JETTY_BASE /var/lib/jetty
RUN mkdir -p "$JETTY_BASE"

RUN wget http://central.maven.org/maven2/org/eclipse/jetty/jetty-distribution/9.4.1.v20170120/jetty-distribution-9.4.1.v20170120.tar.gz && \
	tar zxvf jetty-distribution-9.4.1.v20170120.tar.gz -C /opt/ && \
	mv /opt/jetty-distribution-9.4.1.v20170120/ /opt/jetty && \
	useradd -m jetty && \
	mkdir /var/run/jetty && \
	mkdir /var/log/jetty && \
	chown jetty:jetty /var/log/jetty && \
	chown jetty:jetty /var/run/jetty && \
	chown -R jetty:jetty /opt/jetty/ && \
	ln -s /opt/jetty/bin/jetty.sh /etc/init.d/jetty && \
	chkconfig --add jetty && \
	chkconfig --level 345 jetty on && \
	cd $JETTY_BASE \
	&& modules="$(grep -- ^--module= "$JETTY_HOME/start.ini" | cut -d= -f2 | paste -d, -s)" \
	&& java -jar "$JETTY_HOME/start.jar" --add-to-startd="$modules,setuid"

WORKDIR $JETTY_BASE

ENV TMPDIR /tmp/jetty
RUN set -xe \
	&& mkdir -p "$TMPDIR" \
	&& chown -R jetty:jetty "$TMPDIR" "$JETTY_BASE"

#nginx
RUN yum install -y nginx

#rabbit
RUN wget https://www.rabbitmq.com/releases/rabbitmq-server/v3.6.10/rabbitmq-server-3.6.10-1.el7.noarch.rpm
RUN rpm --import https://www.rabbitmq.com/rabbitmq-release-signing-key.asc
RUN yum install -y rabbitmq-server-3.6.10-1.el7.noarch.rpm

### KONKER RESOURCES

#MONGO
RUN mkdir /etc/mongo

COPY build/mongodb/mongod.conf /etc/default/
COPY build/mongodb/sleepstart.sh /etc/mongo/
COPY build/rabbitmq/sleepstart.sh /etc/rabbitmq/
RUN chmod +x /etc/mongo/sleepstart.sh && \
	chmod +x /etc/rabbitmq/sleepstart.sh

#ENTRYPONT
COPY docker-entrypoint.sh /
RUN chmod 777 /docker-entrypoint.sh

#mosquitto-lib
COPY build/mosquitto/konker-mosquitto-auth-plugin-ld.conf /etc/ld.so.conf.d/konker-mosquitto-auth-plugin-ld.conf
COPY build/mosquitto/lib/*.so /usr/local/lib/konker-mosquitto-auth-plugin/
COPY build/mosquitto/mosquitto.conf /etc/mosquitto/mosquitto.conf
COPY build/mosquitto/konker-mosquitto-auth-plugin.conf /etc/mosquitto/konker-mosquitto-auth-plugin.conf
COPY build/mosquitto/konker-mqtt.conf /etc/mosquitto/conf.d/konker-mqtt.conf
RUN  ln -s /usr/lib/libcrypto.so.1.0.0 /usr/lib/libcrypto.so.10 && \
     ln -s /usr/lib/libssl.so.1.0.0 /usr/lib/libssl.so.10
COPY build/mosquitto/generate_mosquitto_credentials.sh /usr/bin
RUN chmod +x /usr/bin/generate_mosquitto_credentials.sh

#registry web
COPY build/jetty/registry.war /var/lib/jetty/webapps/
COPY build/jetty/application.conf /var/lib/jetty/resources/
COPY build/jetty/logback.xml /var/lib/jetty/webapps/resources/
COPY build/jetty/logback.xml /var/lib/jetty/resources/
COPY build/jetty/mail /var/lib/jetty/webapps/resources/mail/
COPY build/jetty/mail /var/lib/jetty/resources/mail/

#konkers modules
RUN  mkdir /var/lib/konker
RUN  mkdir /var/log/konker
COPY build/konker/registry-api.jar /var/lib/konker/
COPY build/konker/registry-data.jar /var/lib/konker/
COPY build/konker/registry-router.jar /var/lib/konker/
COPY build/konker/mosquitto-rabbitmq-bridge.jar /var/lib/konker/

#usage statistics feature
RUN pip install requests
COPY build/konker/usage-statistics.py /var/lib/konker/

#nginx
COPY build/nginx/nginx.conf /etc/nginx/nginx.conf
COPY build/nginx/nginx.conf /etc/nginx/conf/nginx.conf
COPY build/nginx/mime.types /etc/nginx/mime.types
COPY build/nginx/conf.d /etc/nginx/conf.d
COPY build/nginx/error_page/* /usr/share/nginx/html/


#DSL for Instance Administration
COPY build/dsl/__init__.py /usr/bin
COPY build/dsl/populate_users.py /usr/bin
COPY build/dsl/dsl.py /usr/bin/
COPY build/dsl/userskonker/ /usr/bin/userskonker/
COPY build/dsl/dao/ /usr/bin/dao/
COPY build/dsl/setup.py /usr/bin/
COPY build/dsl/update_database.py /usr/bin/

RUN chmod 777 /usr/bin/setup.py
RUN chmod 777 /usr/bin/update_database.py
RUN python /usr/bin/setup.py install
RUN rm /usr/bin/setup.py
RUN ln -s /usr/bin/dsl.py /usr/bin/konker
RUN ln -s /usr/bin/populate_users.py /usr/bin/populate_users

#clean
RUN rm -rf /var/cache/yum/*
RUN rm -f jetty-distribution-9.4.1.v20170120.tar.gz

EXPOSE 80 1883

VOLUME /data/db

ENTRYPOINT ["/docker-entrypoint.sh"]
