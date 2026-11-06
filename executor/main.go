package main

import (
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"os"

	"time"

	"github.com/AutoBuilders/AutoBuild/executor/build"
	"github.com/AutoBuilders/AutoBuild/executor/dto"
	"github.com/AutoBuilders/AutoBuild/executor/runner"
	"github.com/joho/godotenv"
)

func handleExecute(w http.ResponseWriter, r *http.Request) {
	// Parse the DTO
	pipelineDTO, shouldReturn := build.ParsePayload(r, w)
	if shouldReturn {
		return
	}

	// TODO: abstract it behind a runner
	//setup dagger client
	ctx, client := runner.SetupDaggerClient()
	defer client.Close()

	fmt.Println("Executing Pipeline ", pipelineDTO.Name)

	pipelineBuildDTO, anyStageBuildFailed := build.Start(ctx, client, pipelineDTO)

	if anyStageBuildFailed {
		pipelineBuildDTO.CurrentState = dto.PipelineFailed
		http.Error(w, `{"error": "pipeline failed"}`, http.StatusInternalServerError)
	} else {
		pipelineBuildDTO.CurrentState = dto.PipelineSuccess
		// http.StatusOK(w, `{"error": "pipeline failed"}`, http.StatusInternalServerError)
		json.NewEncoder(w).Encode(pipelineBuildDTO)
	}
}

func main() {
	appProfile := os.Getenv("EXECUTOR_PROFILE")

	if appProfile == "" {
		log.Fatal("Please pass along a valid application profile")
	}

	envFile := ".env." + appProfile
	err := godotenv.Load(envFile)
	if err!=nil {
		log.Fatal("Error ocurred while loading env file: ", envFile)
	}
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

	mux.HandleFunc("POST /api/v1/execute/pipeline", handleExecute)

	// Configure server settings explicitly
	serverPortAddr := ":" + os.Getenv("EXECUTOR_PORT")
	server := &http.Server{
		Addr:           serverPortAddr,
		Handler:        mux,
		ReadTimeout:    10 * time.Second, // Max duration reading the entire request
		WriteTimeout:   10 * time.Second, // Max duration writing the response
		MaxHeaderBytes: 1 << 20,          // 1 MB max header size
	}

	log.Println("Server listening on http://localhost", serverPortAddr)
	if err := server.ListenAndServe(); err != nil {
		log.Fatalf("Server failed: %s", err)
	}
}
