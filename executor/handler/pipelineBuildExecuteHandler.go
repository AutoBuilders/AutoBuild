package handler

import (
	"context"
	"encoding/json"
	"fmt"
	"net/http"

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

func (h *PipelineBuildExecuteHandler) HandleExecutePipeline(w http.ResponseWriter, r *http.Request) {
	pipelineDTO, shouldReturn := parsePayload(r, w)
	if shouldReturn {
		return
	}

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

func parsePayload(r *http.Request, w http.ResponseWriter) (dto.PipelineDTO, bool) {
	var pipelineDTO dto.PipelineDTO

	err := json.NewDecoder(r.Body).Decode(&pipelineDTO)
	if err != nil {
		http.Error(w, "Invalid JSON payload: "+err.Error(), http.StatusBadRequest)
		return dto.PipelineDTO{}, true
	}

	w.Header().Set("Content-Type", "application/json")
	return pipelineDTO, false
}
