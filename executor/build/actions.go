package build

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"net/http"

	"dagger.io/dagger"
	"github.com/AutoBuilders/AutoBuild/executor/dto"
	"github.com/AutoBuilders/AutoBuild/executor/mapper"
	"github.com/AutoBuilders/AutoBuild/executor/repository"
	"github.com/google/uuid"
)

// TODO: Don't hardcode dagger client.. abstract it behind interface
func Start(ctx context.Context, client *dagger.Client, pipelineDTO dto.PipelineDTO, repo *repository.PipelineBuildRepository) (dto.PipelineBuildDTO, bool) {
	container, pipelineBuildDTO, anyStageBuildFailed := initBuild(client, pipelineDTO, repo)

	// execute per stage in the container
	for i, stage := range pipelineDTO.Stages {
		stageBuild := &pipelineBuildDTO.StageBuilds[i]

		stageBuild.CurrentState = dto.StageRunning
		notify(pipelineBuildDTO, repo)

		fmt.Println("Executing Stage ", stage.Name)

		if anyStageBuildFailed {
			stageBuild.CurrentState = dto.StageStopped
			notify(pipelineBuildDTO, repo)
			continue
		}

		scriptPath := fmt.Sprintf("/tmp/script_%s.sh", stage.Name)
		container = container.WithNewFile(scriptPath, stage.Command, dagger.ContainerWithNewFileOpts{
			Permissions: 0755,
		}).WithExec([]string{"sh", scriptPath})

		output, err := container.Stdout(ctx)
		if err != nil {
			anyStageBuildFailed = true
			stageBuild.CurrentState = dto.StageFailed
			notify(pipelineBuildDTO, repo)

			log.Printf("Pipeline execution failed: %v", err)
			continue
		}

		fmt.Println("Stage build output", output)

		stageBuild.CurrentState = dto.StageSuccess
		notify(pipelineBuildDTO, repo)
	}

	if !anyStageBuildFailed {
		pipelineBuildDTO.CurrentState = dto.PipelineSuccess
	} else {
		pipelineBuildDTO.CurrentState = dto.PipelineFailed
	}

	notify(pipelineBuildDTO, repo)

	return pipelineBuildDTO, anyStageBuildFailed
}

func initBuild(client *dagger.Client, pipelineDTO dto.PipelineDTO, repo *repository.PipelineBuildRepository) (*dagger.Container, dto.PipelineBuildDTO, bool) {
	container := client.Container().From("alpine").WithWorkdir("/src")

	pipelineBuildDTO := initializePipelineBuild(pipelineDTO)
	notify(pipelineBuildDTO, repo)

	pipelineBuildDTO.CurrentState = dto.PipelineRunning
	notify(pipelineBuildDTO, repo)

	anyStageBuildFailed := false
	return container, pipelineBuildDTO, anyStageBuildFailed
}

func ParsePayload(r *http.Request, w http.ResponseWriter) (dto.PipelineDTO, bool) {
	var pipelineDTO dto.PipelineDTO

	err := json.NewDecoder(r.Body).Decode(&pipelineDTO)
	if err != nil {
		http.Error(w, "Invalid JSON payload: "+err.Error(), http.StatusBadRequest)
		return dto.PipelineDTO{}, true
	}

	w.Header().Set("Content-Type", "application/json")
	return pipelineDTO, false
}

func initializePipelineBuild(pipelineDTO dto.PipelineDTO) dto.PipelineBuildDTO {
	pipelineBuildId, _ := uuid.NewV7()
	pipelineBuildDTO := dto.PipelineBuildDTO{
		Id:           pipelineBuildId,
		PipelineId:   pipelineDTO.Id,
		PipelineName: pipelineDTO.Name,
		CurrentState: dto.PipelineWaiting,
		StageBuilds:  []dto.StageBuildDTO{},
	}

	for _, stageDTO := range pipelineDTO.Stages {
		stageBuildId, _ := uuid.NewV7()
		stageBuildDTO := dto.StageBuildDTO{
			Id:           stageBuildId,
			StageId:      stageDTO.Id,
			StageName:    stageDTO.Name,
			StageOrder:   stageDTO.StageOrder,
			CurrentState: dto.StageWaiting,
		}

		pipelineBuildDTO.StageBuilds = append(pipelineBuildDTO.StageBuilds, stageBuildDTO)
	}

	b, _ := json.MarshalIndent(pipelineBuildDTO, "", "  ")
	fmt.Println("init build dto:", string(b))
	return pipelineBuildDTO
}

// notify persists the current state of the pipeline build to the database and logs a snapshot.
func notify(pipelineBuildDTO dto.PipelineBuildDTO, repo *repository.PipelineBuildRepository) {
	pb := mapper.PipelineBuildFromDTO(pipelineBuildDTO)

	if err := repo.Save(pb); err != nil {
		log.Printf("failed to persist pipeline build: %v", err)
	}

	fmt.Println("-------------------------------")
	b, _ := json.MarshalIndent(pipelineBuildDTO, "", "  ")
	fmt.Println("Build snapshot:", string(b))
	fmt.Println("-------------------------------")
}

