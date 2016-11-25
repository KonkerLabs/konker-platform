# konker-registry
Docker image for konker ioT platform

#Dependencies
  * A redis server
  * A mongodb server
  * A mosquitto mqtt broker

#Build
```
make ENV='Your profile folder inside env/'
```
The profile folder should have 3 files:
 - logback.xml (logback config file)
 - resolv.conf (/etc/resolv.conf, file with dns config provider)
 - application.conf (konker platform file)

#Install docker image
```
sudo make install
```
#Run
```
docker run -p 8080:8080 --name konker-webapp -itd [docker image id]
```

#Usage
Access your localhost:8080/registry


Contributors
  * André Rocha

Licence
  * Copyright konker 2016
