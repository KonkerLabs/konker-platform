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
Access your localhost:8080/registry


Contributors
  * André Rocha

Licence
  * Copyright konker 2016
