FROM openjdk:8-jre-alpine

MAINTAINER Andre Rocha <andre@konkerlabs.com>

#ENV TZ=America/Sao_Paulo
#RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
#RUN echo America/Sao_Paulo | tee /etc/timezone && dpkg-reconfigure --frontend noninteractive tzdata
#ENV TERM=xterm \
#ENV TZ=America/Sao_Paulo
#RUN apk --update add tzdata && cp /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
#apk del tzdata

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

COPY docker-entrypoint.sh /
RUN chmod 777 /docker-entrypoint.sh

COPY build/registry.war /var/lib/jetty/webapps/
COPY build/application.conf /var/lib/jetty/resources/
COPY build/logback.xml /var/lib/jetty/webapps/resources/
COPY build/logback.xml /var/lib/jetty/resources/
#COPY build/resolv.conf /etc/
COPY build/mail /var/lib/jetty/webapps/resources/mail/
COPY build/mail /var/lib/jetty/resources/mail/

#nginx
ENV NGINX_VERSION nginx-1.10.3

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

#VOLUME ["/var/log/nginx"]
#WORKDIR /etc/nginx
RUN chmod 755 /usr/local/sbin/nginx

COPY build/nginx.conf /etc/nginx/nginx.conf
COPY build/nginx.conf /etc/nginx/conf/nginx.conf
COPY build/mime.types /etc/nginx/mime.types
#COPY build/sites /var/www/
COPY build/conf.d /etc/nginx/conf.d

#start
EXPOSE 8080 80 443
ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["java","-jar","/usr/local/jetty/start.jar","-Dorg.eclipse.jetty.server.Request.maxFormContentSize=99900000"]
