#!/usr/bin/env bash
set -euo pipefail
cd "$BUILD_WORKSPACE_DIRECTORY"

case "${1:-}" in
  up)
    docker compose --env-file .env.local up -d
    ;;
  down)
    docker compose down
    ;;
  fresh)
    docker compose down -v
    ;;
  up-monitoring)
    docker compose --env-file .env.local \
      -f docker-compose.yml \
      -f monitoring/docker-compose-monitoring.yml \
      -f monitoring/docker-compose-monitoring-integration.yml \
      up -d
    ;;
  down-monitoring)
    docker compose \
      -f docker-compose.yml \
      -f monitoring/docker-compose-monitoring.yml \
      -f monitoring/docker-compose-monitoring-integration.yml \
      down
    ;;
  *)
    echo "Usage: bazel run //:docker -- {up|down|fresh|up-monitoring|down-monitoring}"
    exit 1
    ;;
esac
