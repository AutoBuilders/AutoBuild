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

const getPipelineBuildHttpMethod = "GET"
const getPipelineBuildUrl = "/api/v1/pipeline/build/{pipelineBuildId}"

const getAllPipelineBuildsHttpMethod = "GET"
const getAllPipelineBuildsUrl = "/api/v1/pipeline/build/{$}"

const deletePipelineBuildHttpMethod = "DELETE"
const deletePipelineBuildUrl = "/api/v1/pipeline/build/{pipelineBuildId}"

func handleGetPipelineBuild(repo *repository.PipelineBuildRepository) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		id, err := uuid.Parse(r.PathValue("pipelineBuildId"))
		if err != nil {
			http.Error(w, `{"error": "invalid pipeline build id"}`, http.StatusBadRequest)
			return
		}

		pb, err := repo.FindByID(id)
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
}

func handleGetAllPipelineBuilds(repo *repository.PipelineBuildRepository) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		pbs, err := repo.FindAll()
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
}

func handleDeletePipelineBuild(repo *repository.PipelineBuildRepository) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		id, err := uuid.Parse(r.PathValue("pipelineBuildId"))
		if err != nil {
			http.Error(w, `{"error": "invalid pipeline build id"}`, http.StatusBadRequest)
			return
		}

		if err := repo.Delete(id); err != nil {
			http.Error(w, `{"error": "internal server error"}`, http.StatusInternalServerError)
			return
		}

		w.WriteHeader(http.StatusNoContent)
	}
}
