echo "Do not run this as a script"
echo "Copy application.properties.example to application.properties and edit"
echo "Copy firebase private key to firebase-priv.json"
exit 1

docker run --name selenium -p 4444:4444 -d -v /dev/shm:/dev/shm selenium/standalone-chrome
docker build .
docker kill selenium
docker rm selenium