# Executor Go Module

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
_DAGGER_RUNNER_HOST=tcp://dagger-engine:8081 go run main.go
```
