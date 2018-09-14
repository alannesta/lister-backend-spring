#!/usr/bin/env bash
# this script will be run by `maven package` job
set -ex

echo $imageVersion

USERNAME=alannesta
IMAGE=reporting-service
VERSION=`cat ./DOCKER_IMAGE_VERSION`

echo "login into docker hub..."
docker login --username=alannesta

echo "building $USERNAME/$IMAGE:$VERSION..."

docker build --build-arg JAR_FILE=target/lister-report-${imageVersion}.jar -t $USERNAME/$IMAGE:$imageVersion -t $USERNAME/$IMAGE:latest .

docker push $USERNAME/$IMAGE:latest
docker push $USERNAME/$IMAGE:$imageVersion

echo "image ${USERNAME}/${IMAGE}:${imageVersion} published..."
