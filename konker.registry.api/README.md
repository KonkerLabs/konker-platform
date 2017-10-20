# Registry Api Application

Package:

If you use MongoDb as event storage:

```
mvn clean install package spring-boot:repackage
```

If you use Cassandra as event storage
```
mvn clean install package spring-boot:repackage -Pcassandra
```

Usage:
```
java -jar target/registry.api.jar
```

To build docker image:

Requisites:
 - build binary by using the Package step.
 - define params in resources/application.conf file


 - Copy the registry-api.jar from target folder to docker-resources/build
 - Copy the application.conf.example file to docker-resources/build/application.conf
 - Set the targets of your mongodb*, cassandra and redis instalations ( the parameters are described as CHANGE_ME in the application.conf.example)
   * If you wanto to use mongodb as event storage, you can change the block:
   ```
   eventstorage {
    bean="cassandraEvents"
   }
   ```
   to 
   ```
   eventstorage {
    bean="mongoEvents"
   }
   ```
   ```
 - Set the target of filebeat according to your logstash instalation
Build:
```
sudo docker build -t youruser/yourrepository:desired-tag .
```
Run:
```
sudo docker run --rm --name konkerapi -p 80:80 -d -e MONGODB_HOSTNAME=CHANGE_ME -e MONGODB_PORT=CHANGE_ME -e MONGODB_USERNAME=CHANGE_ME -e MONGODB_AUDIT_HOSTNAME=CHANGE_ME -e MONGODB_AUDIT_PORT=CHANGE_ME -e MONGODB_AUDIT_USERNAME=CHANGE_ME -e MONGODB_AUDIT_PASSWORD=CHANGE_ME -e MONGODB_PASSWORD=CHANGE_ME -e EVENT_STORAGE=CHANGE_ME -e CASSANDRA_HOSTNAME=CHANGE_ME -e CASSANDRA_PORT=CHANGE_ME -e CASSANDRA_USERNAME=CHANGE_ME -e CASSANDRA_PASSWORD=CHANGE_ME -e CASSANDRA_KEYSPACE=CHANGE_ME -e CASSANDRA_CLUSTERNAME=CHANGE_ME youruser/your_repository:desired-tag
```