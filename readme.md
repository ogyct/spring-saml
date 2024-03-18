# Springboot with saml2 vs keycloak1

## minimal keycloak setup

- create a separate realm
- create client with protocol saml
- disable Client Signature Required to work without client certificates
- enable Client Signature Required to work with client certificates
- generate client cert using `openssl req -newkey rsa:2048 -nodes -keyout local.key -x509 -days 365 -out local.crt`
- add public key to `keys` tab

## minimal app setup

- see src/main/resources/application.yaml
