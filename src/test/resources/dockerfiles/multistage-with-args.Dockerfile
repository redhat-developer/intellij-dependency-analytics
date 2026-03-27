ARG VERSION=23.0.1
FROM --platform=linux/amd64 quay.io/keycloak/keycloak:${VERSION}
FROM scratch
FROM quay.io/fedora/fedora:36
