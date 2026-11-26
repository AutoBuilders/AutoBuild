package runner

import (
	"context"
	"encoding/json"
	"fmt"
	"log"

	"dagger.io/dagger"

	"github.com/AutoBuilders/AutoBuild/executor/dto"
	"github.com/google/uuid"
)

type DaggerRunner struct {
	client *dagger.Client
}

func NewDaggerRunner(client *dagger.Client) *DaggerRunner {
	return &DaggerRunner{
		client: client,
	}
}

func (dr *DaggerRunner) StartBuild(buildCtx context.Context, pipelineDTO dto.PipelineDTO, notify func(dto.PipelineBuildDTO)) (dto.PipelineBuildDTO, bool) {
	container, pipelineBuildDTO, anyStageBuildFailed := dr.initBuild(dr.client, pipelineDTO, notify)

	// execute per stage in the container
	for i, stage := range pipelineDTO.Stages {
		stageBuild := &pipelineBuildDTO.StageBuilds[i]

		stageBuild.CurrentState = dto.StageRunning
		notify(pipelineBuildDTO)

		fmt.Println("Executing Stage ", stage.Name)

		if anyStageBuildFailed {
			stageBuild.CurrentState = dto.StageStopped
			notify(pipelineBuildDTO)
			continue
		}

		scriptPath := fmt.Sprintf("/tmp/script_%s.sh", stage.Name)
		container = container.WithNewFile(scriptPath, stage.Command, dagger.ContainerWithNewFileOpts{
			Permissions: 0755,
		}).WithExec([]string{"sh", scriptPath})

		output, err := container.Stdout(buildCtx)
		if err != nil {
			anyStageBuildFailed = true
			stageBuild.CurrentState = dto.StageFailed
			notify(pipelineBuildDTO)

			log.Printf("Pipeline execution failed: %v", err)
			continue
		}

		fmt.Println("Stage build output", output)

		stageBuild.CurrentState = dto.StageSuccess
		notify(pipelineBuildDTO)
	}

	if !anyStageBuildFailed {
		pipelineBuildDTO.CurrentState = dto.PipelineSuccess
	} else {
		pipelineBuildDTO.CurrentState = dto.PipelineFailed
	}

	notify(pipelineBuildDTO)

	return pipelineBuildDTO, anyStageBuildFailed
}

func (dr *DaggerRunner) initBuild(client *dagger.Client, pipelineDTO dto.PipelineDTO, notify func(dto.PipelineBuildDTO)) (*dagger.Container, dto.PipelineBuildDTO, bool) {
	container := client.Container().From("alpine").WithWorkdir("/src")

	pipelineBuildDTO := initializePipelineBuild(pipelineDTO)
	notify(pipelineBuildDTO)

	pipelineBuildDTO.CurrentState = dto.PipelineRunning
	notify(pipelineBuildDTO)

	anyStageBuildFailed := false
	return container, pipelineBuildDTO, anyStageBuildFailed
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
