FROM python:2.7

MAINTAINER Andre Rocha <andre@konkerlabs.com>

# #PACKAGE SUPPORT
# RUN yum install -y wget nc

# #JAVA
# RUN yum -y install java-1.8.0-openjdk-devel

#PYTHON
#RUN rpm -iUvh http://dl.fedoraproject.org/pub/epel/7/x86_64/Packages/e/epel-release-7-11.noarch.rpm  && \
#RUN yum update -y && \
#yum install -y python python-pip && \
RUN  pip install --upgrade pip && \
        pip install pymongo


#ENTRYPONT
COPY docker-entrypoint.sh /
RUN chmod 777 /docker-entrypoint.sh


#DSL for Instance Administration
COPY build/dsl/__init__.py /usr/bin/__init__.py
COPY build/dsl/populate_users.py /usr/bin/populate_users.py
COPY build/dsl/dsl.py /usr/bin/dsl.py
COPY build/dsl/userskonker/ /usr/bin/userskonker/
COPY build/dsl/dao/ /usr/bin/dao/
COPY build/dsl/setup.py /usr/bin/setup.py
COPY build/dsl/update_database.py /usr/bin/update_database.py
COPY build/dsl/populate_demo_data_roles.py /usr/bin/populate_demo_data_roles.py

# USERS TO ADD
COPY users.txt /


RUN chmod 777 /usr/bin/setup.py
RUN chmod 777 /usr/bin/update_database.py
RUN chmod 777 /usr/bin/populate_users.py
RUN chmod 777 /usr/bin/populate_demo_data_roles.py
RUN chmod 777 -R /usr/bin/userskonker/
RUN python /usr/bin/setup.py install
RUN rm /usr/bin/setup.py
RUN ln -s /usr/bin/dsl.py /usr/bin/konker
#RUN ln -s /usr/bin/populate_users.py /usr/bin/populate_users
#RUN ln -s /usr/bin/populate_demo_data_roles.py /usr/bin/populate_demo_data_roles

VOLUME /data/db

ENTRYPOINT ["/docker-entrypoint.sh"]
