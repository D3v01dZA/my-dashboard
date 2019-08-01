#!/bin/bash
set -e

branch="${1}"

if [[ -z "${branch}" ]]; then
    echo "No branch specified"
    exit 1
fi

echo "Building boot jar"
cd my-dashboard/service
git fetch
git checkout "${branch}"
git pull
./gradlew clean build -i --no-daemon
cd -

echo "Copying jar"
cp ./my-dashboard/service/build/libs/my-dashboard-0.1.0.jar "${branch}/boot.jar"

cd "${branch}"
echo "Reading keystore password"
password=`../prop.sh server.ssl.key-store-password application.properties`
echo "Building keystore"
openssl pkcs12 -export -in ssl/fullchain.pem -inkey ssl/privkey.pem -out keystore -name tomcat -CAfile ssl/chain.pem -caname root -password "pass:${password}"
cd -

echo "Changing to run directory"
cd "${branch}"
java -jar ./boot.jar --spring.config.additional-location="application.properties" >> "log.txt" &
echo $! > "pid.txt"
cd -

less +F "${branch}/log.txt"
