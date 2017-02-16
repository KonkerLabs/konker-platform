# konker-platform

Konker Platform is an Open Source Platform for the Internet of Things (IoT). It is developed by Konker and the community.

The Platform alows easy connection and management of devices using HTTP or MQTT protocols.

## Pre-requisites
Konker Platform runs and compiles on Java 8.

It has a compile-time dependency on Lombok (see below) and runtime dependencies on Eclipse Jetty, MongoDB, Redis and Mosquitto.

### Dependencies
#### Lombok

###### Intellij
Just install the Lombok plugin.

###### Eclipse
1. ```java -jar $MAVEN_REPOSITORY/org/projectlombok/lombok/$VERSION/lombok-$VERSION.jar```
2. Click on "Specify Location"
3. Select the eclipse executable
4. Click on Install / Update
5. Restart the Eclipse

### Building
Konker Platform is built by using Apache Maven

```maven package```

### Running
#### Run standalone container ####
If you want to run the Konker Open Platform on your own desktop, we offer a Docker image with allin resources to help you.
Please visit : https://hub.docker.com/r/konkerlabs/konker-platform/

If you need some help, please contact-us on support@konkerlabs.com

#### Hosted Cloud Environment ####
Konker provides a hosted Konker Platform. Please, contact us at http://www.konkerlabs.com .

#### Deploying ####
If you built your package with maven, the Konker Platform can be deployed as a web application on your favorit servlet container (we use jetty). You will need to customize the application.conf file to your needs. See application.conf.example on how to do that.

## License and Copyright
   Copyright 2017 Konker Labs

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
