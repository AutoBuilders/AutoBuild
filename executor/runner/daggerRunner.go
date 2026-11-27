package runner

import (
	"context"
	"fmt"
	"os"
	"path/filepath"

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
	container, pipelineBuildDTO, anyStageBuildFailed, err := dr.initBuild(dr.client, pipelineDTO, notify)
	if err != nil {
		pipelineBuildDTO.CurrentState = dto.PipelineFailed
		notify(pipelineBuildDTO)
		return pipelineBuildDTO, true
	}

	// execute per stage in the container
	for i, stage := range pipelineDTO.Stages {
		stageBuild := &pipelineBuildDTO.StageBuilds[i]

		stageBuild.CurrentState = dto.StageRunning
		notify(pipelineBuildDTO)

		if anyStageBuildFailed {
			stageBuild.CurrentState = dto.StageStopped
			notify(pipelineBuildDTO)
			continue
		}

		scriptPath := fmt.Sprintf("/tmp/script_%s.sh", stage.Name)
		container = container.WithNewFile(scriptPath, stage.Command, dagger.ContainerWithNewFileOpts{
			Permissions: 0755,
		}).WithExec([]string{"sh", scriptPath})

		stdout, stdoutErr := container.Stdout(buildCtx)
		stderr, stderrErr := container.Stderr(buildCtx)

		logFile, fileErr := os.Create(stageBuild.LogPath)
		if fileErr == nil {
			if stdout != "" {
				fmt.Fprint(logFile, stdout)
			}
			if stderr != "" {
				fmt.Fprint(logFile, stderr)
			}
			logFile.Close()
		}

		if stdoutErr != nil || stderrErr != nil {
			anyStageBuildFailed = true
			stageBuild.CurrentState = dto.StageFailed
			notify(pipelineBuildDTO)
			continue
		}

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

func (dr *DaggerRunner) initBuild(client *dagger.Client, pipelineDTO dto.PipelineDTO, notify func(dto.PipelineBuildDTO)) (*dagger.Container, dto.PipelineBuildDTO, bool, error) {
	container := client.Container().From("alpine").WithWorkdir("/src")

	pipelineBuildDTO := initializePipelineBuild(pipelineDTO)
	notify(pipelineBuildDTO)

	logDir := filepath.Join("..", "pipeline_build_logs", pipelineBuildDTO.Id.String())
	if err := os.MkdirAll(logDir, 0755); err != nil {
		return nil, pipelineBuildDTO, false, fmt.Errorf("failed to create log directory: %w", err)
	}

	for i := range pipelineBuildDTO.StageBuilds {
		stageBuild := &pipelineBuildDTO.StageBuilds[i]
		stageBuild.LogPath = filepath.Join(logDir, stageBuild.Id.String()+".log")
	}

	pipelineBuildDTO.CurrentState = dto.PipelineRunning
	notify(pipelineBuildDTO)

	return container, pipelineBuildDTO, false, nil
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

	return pipelineBuildDTO
}
