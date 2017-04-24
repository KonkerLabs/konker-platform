# Registry Data Ingestion Application

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
java -jar target/registry.data.jar
```