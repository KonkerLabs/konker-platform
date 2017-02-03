FROM centos:centos7

MAINTAINER Andre Rocha <andre@konkerlabs.com>

#PACKAGE SUPPORT
RUN yum install -y wget

#JAVA
RUN yum -y install java-1.8.0-openjdk-devel

#PYTHON
RUN rpm -iUvh http://dl.fedoraproject.org/pub/epel/7/x86_64/e/epel-release-7-9.noarch.rpm  && \
yum update -y && \
yum install -y python python-pip && \
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
RUN yum install -y mongodb mongodb-server && \
	mkdir -p /data/db && \
	mkdir /var/log/mongo

#REDIS
RUN yum install -y redis

#JETTY
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
	chkconfig --level 345 jetty on

COPY build/jetty/default_jetty /etc/default/jetty

#nginx
RUN yum install -y nginx

### KONKER RESOURCES

#MONGO
RUN mkdir /etc/mongo
COPY build/mongodb/mongod.conf /etc/mongo/


#ENTRYPONT
COPY docker-entrypoint.sh /
RUN chmod 777 /docker-entrypoint.sh

#mosquitto-lib
COPY build/plugin/konker-mosquitto-auth-plugin.conf /etc/ld.so.conf.d/konker-mosquitto-auth-plugin.conf
COPY build/plugin/konker-mosquitto-auth-plugin /usr/local/lib/konker-mosquitto-auth-plugin
COPY build/mosquitto.conf /etc/mosquitto/mosquitto.conf
COPY build/passwords.pwd /etc/mosquitto/passwords.pwd
COPY build/konker-mosquitt-auth-plugin.conf /etc/mosquitto/konker-mosquitto-auth-plugin.conf
COPY build/konker-mqtt.conf /etc/mosquitto/conf.d/konker-mqtt.conf

#registry
COPY build/registry.war /opt/jetty/webapps/
COPY build/application.conf /opt/jetty/resources/
COPY build/logback.xml /opt/jetty/webapps/resources/
COPY build/logback.xml /opt/jetty/resources/
COPY build/mail /opt/jetty/webapps/resources/mail/
COPY build/mail /opt/jetty/resources/mail/

#nginx
COPY build/nginx.conf /etc/nginx/nginx.conf
COPY build/nginx.conf /etc/nginx/conf/nginx.conf
COPY build/mime.types /etc/nginx/mime.types
COPY build/conf.d /etc/nginx/conf.d
COPY build/error_page/* /usr/share/nginx/html/


#DSL for Instance Administration
COPY build/__init__.py /usr/bin
COPY build/populate_users.py /usr/bin
COPY build/dsl.py /usr/bin/
COPY build/users/ /usr/bin/konkerusers/
COPY build/dao/ /usr/bin/dao/
COPY build/setup.py /usr/bin/
RUN chmod 777 /usr/bin/setup.py
RUN python /usr/bin/setup.py install
RUN rm /usr/bin/setup.py
RUN ln -s /usr/bin/dsl.py /usr/bin/konker
RUN ln -s /usr/bin/populate_users.py /usr/bin/populate_users


EXPOSE 1883 80
ENTRYPOINT ["/docker-entrypoint.sh"]
