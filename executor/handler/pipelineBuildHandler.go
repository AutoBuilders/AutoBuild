package handler

import (
	"encoding/json"
	"errors"
	"net/http"

	customerror "github.com/AutoBuilders/AutoBuild/executor/error"
	"github.com/AutoBuilders/AutoBuild/executor/dto"
	"github.com/google/uuid"
)

// TODO: extract into service layer

type PipelineBuildService interface {
	GetPipelineBuild(id uuid.UUID) (*dto.PipelineBuildDTO, error)
	GetAllPipelineBuilds() (*[]dto.PipelineBuildDTO, error)
	DeletePipelineBuild(id uuid.UUID) error 
}

type PipelineBuildHandler struct {
	srv PipelineBuildService
}

func NewPipelineBuildHandler(srv PipelineBuildService) *PipelineBuildHandler {
	pipelineBuildHandler := &PipelineBuildHandler{
		srv: srv,
	}

	return pipelineBuildHandler
}

func (pbh *PipelineBuildHandler) HandleGetPipelineBuild(w http.ResponseWriter, r *http.Request) {
	id, err := uuid.Parse(r.PathValue("pipelineBuildId"))
	if err != nil {
		http.Error(w, `{"error": "invalid pipeline build id"}`, http.StatusBadRequest)
		return
	}

	pb, err := pbh.srv.GetPipelineBuild(id)
	w.Header().Set("Content-Type", "application/json")

	if err != nil {
		var nfe *customerror.NotFoundError
		if errors.As(err, &nfe) {
			http.Error(w, `{"error": "` + nfe.Message + `" }`, http.StatusNotFound)
		} else {
			http.Error(w, `{"error": "internal server error"}`, http.StatusInternalServerError)
		}
		return
	}

	json.NewEncoder(w).Encode(pb)
}

func (pbh *PipelineBuildHandler) HandleGetAllPipelineBuilds(w http.ResponseWriter, r *http.Request) {
	pbs, err := pbh.srv.GetAllPipelineBuilds()
	if err != nil {
		http.Error(w, `{"error": "internal server error"}`, http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(pbs)
}

func (pbh *PipelineBuildHandler) HandleDeletePipelineBuild(w http.ResponseWriter, r *http.Request) {
	id, err := uuid.Parse(r.PathValue("pipelineBuildId"))
	if err != nil {
		http.Error(w, `{"error": "invalid pipeline build id"}`, http.StatusBadRequest)
		return
	}

	if err := pbh.srv.DeletePipelineBuild(id); err != nil {
		var nfe *customerror.NotFoundError
		if errors.As(err, &nfe) {
			http.Error(w, `{"error": "` + nfe.Message + `" }`, http.StatusNotFound)
		} else {
			http.Error(w, `{"error": "internal server error"}`, http.StatusInternalServerError)
		}
	}

	w.WriteHeader(http.StatusNoContent)
}
