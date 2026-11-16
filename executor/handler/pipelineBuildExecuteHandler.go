package handler

import (
	"context"
	"encoding/json"
	"fmt"
	"net/http"

	"github.com/AutoBuilders/AutoBuild/executor/build"
	"github.com/AutoBuilders/AutoBuild/executor/dto"
	"github.com/AutoBuilders/AutoBuild/executor/service"
)

type PipelineBuildExecutorService interface {
	StartBuild(buildCtx context.Context, pipelineDTO dto.PipelineDTO) (dto.PipelineBuildDTO, bool)
}

type PipelineBuildExecuteHandler struct {
	srv PipelineBuildExecutorService
}

func NewPipelineExecuteHandler(pipelineBuildExecutorService *service.PipelineBuildExecutorService) *PipelineBuildExecuteHandler {
	return &PipelineBuildExecuteHandler{
		srv: pipelineBuildExecutorService,
	}
}

func (h *PipelineBuildExecuteHandler) HandleExecutePipeline(w http.ResponseWriter, r *http.Request)  {
		pipelineDTO, shouldReturn := build.ParsePayload(r, w)
		if shouldReturn {
			return
		}

		// // TODO: abstract it behind a runner
		// ctx, client := runner.SetupDaggerClient()
		// defer client.Close()

		fmt.Println("Executing Pipeline ", pipelineDTO.Name)

		pipelineBuildDTO, anyStageBuildFailed := h.srv.StartBuild(context.Background(), pipelineDTO)

		if anyStageBuildFailed {
			pipelineBuildDTO.CurrentState = dto.PipelineFailed
			http.Error(w, `{"error": "pipeline failed"}`, http.StatusInternalServerError)
		} else {
			pipelineBuildDTO.CurrentState = dto.PipelineSuccess
			json.NewEncoder(w).Encode(pipelineBuildDTO)
		}
}
