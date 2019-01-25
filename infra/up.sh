#!/bin/bash

branch="${1}"
port="${2}"

echo "Branch ${branch}"
echo "Port ${port}"

cd my-dashboard/service
git fetch
git checkout "${branch}"
git pull
./gradlew build
cd -

java -jar ./my-dashboard/service/build/libs/my-dashboard-0.1.0.jar \
        --server.port="${port}" \
        --security.require-ssl=true \
        --server.ssl.key-store=/etc/letsencrypt/live/li1044-67.members.linode.com
        --server.ssl.key-store-type=PKCS12
        --server.ssl.key-alias=tomcat
        --spring.datasource.url="jdbc:postgresql://localhost:5432/${branch}" \
        --spring.datasource.username=app \
        --spring.datasource.password=password >> "running/log_${branch}.txt" &
echo $! > "running/pid_${branch}.txt"
