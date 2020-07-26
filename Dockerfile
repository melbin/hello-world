FROM openjdk:8u212-jre-alpine3.9
ARG JAR_FILE
ADD ${JAR_FILE} app.jar
EXPOSE 8080
ENV SPRING_APPLICATION_JSON='{"db.password":"test2"}'
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]
