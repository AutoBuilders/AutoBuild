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
	// Parse the DTO
	var pipelineDTO dto.PipelineDTO

	err := json.NewDecoder(r.Body).Decode(&pipelineDTO)
	if err != nil {
		http.Error(w, "Invalid JSON payload: "+err.Error(), http.StatusBadRequest)
		return
	}
	fmt.Println(pipelineDTO) // TODO: Remove as not required now

	w.Header().Set("Content-Type", "application/json")

	//setup dagger client
	ctx := context.Background()
	// 1. Initialize the Dagger client
	client, err := dagger.Connect(ctx, dagger.WithLogOutput(os.Stdout))
	if err != nil {
		panic(err)
	}
	defer client.Close()

	fmt.Println("Executing Pipeline ", pipelineDTO.Name)

	// define dagger container
	container := client.Container().From("alpine").WithWorkdir("/src")

	// var pipelineBuildDTO dto.PipelineBuildDTO
	pipelineBuildDTO := dto.PipelineBuildDTO{
		PipelineId: pipelineDTO.Id,
		CurrentState: dto.PipelineRunning,
		StageBuilds: []dto.StageBuildDTO{},
	}

	fmt.Println("Pipeline Build state: ", pipelineBuildDTO.CurrentState)
	// execute per stage in the container
	for _ , stage := range pipelineDTO.Stages {
		fmt.Println("Executing Stage ", stage.Name)
		stageBuildDTO := dto.StageBuildDTO {
			StageId: stage.Id,
			CurrentState: dto.StageRunning,
		}

		scriptPath := fmt.Sprintf("/tmp/script_%s.sh", stage.Name)
		container = container.WithNewFile(scriptPath, stage.Command, dagger.ContainerWithNewFileOpts{
			Permissions: 0755,
		}).WithExec([]string{"sh", scriptPath})

		output, err := container.Stdout(ctx)
		if err != nil {
			log.Printf("Pipeline execution failed: %v", err)
			stageBuildDTO.CurrentState = dto.StageFailed
			pipelineBuildDTO.StageBuilds = append(pipelineBuildDTO.StageBuilds, stageBuildDTO)
			pipelineBuildDTO.CurrentState = dto.PipelineFailed
			http.Error(w, `{"error": "pipeline failed"}`, http.StatusInternalServerError)
			return
		}

		stageBuildDTO.CurrentState = dto.StageSuccess
		pipelineBuildDTO.StageBuilds = append(pipelineBuildDTO.StageBuilds, stageBuildDTO)
		fmt.Println("Stage build output", output)
		fmt.Println("Build snapshot: ", pipelineBuildDTO)
	}

	pipelineBuildDTO.CurrentState = dto.PipelineSuccess
	fmt.Println("Final Build snapshot: ", pipelineBuildDTO)
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
