FROM maven:3.8-openjdk-11 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src/ ./src/
RUN mvn package -DskipTests

FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=build /app/target/partymaker-spring-server-1.0.0.jar app.jar
COPY src/main/resources/serviceAccountKey.json /app/serviceAccountKey.json

ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENV FIREBASE_DATABASE_URL="https://partymaker-app.firebaseio.com"
ENV FIREBASE_STORAGE_BUCKET="partymaker-app.appspot.com"

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"] 