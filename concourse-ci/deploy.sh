#!/bin/bash

spruce merge --prune params registry.yml parameters.yml > data.yml
fly -t registry set-pipeline -p registry -c data.yml
