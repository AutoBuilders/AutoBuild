package handler

import (
	"encoding/json"
	"errors"
	"net/http"

	"github.com/AutoBuilders/AutoBuild/executor/mapper"
	"github.com/AutoBuilders/AutoBuild/executor/repository"
	"github.com/google/uuid"
	"gorm.io/gorm"
)

// TODO: extract into service layer

type PipelineBuildHandler struct {
	db *repository.PipelineBuildRepository
}

func NewPipelineBuildHandler(db *repository.PipelineBuildRepository) *PipelineBuildHandler {
	pipelineBuildHandler := &PipelineBuildHandler{
		db: db,
	}

	return pipelineBuildHandler
}

func (pbh *PipelineBuildHandler) HandleGetPipelineBuild(w http.ResponseWriter, r *http.Request) {
	id, err := uuid.Parse(r.PathValue("pipelineBuildId"))
	if err != nil {
		http.Error(w, `{"error": "invalid pipeline build id"}`, http.StatusBadRequest)
		return
	}

	pb, err := pbh.db.FindByID(id)
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			http.Error(w, `{"error": "pipeline build not found"}`, http.StatusNotFound)
		} else {
			http.Error(w, `{"error": "internal server error"}`, http.StatusInternalServerError)
		}
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(mapper.PipelineBuildToDTO(pb, "", nil))
}

func (pbh *PipelineBuildHandler) HandleGetAllPipelineBuilds(w http.ResponseWriter, r *http.Request) {
	pbs, err := pbh.db.FindAll()
	if err != nil {
		http.Error(w, `{"error": "internal server error"}`, http.StatusInternalServerError)
		return
	}

	dtos := make([]interface{}, len(pbs))
	for i, pb := range pbs {
		dtos[i] = mapper.PipelineBuildToDTO(pb, "", map[string]string{})
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(dtos)
}

func (pbh *PipelineBuildHandler) HandleDeletePipelineBuild(w http.ResponseWriter, r *http.Request) {
	id, err := uuid.Parse(r.PathValue("pipelineBuildId"))
	if err != nil {
		http.Error(w, `{"error": "invalid pipeline build id"}`, http.StatusBadRequest)
		return
	}

	if err := pbh.db.Delete(id); err != nil {
		http.Error(w, `{"error": "internal server error"}`, http.StatusInternalServerError)
		return
	}

	w.WriteHeader(http.StatusNoContent)
}
