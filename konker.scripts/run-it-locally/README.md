## HOW to use docker-compose

Before you can run, we need to create the databases

Plase start by running `docker-compose up-d mongodb`

Now we need to access the container and create the databases

```
docker exec -it mongodb bash
mongo -u root -p mongo --authenticationDatabase=admin

use registry;
db.createUser(
{
    user: "registry",
    pwd: "registry",
    roles: [
              { role: "dbOwner", db: "registry" }
           ]
});

use logs;
db.createUser(
{
    user: "registry",
    pwd: "registry",
    roles: [
              { role: "dbOwner", db: "logs" }
           ]
});

use billing;
db.createUser(
{
    user: "registry",
    pwd: "registry",
    roles: [
              { role: "dbOwner", db: "billing" }
           ]
});

use private-storage;
db.createUser(
{
    user: "registry",
    pwd: "registry",
    roles: [
              { role: "dbOwner", db: "private-storage" }
           ]
});

exit
exit
```
We also need to populate roles and create initial admin user
```
sudo docker-compose up -d init-database
```

Now we can start the event database
```
sudo docker-compose up -d cassandra
```
And create the keyspace
```
docker exec -it mongodb bash

cqlsh -u cassandra -p cassandra
CREATE KEYSPACE registry  WITH REPLICATION = {     'class' : 'SimpleStrategy', 'replication_factor': 1 } ;

exit
exit
```
Now go to konker.registry.services.storage.cassandra and run all the sql files usin cqlsh


Now we can start other containers
```
sudo docker-compose up -d rabbitmq redis mosquitto web api data data-processor bridge router
```

Services will be available at
- API http://localhost:8080
- API docs http://localhost:8080/v1/swagger-ui.html
- WEB http://localhost:8484/registry/login
- DATA http://localhost:8181

The initial password will be:
user = admin@localhost
pwd = changeme


## Optional
You can get rid of one database by using Mongodb as the eventstorage, for that:
- Comment out the block for cassandra database in the docker-compose.yml
- Change the configs for `EVENT_STORAGE_BEAN` from "cassandraEvents" to "mongoEvents" (default is mongoEvents if not informed) 