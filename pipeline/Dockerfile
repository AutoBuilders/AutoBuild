#
# Build stage
#
FROM maven:3.8.3-openjdk-17 AS build
WORKDIR /home/app
COPY pom.xml .
RUN mvn dependency:go-offline -Pmonitoring -q
COPY src ./src
RUN mvn clean package -DskipTests -Pmonitoring

#
# Runtime stage
#
FROM eclipse-temurin:17-jre-jammy
RUN groupadd --system appgroup && useradd --system --gid appgroup appuser
WORKDIR /app
COPY --from=build /home/app/target/pipeline*.jar app.jar
COPY --from=build /home/app/target/agents/*.jar otel-agent.jar
RUN mkdir -p /pipeline_scripts /pipeline_build_logs \
    && chown -R appuser:appgroup /app /pipeline_scripts /pipeline_build_logs
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-javaagent:otel-agent.jar", "-jar", "app.jar"]
