package main

import (
	"log"
	"net/http"
	"os"

	"github.com/AutoBuilders/AutoBuild/executor/config"
	"github.com/AutoBuilders/AutoBuild/executor/handler"
	"github.com/AutoBuilders/AutoBuild/executor/repository"
)


func main() {
	config.LoadProfile()

	repo := repository.NewPipelineBuildRepository(os.Getenv("EXECUTOR_DB_DSN"))

	mux := http.NewServeMux()

	handler.RegisterHandlers(mux, repo)
	serverPortAddr, server := config.InitServer(mux)

	log.Println("Server listening on http://localhost", serverPortAddr)
	if err := server.ListenAndServe(); err != nil {
		log.Fatalf("Server failed: %s", err)
	}
}

