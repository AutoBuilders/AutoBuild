package main

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"os"
	"time"

	"dagger.io/dagger"
	"github.com/AutoBuilders/AutoBuild/executor/dto"
)

func executePipeline(w http.ResponseWriter, r *http.Request) {
	var pipelineDTO dto.PipelineDTO

	err := json.NewDecoder(r.Body).Decode(&pipelineDTO)
	if err != nil {
		http.Error(w, "Invalid JSON payload: "+err.Error(), http.StatusBadRequest)
		return
	}
	fmt.Println(pipelineDTO) // TODO: Remove as not required now

	w.Header().Set("Content-Type", "application/json")
	ctx := context.Background()
	// 1. Initialize the Dagger client
	client, err := dagger.Connect(ctx, dagger.WithLogOutput(os.Stdout))
	if err != nil {
		panic(err)
	}
	defer client.Close()


	fmt.Println("Executing Pipeline ", pipelineDTO.Name)

	container := client.Container().From("alpine").WithWorkdir("/src")

	for _ , stage := range pipelineDTO.Stages {
		fmt.Println("Executing Stage ", stage.Name)
		// container = container.WithExec([]string{"sh", "-c", stage.Command}) // TODO: expect a splitted command
		scriptPath := fmt.Sprintf("/tmp/script_%s.sh", stage.Name)
		container = container.WithNewFile(scriptPath, stage.Command, dagger.ContainerWithNewFileOpts{
			Permissions: 0755,
		}).WithExec([]string{"sh", scriptPath})
	}

	output, err := container.Stdout(ctx)
	if err != nil {
		log.Printf("Pipeline execution failed: %v", err)
		http.Error(w, `{"error": "pipeline failed"}`, http.StatusInternalServerError)
		return
	}

	// 2. Fetch the host repository (where the user's code lives)
	// In your real platform, this could come from a Git clone instead of the local host
	// src := client.Host().Directory(".")
	//
	// output, err := client.Container().
	// 	From("alpine").
	// 	WithDirectory("/src", src).
	// 	WithWorkdir("/src").
	// 	WithExec([]string{"ls", "-la"}).
	// 	Stdout(ctx)
	// if err != nil {
	// 	log.Printf("Pipeline execution failed: %v", err)
	// 	http.Error(w, `{"error": "pipeline failed"}`, http.StatusInternalServerError)
	// 	return
	// }

	fmt.Println(output)
	fmt.Fprintln(w, `{"status": "Running Pipeline"}`)
}

func main() {
	// Create an isolated router instance (instead of global DefaultServeMux)
	mux := http.NewServeMux()

	// Define routes
	mux.HandleFunc("GET /", func(w http.ResponseWriter, r *http.Request) {
		fmt.Fprintln(w, "Welcome to the home page!")
	})

	mux.HandleFunc("GET /api/status", func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		fmt.Fprintln(w, `{"status": "OK"}`)
	})

	mux.HandleFunc("POST /api/v1/execute/pipeline", executePipeline)

	// Configure server settings explicitly
	server := &http.Server{
		Addr:           ":8080",
		Handler:        mux,
		ReadTimeout:    10 * time.Second, // Max duration reading the entire request
		WriteTimeout:   10 * time.Second, // Max duration writing the response
		MaxHeaderBytes: 1 << 20,          // 1 MB max header size
	}

	log.Println("Server listening on http://localhost:8080")
	if err := server.ListenAndServe(); err != nil {
		log.Fatalf("Server failed: %s", err)
	}
}
