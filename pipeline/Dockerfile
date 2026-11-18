#
# Build stage
#
FROM maven:3.9.6-eclipse-temurin-17 AS build
ARG ENABLE_OTEL=false
WORKDIR /home/app
COPY pipeline/pom.xml .
COPY pipeline/src ./src
COPY apis/protobuf /home/apis/protobuf
RUN if [ "$ENABLE_OTEL" = "true" ]; then \
      mvn clean package -DskipTests -Pmonitoring; \
    else \
      mvn clean package -DskipTests; \
    fi

#
# Runtime stage
#
FROM eclipse-temurin:17-jdk-jammy
ARG ENABLE_OTEL=false
WORKDIR /app
COPY --from=build /home/app/target/pipeline*.jar app.jar
RUN --mount=type=bind,from=build,source=/home/app/target,target=/build-output \
    if [ "$ENABLE_OTEL" = "true" ] && [ -f /build-output/agents/opentelemetry-javaagent.jar ]; then \
      cp /build-output/agents/opentelemetry-javaagent.jar /app/otel-agent.jar; \
    fi
COPY pipeline/entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh
EXPOSE 8080
ENTRYPOINT ["/app/entrypoint.sh"]
