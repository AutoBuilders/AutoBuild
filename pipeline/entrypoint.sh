#!/bin/sh
if [ -f /app/otel-agent.jar ]; then
  exec java -javaagent:/app/otel-agent.jar -jar /app/app.jar "$@"
else
  exec java -jar /app/app.jar "$@"
fi
