FROM ubuntu:20.04

RUN apt-get update && apt-get install -y openjdk-8-jdk

RUN useradd -m time
USER time
WORKDIR /home/time

COPY . .

RUN ./gradlew clean build

ENTRYPOINT ["run.sh"]