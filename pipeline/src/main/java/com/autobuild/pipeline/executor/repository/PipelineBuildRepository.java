package com.autobuild.pipeline.executor.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.autobuild.pipeline.executor.entity.PipelineBuild;

/**
 * Repository layer for PipelineBuild.
 *
 * @author Suvabrata Chowdhury
 */

@Repository
public interface PipelineBuildRepository extends JpaRepository<PipelineBuild, UUID> {

    /**
     * Finds a pipeline build by ID with stage builds eagerly fetched.
     *
     * @param id the pipeline build UUID
     * @return an Optional containing the build with stage builds loaded
     */
    @Query("SELECT pb FROM PipelineBuild pb JOIN FETCH pb.stageBuilds WHERE pb.id = :id")
    Optional<PipelineBuild> findByIdWithStageBuilds(@Param("id") UUID id);
}

