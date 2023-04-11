# Run Konker Platform with a single command using Docker compose
One insteresting way to run Konker platform is by using this docker-compose file. It serves the purpose of spinning up a single konker platform instance locally or even in a development server

# What is inside
In this docker-compose you will start
- Cassandra single instance
- Mongodb single instance
- Redis single instance
- RabbitMQ single instance
- nginx single instance for ingress with SNI and ssl possibility
- Konker Api single instance
- Konker Data single instance
- Konker Webapp single instance
- Konker Mosquitto MQQT single instance with in house developed Basic Authentication support


# How much it costs
Konker Platform is Open Source, we use Apache License model, so it's free for private and business usage. We appreciate to se a note in your system/product telling that Konker is a core feature for you


# How to use
First copy the env_example file with .env name, second step is that you adjust the parameters inside of .env to fit your requirements, however if you leave it as it is, it's enough to start testing

Run the docker-compose
```
docker-compose up -d
```
It's all, you now can check the services in the ports
- http://localhost:8080/registry/login for webapp
- http://localhost:8081/v1/swagger-ui.html for api
- http://localhost:8082/registry-data/status for data

Using nginx for your domain
Change the nginx/nginx.conf server_name for each webapp, api and data, then either test it by configuring your etc/hosts or making it public through your DNS server.

Checking the instalation
Check the docker-compose services status, all services should have a run status
```
docker-compose ps
```

# MQTT TLS
This configuration is using MQTT tls bind, which means mosquitto will open ports 1883 for standard connections and 8883 for tls connections. In order to have the right valid certificate, please replace the files bellow with your company certificates, otherwise tls will work but not ensure the correct way for your clients to make sure they are connecting to the right provider
```
mqtt/ca.crt
mqtt/server.crt
mqtt/server.key
```

Questions?
Feel free to leave us an issue in Github, we are happy to support you.

