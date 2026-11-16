package service

import (
	"context"
	"encoding/json"
	"fmt"
	"log"

	"github.com/google/uuid"

	"dagger.io/dagger"
	"github.com/AutoBuilders/AutoBuild/executor/dto"
)

type PipelineBuildExecutorService struct {
	notify       func(dto.PipelineBuildDTO)
	runnerClient *dagger.Client
}

func NewPipelineBuildExecutorService(notify func(dto.PipelineBuildDTO), runnerClient *dagger.Client) *PipelineBuildExecutorService {
	return &PipelineBuildExecutorService{
		notify:       notify,
		runnerClient: runnerClient,
	}
}

// TODO: error handling
func (es *PipelineBuildExecutorService) StartBuild(buildCtx context.Context, pipelineDTO dto.PipelineDTO) (dto.PipelineBuildDTO, bool) {
	// TODO: Instead of returning boolean try throwing error and handling it which will make it explicit
	container, pipelineBuildDTO, anyStageBuildFailed := es.initBuild(es.runnerClient, pipelineDTO)

	// execute per stage in the container
	for i, stage := range pipelineDTO.Stages {
		stageBuild := &pipelineBuildDTO.StageBuilds[i]

		stageBuild.CurrentState = dto.StageRunning
		es.notify(pipelineBuildDTO)

		fmt.Println("Executing Stage ", stage.Name)

		if anyStageBuildFailed {
			stageBuild.CurrentState = dto.StageStopped
			es.notify(pipelineBuildDTO)
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
			es.notify(pipelineBuildDTO)

			log.Printf("Pipeline execution failed: %v", err)
			continue
		}

		fmt.Println("Stage build output", output)

		stageBuild.CurrentState = dto.StageSuccess
		es.notify(pipelineBuildDTO)
	}

	if !anyStageBuildFailed {
		pipelineBuildDTO.CurrentState = dto.PipelineSuccess
	} else {
		pipelineBuildDTO.CurrentState = dto.PipelineFailed
	}

	es.notify(pipelineBuildDTO)

	return pipelineBuildDTO, anyStageBuildFailed
}

func (es *PipelineBuildExecutorService) initBuild(client *dagger.Client, pipelineDTO dto.PipelineDTO) (*dagger.Container, dto.PipelineBuildDTO, bool) {
	container := client.Container().From("alpine").WithWorkdir("/src")

	pipelineBuildDTO := initializePipelineBuild(pipelineDTO)
	es.notify(pipelineBuildDTO)

	pipelineBuildDTO.CurrentState = dto.PipelineRunning
	es.notify(pipelineBuildDTO)

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
