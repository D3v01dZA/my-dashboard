#!/bin/bash

set -e

branch="${1}"

if [[ -z "${branch}" ]]; then
    echo "No branch specified"
    exit 1
fi

cd /home/dashboard/

echo "Building boot jar"
cd my-dashboard/service
git fetch
git checkout "${branch}"
git pull
./gradlew clean build
cd -

echo "Copying boot jar"
cp ./my-dashboard/service/build/libs/my-dashboard-0.1.0.jar "${branch}/boot.jar"

echo "Building keystore"
cd "${branch}"
password=`../prop.sh server.ssl.key-store-password application.properties`
openssl pkcs12 -export -in ssl/fullchain.pem -inkey ssl/privkey.pem -out keystore -name tomcat -CAfile ssl/chain.pem -caname root -password "pass:${password}"

echo "Running application"
java -jar ./boot.jar --spring.config.additional-location="application.properties" --logging.file="log.txt"