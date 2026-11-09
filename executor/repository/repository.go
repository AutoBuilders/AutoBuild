package repository

import (
	"log"

	"github.com/AutoBuilders/AutoBuild/executor/entity"
	"gorm.io/driver/sqlite"
	"gorm.io/gorm"
)

type PipelineBuildRepository struct {
	db *gorm.DB
}

func NewPipelineBuildRepository(db *gorm.DB) *PipelineBuildRepository {
	return &PipelineBuildRepository{db: db}
}

func InitDB(dsn string) *gorm.DB {
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
