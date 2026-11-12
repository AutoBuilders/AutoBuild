package com.autobuild.pipeline.executor.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autobuild.pipeline.executor.entity.PipelineBuild;

/**
 * Repository layer for PipelineBuild.
 */

@Repository
public interface PipelineBuildRepository extends JpaRepository<PipelineBuild, UUID>{

}
