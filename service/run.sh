#!/bin/bash

java -jar "build/libs/my-dashboard-0.1.0.jar" --spring.config.additional-location="application.properties" --logging.file="logs/log.txt"