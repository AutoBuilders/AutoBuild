package main

import (
	"log"
	"net/http"

	"github.com/AutoBuilders/AutoBuild/executor/config"
	"github.com/AutoBuilders/AutoBuild/executor/handler"
)


func main() {
	config.LoadProfile()
	
	mux := http.NewServeMux()

	handler.RegisterHandlers(mux)
	serverPortAddr, server := config.InitServer(mux)

	log.Println("Server listening on http://localhost", serverPortAddr)
	if err := server.ListenAndServe(); err != nil {
		log.Fatalf("Server failed: %s", err)
	}
}

