package handler

import (
	"net/http"

	"github.com/AutoBuilders/AutoBuild/executor/repository"
)

func RegisterHandlers(mux *http.ServeMux, repo *repository.PipelineBuildRepository) {
	mux.HandleFunc(executePipelineHttpMethod+" "+executePipelineUrl, handleExecutePipeline(repo))

	mux.HandleFunc(getPipelineBuildHttpMethod+" "+getPipelineBuildUrl, handleGetPipelineBuild(repo))
	mux.HandleFunc(getAllPipelineBuildsHttpMethod+" "+getAllPipelineBuildsUrl, handleGetAllPipelineBuilds(repo))
	mux.HandleFunc(deletePipelineBuildHttpMethod+" "+deletePipelineBuildUrl, handleDeletePipelineBuild(repo))
}

