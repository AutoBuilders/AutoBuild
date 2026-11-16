package handler

import (
	"encoding/json"
	"fmt"
	"log"
	"net/http"

	"github.com/AutoBuilders/AutoBuild/executor/mapper"
	"github.com/AutoBuilders/AutoBuild/executor/repository"
	"github.com/AutoBuilders/AutoBuild/executor/service"
)

func RegisterHandlers(mux *http.ServeMux, repo *repository.PipelineBuildRepository) {
	//TODO: Block it behind local profile.. it should be only activated for local profile
	// mux.HandleFunc(executePipelineHttpMethod+" "+executePipelineUrl, handleExecutePipeline(repo))
	pipelineBuildExecutorService := service.NewPipelineBuildExecutorService(func(pbd dto.PipelineBuildDTO) {

	pb := mapper.PipelineBuildFromDTO(pbd)

	if err := repo.Save(pb); err != nil {
		log.Printf("failed to persist pipeline build: %v", err)
	}

	fmt.Println("-------------------------------")
	b, _ := json.MarshalIndent(pbd, "", "  ")
	fmt.Println("Build snapshot:", string(b))
	fmt.Println("-------------------------------")
	})

	pipelineService := service.NewPipelineBuildService(repo)
	pipelineBuildHandler := NewPipelineBuildHandler(pipelineService)

	mux.HandleFunc("GET /api/v1/pipeline/build/{pipelineBuildId}", pipelineBuildHandler.HandleGetPipelineBuild)
	mux.HandleFunc("GET /api/v1/pipeline/build/{$}", pipelineBuildHandler.HandleGetAllPipelineBuilds)
	mux.HandleFunc("DELETE /api/v1/pipeline/build/{pipelineBuildId}", pipelineBuildHandler.HandleDeletePipelineBuild)
}

