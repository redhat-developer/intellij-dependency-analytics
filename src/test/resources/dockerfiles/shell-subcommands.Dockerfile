####
# This Dockerfile is used in order to build a container that runs the Spring Boot application
####
FROM registry.access.redhat.com/ubi8/openjdk-17:1.14 AS builder

# Build dependency offline to streamline build
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src src
RUN mvn package -Dmaven.test.skip=true
# compute the created jar name and put it in a known location to copy to the next layer.
RUN grep version /home/jboss/target/maven-archiver/pom.properties | cut -d '=' -f2 >.env-version
RUN grep artifactId /home/jboss/target/maven-archiver/pom.properties | cut -d '=' -f2 >.env-id
RUN mv /home/jboss/target/$(cat .env-id)-$(cat .env-version).jar /home/jboss/target/export-run-artifact.jar

FROM registry.access.redhat.com/ubi8/openjdk-17-runtime:1.15
COPY --from=builder /home/jboss/target/export-run-artifact.jar  /deployments/export-run-artifact.jar
EXPOSE 8081
ENTRYPOINT ["/opt/jboss/container/java/run/run-java.sh", "--server.port=8081"]
