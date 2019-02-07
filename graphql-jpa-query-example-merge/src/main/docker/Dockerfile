FROM openjdk:8-jdk-alpine

VOLUME /var/log/
VOLUME /tmp

EXPOSE 8080

ADD graphql-jpa-query-example-merge.jar app.jar

ENV JAVA_OPTS=""

ENTRYPOINT exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar
