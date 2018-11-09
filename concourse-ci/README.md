# Konker CI/CD

## Compose Docker

To run Concourse on your machine, follow these steps:

   `https://concourse.ci/docker-repository.html`

## Requirements

* [spruce](https://github.com/geofffranks/spruce): YAML merging tool
* [fly](http://concourse.staging.konkerlabs.net/api/v1/cli?arch=amd64&platform=linux)

## Merge YAMLs

`spruce merge --prune params registry.yml parameters.yml > data.yml`

## Run

### Host

```
concourse-server-host
```

### Commands

```
sudo service postgresql95 start
sudo service concourse-worker start
sudo service concourse-scheduler start
```

## FLY

`fly targets`

### Create a target

`fly --target <your target> login --concourse-url http://concourse.staging.konker.internal:8080/`

### Update Concourse

`fly -t <your target> set-pipeline -p <your pipeline> -c < your data.yml>`


