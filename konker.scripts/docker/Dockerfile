from jetty:9.3.11-jre8-alpine

MAINTAINER Andre Rocha <andre@konkerlabs.com>

COPY build/registry.war /var/lib/jetty/webapps/
COPY build/application.conf /var/lib/jetty/resources/
COPY build/logback.xml /var/lib/jetty/webapps/resources/
COPY build/resolv.conf /etc/
