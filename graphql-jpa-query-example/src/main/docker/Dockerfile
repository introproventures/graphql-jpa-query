
FROM frolvlad/alpine-oraclejdk8:slim
VOLUME /var/log/
EXPOSE 8080


COPY . /usr/src/
WORKDIR /usr/src/
# run the command
CMD ["java","-jar", "graphql-jpa-query-example-0.3.0-SNAPSHOT.jar"]

