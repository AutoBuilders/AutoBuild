package build

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"uuid"

	"dagger.io/dagger"
	"github.com/AutoBuilders/AutoBuild/executor/dto"
)

// TODO: Don't hardcode dagger client.. abstract it behind interface
func Start(ctx context.Context, client *dagger.Client, pipelineDTO dto.PipelineDTO) (dto.PipelineBuildDTO, bool) {
	container, pipelineBuildDTO, anyStageBuildFailed := initBuild(client, pipelineDTO)

	// execute per stage in the container
	for i, stage := range pipelineDTO.Stages {
		stageBuild := &pipelineBuildDTO.StageBuilds[i]
		Notify(pipelineBuildDTO)

		stageBuild.CurrentState = dto.StageRunning
		Notify(pipelineBuildDTO)

		fmt.Println("Executing Stage ", stage.Name)

		if anyStageBuildFailed {
			stageBuild.CurrentState = dto.StageStopped
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

			log.Printf("Pipeline execution failed: %v", err)
			continue
		}

		fmt.Println("Stage build output", output)

		stageBuild.CurrentState = dto.StageSuccess
	}

	if !anyStageBuildFailed {
		pipelineBuildDTO.CurrentState = dto.PipelineSuccess
	} else {
		pipelineBuildDTO.CurrentState = dto.PipelineFailed
	}

	Notify(pipelineBuildDTO)

	return pipelineBuildDTO, anyStageBuildFailed
}

func initBuild(client *dagger.Client, pipelineDTO dto.PipelineDTO) (*dagger.Container, dto.PipelineBuildDTO, bool) {
	container := client.Container().From("alpine").WithWorkdir("/src")

	pipelineBuildDTO := initializePipelineBuild(pipelineDTO)
	pipelineBuildDTO.CurrentState = dto.PipelineRunning

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
	pipelineBuildDTO := dto.PipelineBuildDTO{
		Id:           uuid.NewV7(),
		PipelineId:   pipelineDTO.Id,
		CurrentState: dto.PipelineWaiting,
		StageBuilds:  []dto.StageBuildDTO{},
	}

	for _, stageDTO := range pipelineDTO.Stages {
		stageBuildDTO := dto.StageBuildDTO{
			Id:           uuid.NewV7(),
			StageId:      stageDTO.Id,
			CurrentState: dto.StageWaiting,
		}

		pipelineBuildDTO.StageBuilds = append(pipelineBuildDTO.StageBuilds, stageBuildDTO)
	}

	b, _ := json.MarshalIndent(pipelineBuildDTO, "", "  ")
	fmt.Println("init build dto:", string(b))
	return pipelineBuildDTO
}

func Notify(pipelineBuildDTO dto.PipelineBuildDTO) {
	fmt.Println("-------------------------------")
	b, _ := json.MarshalIndent(pipelineBuildDTO, "", "  ")
	fmt.Println("Build snapshot:", string(b))
	fmt.Println("-------------------------------")
}
