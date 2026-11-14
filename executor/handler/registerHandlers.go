package handler

import (
	"net/http"

	"github.com/AutoBuilders/AutoBuild/executor/repository"
)

func RegisterHandlers(mux *http.ServeMux, repo *repository.PipelineBuildRepository) {
	mux.HandleFunc(executePipelineHttpMethod+" "+executePipelineUrl, handleExecutePipeline(repo))

	pipelineBuildHandler := NewPipelineBuildHandler(repo)

	mux.HandleFunc("GET /api/v1/pipeline/build/{pipelineBuildId}", pipelineBuildHandler.HandleGetPipelineBuild)
	mux.HandleFunc("GET /api/v1/pipeline/build/{$}", pipelineBuildHandler.HandleGetAllPipelineBuilds)
	mux.HandleFunc("DELETE /api/v1/pipeline/build/{pipelineBuildId}", pipelineBuildHandler.HandleDeletePipelineBuild)
}

