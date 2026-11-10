package mapper

import (
	"github.com/AutoBuilders/AutoBuild/executor/dto"
	"github.com/AutoBuilders/AutoBuild/executor/entity"
)

func StageBuildToDTO(sb entity.StageBuild, stageName string) dto.StageBuildDTO {
	return dto.StageBuildDTO{
		Id:           sb.ID,
		StageId:      sb.StageID,
		StageName:    sb.StageName,
		StageOrder:   sb.StageOrder,
		CurrentState: dto.StageExecutionState(sb.CurrentState),
	}
}

func StageBuildFromDTO(d dto.StageBuildDTO) entity.StageBuild {
	return entity.StageBuild{
		ID:           d.Id,
		StageID:      d.StageId,
		StageName:    d.StageName,
		StageOrder:   d.StageOrder,
		CurrentState: entity.StageExecutionState(d.CurrentState),
	}
}

func PipelineBuildToDTO(pb entity.PipelineBuild, pipelineName string, stageNameByID map[string]string) dto.PipelineBuildDTO {
	stageBuilds := make([]dto.StageBuildDTO, len(pb.StageBuilds))
	for i, sb := range pb.StageBuilds {
		stageBuilds[i] = StageBuildToDTO(sb, "")
	}
	return dto.PipelineBuildDTO{
		Id:           pb.ID,
		PipelineId:   pb.PipelineID,
		PipelineName: pb.PipelineName,
		CurrentState: dto.PipelineExecutionState(pb.CurrentState),
		StageBuilds:  stageBuilds,
	}
}

func PipelineBuildFromDTO(d dto.PipelineBuildDTO) entity.PipelineBuild {
	stageBuilds := make([]entity.StageBuild, len(d.StageBuilds))
	for i, sb := range d.StageBuilds {
		stageBuilds[i] = StageBuildFromDTO(sb)
	}
	return entity.PipelineBuild{
		ID:           d.Id,
		PipelineID:   d.PipelineId,
		PipelineName: d.PipelineName,
		CurrentState: entity.PipelineExecutionState(d.CurrentState),
		StageBuilds:  stageBuilds,
	}
}
