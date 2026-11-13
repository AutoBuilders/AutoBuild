package repository

import (
	"log"

	"github.com/AutoBuilders/AutoBuild/executor/entity"
	"github.com/google/uuid"
	"gorm.io/driver/sqlite"
	"gorm.io/gorm"
)

type PipelineBuildRepository struct {
	db *gorm.DB
}

func NewPipelineBuildRepository(dsn string) *PipelineBuildRepository {
	db := initDB(dsn)
	return &PipelineBuildRepository{db: db}
}

func initDB(dsn string) *gorm.DB {
	db, err := gorm.Open(sqlite.Open(dsn), &gorm.Config{})
	if err != nil {
		log.Fatalf("failed to connect to database: %v", err)
	}

	if err := db.AutoMigrate(&entity.StageBuild{}, &entity.PipelineBuild{}); err != nil {
		log.Fatalf("failed to auto-migrate: %v", err)
	}

	return db
}

func (r *PipelineBuildRepository) Save(pb entity.PipelineBuild) error {
	return r.db.Session(&gorm.Session{FullSaveAssociations: true}).Save(&pb).Error
}

func (r *PipelineBuildRepository) FindByID(id uuid.UUID) (entity.PipelineBuild, error) {
	var pb entity.PipelineBuild
	err := r.db.Preload("StageBuilds").First(&pb, "id = ?", id).Error
	return pb, err
}

func (r *PipelineBuildRepository) FindAll() ([]entity.PipelineBuild, error) {
	var pbs []entity.PipelineBuild
	err := r.db.Preload("StageBuilds").Find(&pbs).Error
	return pbs, err
}

func (r *PipelineBuildRepository) Delete(id uuid.UUID) error {
	return r.db.Delete(&entity.PipelineBuild{}, "id = ?", id).Error
}
