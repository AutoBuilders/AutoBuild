package service

import (
	"errors"

	"github.com/google/uuid"

	"github.com/AutoBuilders/AutoBuild/executor/dto"
	customerror "github.com/AutoBuilders/AutoBuild/executor/error"
	"github.com/AutoBuilders/AutoBuild/executor/mapper"
	"github.com/AutoBuilders/AutoBuild/executor/repository"
	"gorm.io/gorm"
)

type PipelineBuildService struct {
	db *repository.PipelineBuildRepository
}

func NewPipelineBuildService(db *repository.PipelineBuildRepository) *PipelineBuildService {
	return &PipelineBuildService{
		db: db,
	}
}

func (s *PipelineBuildService) GetPipelineBuild(id uuid.UUID) (*dto.PipelineBuildDTO, error) {
	pb, err := s.db.FindByID(id)

	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, customerror.NewNotFoundError("PipelineBuild with id " + id.String() + " not found")
		}

		return nil, errors.New("Unexpected error ocurred finding PipelineBuild with Id " + id.String())
	}

	pbDto := mapper.PipelineBuildToDTO(pb)
	return &pbDto, nil
}

func (s *PipelineBuildService) GetAllPipelineBuilds() (*[]dto.PipelineBuildDTO, error) {
	pbs, err := s.db.FindAll()

	if err != nil {
		return nil, errors.New("Unexpected error ocurred finding PipelineBuilds")
	}

	dtos := make([]dto.PipelineBuildDTO, len(pbs))
	for i, pb := range pbs {
		dtos[i] = mapper.PipelineBuildToDTO(pb)
	}

	return &dtos, nil
}

func (s *PipelineBuildService) DeletePipelineBuild(id uuid.UUID) error {
	_, err := s.GetPipelineBuild(id)

	if err != nil {
		return err
	}

	err = s.db.Delete(id)

	if err != nil {
		return errors.New("Unexpected error ocurred finding PipelineBuilds")
	}

	return nil
}
