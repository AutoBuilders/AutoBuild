package service

import (
	"context"

	"github.com/AutoBuilders/AutoBuild/executor/dto"
)

type Runner interface {
	StartBuild(buildCtx context.Context, pipelineDTO dto.PipelineDTO, notify func(dto.PipelineBuildDTO)) (dto.PipelineBuildDTO, bool)
}

type PipelineBuildExecutorService struct {
	notify func(dto.PipelineBuildDTO)
	runner Runner
}

func NewPipelineBuildExecutorService(notify func(dto.PipelineBuildDTO), runnerClient Runner) *PipelineBuildExecutorService {
	return &PipelineBuildExecutorService{
		notify:       notify,
		runner: runnerClient,
	}
}

// TODO: error handling
func (es *PipelineBuildExecutorService) StartBuild(buildCtx context.Context, pipelineDTO dto.PipelineDTO) (dto.PipelineBuildDTO, bool) {
	// TODO: Instead of returning boolean try throwing error and handling it which will make it explicit
	return es.runner.StartBuild(buildCtx, pipelineDTO, es.notify)
}
