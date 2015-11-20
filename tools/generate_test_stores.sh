#!/bin/bash

KEYSIZE=1024
VALIDITY=1460
rm -f serverStore.p12 serverTrustStore.p12 caKey.pem caCert.pem request.crs clientStore.p12 clientCert.crt clientStore.jks

echo ---=== Generating Certificates ===---
echo This tools will request several parameters.
echo The names are free, but it is recommended
echo to enter a name that identifies the certificate
echo for example TestServer, TestCA, TestClient, TestInvalidClient
echo Enter 123456 when asked for a password
echo

# Server Cert
echo --== Generating Server Certificate ==--
keytool -genkey -keyalg RSA -alias notnoop-server \
  -keystore serverStore.p12 -storepass 123456 -storetype PKCS12 \
  -validity ${VALIDITY} -keysize ${KEYSIZE}
keytool -exportcert -alias notnoop-server \
  -keystore serverStore.p12 -storepass 123456 -storetype PKCS12 | keytool -printcert

# Client Cert CA
echo --== Generating Client CA Certificate ==--
keytool -genkey -keyalg RSA -alias notnoop-ca \
  -keystore serverTrustStore.p12 -storetype pkcs12 -storepass 123456 \
  -validity ${VALIDITY} -keysize ${KEYSIZE}
keytool -exportcert -alias notnoop-ca \
  -keystore serverTrustStore.p12 -storepass 123456 -storetype PKCS12 | keytool -printcert

openssl pkcs12 -in serverTrustStore.p12 -nocerts -out caKey.pem 
openssl pkcs12 -in serverTrustStore.p12 -clcerts -nokeys -out caCert.pem 

echo --== Generating Client Certificate ==--
keytool -genkey -keyalg RSA -keystore clientStore.p12 -storepass 123456 -storetype PKCS12 -alias notnoop-client \
  -validity ${VALIDITY} -keysize ${KEYSIZE}
keytool -certreq -keystore clientStore.p12 -storepass 123456 -storetype PKCS12 -alias notnoop-client -file request.crs

echo --== Signing Client Certificate with CA ==--
openssl x509 -req -CA CACert.pem -CAkey CAKey.pem -in request.crs -out clientCert.crt \
  -days ${VALIDITY} -CAcreateserial -outform PEM -set_serial 1
cat caCert.pem >> clientCert.crt
keytool -import -keystore clientStore.p12 -storepass 123456 -storetype PKCS12 -file clientCert.crt -alias notnoop-client
keytool -exportcert -alias notnoop-client \
  -keystore clientStore.p12 -storepass 123456 -storetype PKCS12 | keytool -printcert

echo --== Generating JKS Client Keystore with Client Certificate ==--
keytool -importkeystore \
  -srckeystore clientStore.p12 -srcstorepass 123456 -srcstoretype PKCS12 -srcalias notnoop-client \
  -destkeystore clientStore.jks -deststorepass 123456 -deststoretype JKS -destalias notnoop-client

echo --== Generating Invalid Client Certificate ==--
keytool -genkeypair -keyalg RSA -keystore clientStore.jks -storepass 123456 -storetype JKS -alias notused  

keytool -list -keystore clientStore.jks -storepass 123456 -storetype JKS

rm caKey.pem caCert.pem request.crs clientCert.crt