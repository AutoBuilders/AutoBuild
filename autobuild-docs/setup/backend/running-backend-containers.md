# Run backend containers

## App stack only

From project root, build and start the application and database:
```bash
docker compose --env-file .env.local up -d
```

## App stack + monitoring

Starts the app stack together with the full monitoring stack (Grafana, Loki, Prometheus, Alloy). All services start in dependency order automatically:
```bash
docker compose --env-file .env.local \
  -f docker-compose.yml \
  -f monitoring/docker-compose-monitoring.yml \
  -f monitoring/docker-compose-monitoring-integration.yml \
  up -d
```

Monitoring UIs available at:
- Grafana: http://localhost:3000
- Alloy: http://localhost:12345
- Prometheus: http://localhost:9090

## UI container only

Build and run the UI container in isolation (build args default to the docker-compose values):
```bash
docker build -t pipeline-ui ./pipeline-ui
docker run -p 3000:3000 pipeline-ui
```

To override configuration, see `pipeline-ui/.env.local` for the available build args and their defaults.

## Stopping

```bash
docker compose down
```

To include the monitoring stack when tearing down:
```bash
docker compose \
  -f docker-compose.yml \
  -f monitoring/docker-compose-monitoring.yml \
  -f monitoring/docker-compose-monitoring-integration.yml \
  down
```

An easier command to do the same would be,
```bash
docker compose -p autobuild down
```

## Fresh instance

```bash
docker compose down -v
```
> Note: this will delete all persisted volumes including database data, Grafana dashboards, Loki logs, and Prometheus metrics.

## Protobuf

Proto classes are auto-generated during `docker build` — Maven runs the protobuf compiler as part of the build, so no separate step is needed.
