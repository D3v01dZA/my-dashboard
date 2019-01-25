#!/bin/bash

branch="${1}"
port="${2}"

cd my-dashboard/service
git fetch
git checkout "${branch}"
git pull
./gradlew build
cd -

read db_user < db_user.txt
read db_pass < db_pass.txt
read store_pass < store_pass.txt

java -jar ./my-dashboard/service/build/libs/my-dashboard-0.1.0.jar \
        --server.port="${port}" \
        --security.require-ssl=true \
        --server.ssl.key-store=keystore \
        --server.ssl.key-store-type=PKCS12 \
        --server.ssl.key-store-password="${store_pass}" \
        --server.ssl.key-alias=tomcat \
        --spring.datasource.url="jdbc:postgresql://localhost:5432/${branch}" \
        --spring.datasource.username="${db_user}" \
        --spring.datasource.password="${db_pass}" >> "running/log_${branch}.txt" &
echo $! > "running/pid_${branch}.txt"
