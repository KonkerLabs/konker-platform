# konker-registry
Docker image for demo konker ioT platform

#Dependencies
  * A docker ready machine

#Build
```
Docker build .
```

#Run
```
docker run -p 80:80 -p 443:443 -p 1883:1883 -p 8883:8883 --name konker-webapp-demo -itd [docker image id]
```

#Run from hub
```
docker run -p 80:80 -p 443:443 -p 1883:1883 -p 8883:8883 --name konker-webapp-demo -itd sonecabr/konker-webapp-demo:latest
```

#Usage
Access your localhost/registry


##Environment Variables

* PUB\_HTTP\_HOSTNAME: will overwrite pubServer.httpHostname (application.conf)
* PUB\_MQTT\_HOSTNAME: will overwrite pubServer.mqttHostname (application.conf)

To set this variables in the Docker Container:

` docker run -p 80:80 -p 443:443 -p 1883:1883 -p 8883:8883 -e PUB_HTTP_HOSTNAME="<your ip or hostname>" -e PUB_MQTT_HOSTNAME="<your ip or hostname>" --name konker-webapp-demo -itd sonecabr/konker-webapp-demo:latest `


Contributors
  * André Rocha

Licence
  * Copyright konker 2016
