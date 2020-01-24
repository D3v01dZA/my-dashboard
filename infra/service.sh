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
./gradlew clean build -i --no-daemon
cd -

echo "Copying boot jar"
cp ./my-dashboard/service/build/libs/my-dashboard-0.1.0.jar "${branch}/boot.jar"

echo "Changing to run directory"
cd "${branch}"
echo "Running application"
java -jar "boot.jar" --spring.config.additional-location="application.properties" --logging.file="logs/log.txt"
