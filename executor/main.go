package main

import (
	"context"
	"fmt"
	"log"
	"net/http"
	"os"
	"time"

	"dagger.io/dagger"
)

type StageExecutionState string

// const (
//     StageWaiting StageExecutionState = "WAITING"
//     StageRunning StageExecutionState = "RUNNING"
//     StageSuccess StageExecutionState = "SUCCESS"
//     StageFailed StageExecutionState = "FAILED"
//     StageStopped StageExecutionState = "STOPPED"
// )
//
// type StageBuildDTO struct {
//     id uuid.UUID
//
//     stageId uuid.UUID
//
//     stageName string
//
// 	currentState StageExecutionState
// }
//
// type PipelineExecutionState string
//
// const (
//     PipelineWaiting StageExecutionState = "WAITING"
//     PipelineRunning StageExecutionState = "RUNNING"
//     PipelineSuccess StageExecutionState = "SUCCESS"
//     PipelineFailed StageExecutionState = "FAILED"
// )
//
// type PipelineBuildDTO struct {
//     id uuid.UUID
//
//     pipelineId uuid.UUID
//
//     pipelineName string
//
// 	currentState PipelineExecutionState
//
//     stageBuilds []StageBuildDTO
// }

func executePipeline(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	ctx := context.Background()
	// 1. Initialize the Dagger client
	client, err := dagger.Connect(ctx, dagger.WithLogOutput(os.Stdout))
	if err != nil {
		panic(err)
	}
	defer client.Close()

	// 2. Fetch the host repository (where the user's code lives)
	// In your real platform, this could come from a Git clone instead of the local host
	src := client.Host().Directory(".")

	// 3. Define the container pipeline (e.g., building a Go application)
	// Dagger chains these instructions into a single graph
	// container := client.Container().
	// 	From("golang:1.23-alpine").                    // Base image
	// 	WithDirectory("/src", src).                    // Mount source code
	// 	WithWorkdir("/src").                           // Set working directory
	// 	WithExec([]string{"ls", "-la"}) // Execute build command

	output, err := client.Container().
		From("golang:1.23-alpine").
		WithDirectory("/src", src).
		WithWorkdir("/src").
		WithExec([]string{"ls", "-la"}).
		Stdout(ctx)
	if err != nil {
		log.Printf("Pipeline execution failed: %v", err)
		http.Error(w, `{"error": "pipeline failed"}`, http.StatusInternalServerError)
		return
	}

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
