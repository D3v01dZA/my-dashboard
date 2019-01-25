// Create the keystore post renewal
openssl pkcs12 -export -in fullchain.pem -inkey privkey.pem -out keystore.p12 -name tomcat -CAfile chain.pem -caname root

// Allow ssl users to read certificates
chgrp -R ssl /etc/letsencrypt
chmod -R g=rX /etc/letsencrypt
