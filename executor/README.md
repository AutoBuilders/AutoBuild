# Executor Go Module

## Prod like execution
Script to startup dagger

```sh
docker run \
  --name dagger-engine \
  --privileged \
  -p 8081:8081 \
  -e DAGGER_ENGINE_PORT=8081 \
  -v dagger-engine-cache:/var/lib/dagger \
  --restart unless-stopped \
  registry.dagger.io/engine:v0.12.0
```

Script to start go service

```sh
_DAGGER_RUNNER_HOST=tcp://localhost:8081 EXECUTOR_PROFILE=local go run main.go
```

## Local Execution

```sh
EXECUTOR_PROFILE=local go run main.go
```

No separate dagger service is required as it will fallback to starting it's own container.
