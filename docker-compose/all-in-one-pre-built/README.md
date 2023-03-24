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


Questions?
Feel free to leave us an issue in Github, we are happy to support you.

