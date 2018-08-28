#!/usr/bin/env bash
set -ex

#echo $version
echo "Annie"
echo $imageVersion > test

#USERNAME=alannesta
#IMAGE=reporting-service
#VERSION=`cat ./DOCKER_IMAGE_VERSION`

#echo "login into docker hub..."
#docker login --username=alannesta
#
#echo "building $USERNAME/$IMAGE:$VERSION..."
#
#docker build --build-arg JAR_FILE=target/lister-report-${VERSION}.jar -t $USERNAME/$IMAGE:$VERSION -t $USERNAME/$IMAGE:latest .
#
#docker push $USERNAME/$IMAGE:latest
#docker push $USERNAME/$IMAGE:$VERSION
#
#echo "image ${USERNAME}/${IMAGE}:${VERSION} published..."
