package main

import (
	"log"

	"github.com/AutoBuilders/AutoBuild/executor/config"
)


func main() {
	config.LoadProfile()
	// Create an isolated router instance (instead of global DefaultServeMux)
	serverPortAddr, server := config.InitServer()

	log.Println("Server listening on http://localhost", serverPortAddr)
	if err := server.ListenAndServe(); err != nil {
		log.Fatalf("Server failed: %s", err)
	}
}

