package com.autobuild.pipeline.executor.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autobuild.pipeline.executor.entity.StageBuildEntity;

/**
 * Repository for Stage Builds.
 * 
 * @author Suvabrata Chowdhury
 */

@Repository
public interface StageBuildRepository extends JpaRepository<StageBuildEntity, UUID>{

}
