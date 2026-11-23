package com.autobuild.pipeline.executor.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autobuild.pipeline.executor.entity.PipelineBuildEntity;

/**
 * Repository layer for PipelineBuild.
 * 
 * @author Suvabrata Chowdhury
 */

@Repository
public interface PipelineBuildRepository extends JpaRepository<PipelineBuildEntity, UUID>{

}
