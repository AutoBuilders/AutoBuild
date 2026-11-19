#
# Dev build stage
#
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /home/app
COPY pipeline/pom.xml .
COPY pipeline/src ./src
COPY apis/protobuf /home/apis/protobuf
RUN mvn clean package -DskipTests

#
# Monitoring build stage
#
FROM maven:3.9.6-eclipse-temurin-17 AS build-monitoring
WORKDIR /home/app
COPY pipeline/pom.xml .
COPY pipeline/src ./src
COPY apis/protobuf /home/apis/protobuf
RUN mvn clean package -DskipTests -Pmonitoring

#
# Dev runtime target
#
FROM eclipse-temurin:17-jdk-jammy AS dev
WORKDIR /app
COPY --from=build /home/app/target/pipeline*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

#
# Monitoring runtime target
#
FROM eclipse-temurin:17-jdk-jammy AS monitoring
WORKDIR /app
COPY --from=build-monitoring /home/app/target/pipeline*.jar app.jar
COPY --from=build-monitoring /home/app/target/agents/opentelemetry-javaagent.jar otel-agent.jar
EXPOSE 8080
ENTRYPOINT ["java", "-javaagent:otel-agent.jar", "-jar", "app.jar"]
