package entity

import (
	"github.com/google/uuid"
	"gorm.io/gorm"
)

type PipelineExecutionState string

const (
	PipelineWaiting PipelineExecutionState = "WAITING"
	PipelineRunning PipelineExecutionState = "RUNNING"
	PipelineSuccess PipelineExecutionState = "SUCCESS"
	PipelineFailed  PipelineExecutionState = "FAILED"
)

type StageExecutionState string

const (
	StageWaiting StageExecutionState = "WAITING"
	StageRunning StageExecutionState = "RUNNING"
	StageSuccess StageExecutionState = "SUCCESS"
	StageFailed  StageExecutionState = "FAILED"
	StageStopped StageExecutionState = "STOPPED"
)

type StageBuild struct {
	ID           uuid.UUID           `gorm:"type:uuid;primaryKey"`
	StageID      uuid.UUID           `gorm:"type:uuid;not null;column:stage_id"`
	CurrentState StageExecutionState `gorm:"type:varchar(20);not null;column:stage_build_current_state;default:WAITING"`
	StageOrder   int                 `gorm:"not null;column:stage_order"`
	LogPath      string              `gorm:"not null;column:log_path"`
}

func (s *StageBuild) BeforeCreate(tx *gorm.DB) error {
	if s.ID == uuid.Nil {
		s.ID = uuid.New()
	}
	if s.CurrentState == "" {
		s.CurrentState = StageWaiting
	}
	return nil
}

type PipelineBuild struct {
	ID           uuid.UUID              `gorm:"type:uuid;primaryKey"`
	PipelineID   uuid.UUID              `gorm:"type:uuid;not null;column:pipeline_id"`
	StageBuilds  []StageBuild           `gorm:"many2many:pipeline_stage_builds;joinForeignKey:PipelineBuildID;joinReferences:StageBuildID"`
	CurrentState PipelineExecutionState `gorm:"type:varchar(20);not null;column:pipeline_build_current_state;default:WAITING"`
}

func (p *PipelineBuild) BeforeCreate(tx *gorm.DB) error {
	if p.ID == uuid.Nil {
		p.ID = uuid.New()
	}
	if p.CurrentState == "" {
		p.CurrentState = PipelineWaiting
	}
	return nil
}
