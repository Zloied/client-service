FROM openliberty/open-liberty:kernel-java8-openj9-ubi

ARG VERSION=1.0

LABEL \
  org.opencontainers.image.authors="Eduard Vaisman" \
  org.opencontainers.image.version="$VERSION" \
  name="client-service" \
  summary="The client microservice for trial purposes" \
  description="This image contains the client microservice running with the Open Liberty runtime."

COPY --chown=1001:0 \
    src/main/liberty/config \
    /config/

COPY --chown=1001:0 \
    target/client-service.war \
    /config/apps

COPY --chown=1001:0 ./target/liberty/wlp/usr/shared/resources /opt/ol/wlp/usr/shared/resources/
COPY --chown=1001:0 ./target/bootstrap.properties /opt/ol/wlp/usr/servers/defaultServer/

RUN configure.sh