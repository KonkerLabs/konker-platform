----

## Authentication

### Overview

Konker Platform API uses the OAuth protocol to allow users securely access data.

Before making REST API calls, you must get an access token using [OAuth 2.0](http://oauth.net/).

You can see the API endpoints documentation and interact with them on this page.

### Get OAuth 2 Access Token

* Grant Type: Implicit Grant (or User Agent) Flow
* Access Token URL: http://<HOSTNAME>/v1/oauth/token
* Client ID: your e-mail
* Client Secret: your password

### Swagger Authorization

To use Swagger endpoints:

* Login using your email and password: <a href='/v1/oauth/token?grant_type=client_credentials' target='_blank'>here</a>
* Click the 'Authorize' button in upper right conner

----

## Concepts

### Basic Entities

* Devices: physical devices
* Routes: delivers an incoming message to another device or to a REST destination
* REST destination: HTTP based RESTful URLs
* REST transformation: REST operations that can edit (enrich, convert, etc) the device messages

### Organization & Application

A user belongs to an organization.

An organization can have multiples applications.

An application is a repository of basic entities.

For instance, you can organize your devices in applications if you have many IoT projects.

![Concepts](static/diagram.png)

### Devices

Each device has a key and a password. The 'device credentials' operations manage these credentials.

The Platform will receive the device's messages. The 'events' operations list these messages.

### More Informations

* [Developer Portal](http://developers.konkerlabs.com)
* [Operation Guide (in Portuguese)](https://konker.atlassian.net/wiki/display/DEV/Guia+de+Uso+da+Plataforma+Konker)

----
