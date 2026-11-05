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

func initializePipelineBuild(pipelineDTO dto.PipelineDTO) dto.PipelineBuildDTO {
	pipelineBuildDTO := dto.PipelineBuildDTO{
		PipelineId: pipelineDTO.Id,
		CurrentState: dto.PipelineWaiting,
		StageBuilds: []dto.StageBuildDTO{},
	}

	for _, stageDTO := range pipelineDTO.Stages {
		stageBuildDTO := dto.StageBuildDTO {
			StageId: stageDTO.Id,
			CurrentState: dto.StageWaiting,
		}

		pipelineBuildDTO.StageBuilds = append(pipelineBuildDTO.StageBuilds, stageBuildDTO)
	}

	b, _ := json.MarshalIndent(pipelineBuildDTO, "", "  ")
	fmt.Println("init build dto:", string(b))
	return pipelineBuildDTO
}

func executePipeline(w http.ResponseWriter, r *http.Request) {
	// Parse the DTO
	var pipelineDTO dto.PipelineDTO

	err := json.NewDecoder(r.Body).Decode(&pipelineDTO)
	if err != nil {
		http.Error(w, "Invalid JSON payload: "+err.Error(), http.StatusBadRequest)
		return
	}

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

	pipelineBuildDTO := initializePipelineBuild(pipelineDTO)
	pipelineBuildDTO.CurrentState = dto.PipelineRunning
	
	anyStageBuildFailed := false

	// execute per stage in the container
	for i, stage := range pipelineDTO.Stages {
		notify(pipelineBuildDTO)
		stageBuild := &pipelineBuildDTO.StageBuilds[i]

		fmt.Println("Executing Stage ", stage.Name)

		if anyStageBuildFailed {
			stageBuild.CurrentState = dto.StageStopped
			continue
		}

		scriptPath := fmt.Sprintf("/tmp/script_%s.sh", stage.Name)
		container = container.WithNewFile(scriptPath, stage.Command, dagger.ContainerWithNewFileOpts{
			Permissions: 0755,
		}).WithExec([]string{"sh", scriptPath})

		stageBuild.CurrentState = dto.StageRunning
		output, err := container.Stdout(ctx)
		if err != nil {
			anyStageBuildFailed = true
			stageBuild.CurrentState = dto.StageFailed

			log.Printf("Pipeline execution failed: %v", err)
			continue
		}

		fmt.Println("Stage build output", output)

		stageBuild.CurrentState = dto.StageSuccess

	}

	if anyStageBuildFailed {
		pipelineBuildDTO.CurrentState = dto.PipelineFailed
		http.Error(w, `{"error": "pipeline failed"}`, http.StatusInternalServerError)
	} else {
		pipelineBuildDTO.CurrentState = dto.PipelineSuccess
		// http.StatusOK(w, `{"error": "pipeline failed"}`, http.StatusInternalServerError)
		json.NewEncoder(w).Encode(pipelineBuildDTO)
	}

	notify(pipelineBuildDTO)
}

func notify(pipelineBuildDTO dto.PipelineBuildDTO) {
	fmt.Println("-------------------------------")
	b, _ := json.MarshalIndent(pipelineBuildDTO, "", "  ")
	fmt.Println("Build snapshot:", string(b))
	fmt.Println("-------------------------------")
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
