package dto

import "github.com/google/uuid"

type StageBuildDTO struct {
	Id uuid.UUID `json:"id"`

	StageId uuid.UUID `json:"stageId"`

	StageName string `json:"stageName"`

	StageOrder int `json:"stageOrder"`

	CurrentState StageExecutionState `json:"stageExecutionState"`
}

type PipelineBuildDTO struct {
	Id uuid.UUID `json:"id"`

	PipelineId uuid.UUID `json:"pipelineId"`

	PipelineName string `json:"pipelineName"`

	CurrentState PipelineExecutionState `json:"currentState"`

	StageBuilds []StageBuildDTO `json:"stageBuilds"`
}

type StageDTO struct {
	Id uuid.UUID `json:"id"`

	Name string `json:"name"`

	ScriptType string `json:"scriptType"`

	Command string `json:"command"`
}

type PipelineDTO struct {
	Id uuid.UUID `json:"id"`

	Name string `json:"name"`

	Stages []StageDTO `json:"stages"`
}
