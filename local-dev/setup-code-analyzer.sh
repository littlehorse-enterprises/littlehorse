#!/bin/bash

set -e

docker run -d -p 9999:9000 --name sonarqube sonarqube -Dsonar.forceAuthentication=false
