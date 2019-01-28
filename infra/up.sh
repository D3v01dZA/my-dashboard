#!/bin/bash

branch="${1}"
port="${2}"

if [[ -z "${branch}" ]]; then
    echo "No branch specified"
    exit 1
fi
if [[ -z "${port}" ]]; then
    echo "No port specified"
    exit 1
fi

cd my-dashboard/service
git fetch
git checkout "${branch}"
git pull
./gradlew clean build
cd -

read db_user < props/db_user.txt
read db_pass < props/db_pass.txt
read store_pass < props/store_pass.txt

java -jar ./my-dashboard/service/build/libs/my-dashboard-0.1.0.jar --spring.config.additional-location="${branch}/application.properties" >> "${branch}/log.txt" &
echo $! > "${branch}/pid.txt"
