package handler

import (
	"encoding/json"
	"fmt"
	"net/http"

	"github.com/AutoBuilders/AutoBuild/executor/build"
	"github.com/AutoBuilders/AutoBuild/executor/dto"
	"github.com/AutoBuilders/AutoBuild/executor/repository"
	"github.com/AutoBuilders/AutoBuild/executor/runner"
)

const executePipelineHttpMethod = "POST"
const executePipelineUrl = "/api/v1/execute/pipeline"

func handleExecutePipeline(repo *repository.PipelineBuildRepository) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		pipelineDTO, shouldReturn := build.ParsePayload(r, w)
		if shouldReturn {
			return
		}

		// TODO: abstract it behind a runner
		ctx, client := runner.SetupDaggerClient()
		defer client.Close()

		fmt.Println("Executing Pipeline ", pipelineDTO.Name)

		pipelineBuildDTO, anyStageBuildFailed := build.Start(ctx, client, pipelineDTO, repo)

		if anyStageBuildFailed {
			pipelineBuildDTO.CurrentState = dto.PipelineFailed
			http.Error(w, `{"error": "pipeline failed"}`, http.StatusInternalServerError)
		} else {
			pipelineBuildDTO.CurrentState = dto.PipelineSuccess
			json.NewEncoder(w).Encode(pipelineBuildDTO)
		}
	}
}
