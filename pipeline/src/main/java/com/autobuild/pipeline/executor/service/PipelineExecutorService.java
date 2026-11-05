package com.autobuild.pipeline.executor.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.autobuild.pipeline.definiton.entity.PipelineEntity;
import com.autobuild.pipeline.executor.dto.PipelineBuild;
import com.autobuild.pipeline.definiton.repository.PipelineRepository;
import com.autobuild.pipeline.executor.dto.PipelineExecuteRequest;
import com.autobuild.pipeline.executor.dto.mapper.PipelineBuildMapper;
import com.autobuild.pipeline.executor.entity.PipelineBuildEntity;
import com.autobuild.pipeline.executor.entity.StageBuildEntity;
import com.autobuild.pipeline.executor.job.PipelineExecutor;
import com.autobuild.pipeline.executor.repository.PipelineBuildRepository;
import com.autobuild.pipeline.utility.file.PipelineFileService;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

/**
 * Service layer for executing a pipeline.
 * 
 * @author Suvabrata Chowdhury
 */

//TODO: too much dependencies. refactor

@Slf4j
@Service
public class PipelineExecutorService {

    @Autowired
    private PipelineRepository pipelineRepository;

    @Autowired
    private PipelineBuildRepository buildRepository;

    @Autowired
    private PipelineExecutor pipelineExecutor;

    @Autowired
    private PipelineBuildMapper mapper;

    @Autowired
    private PipelineFileService pipelineFileService;

    @Transactional
    public PipelineBuild executePipeline(PipelineExecuteRequest pipelineRequest) throws IOException {
        UUID pipelineId = pipelineRequest.getPipelineId();

        Optional<PipelineEntity> optionalPipeline = pipelineRepository.findById(pipelineId);

        if (optionalPipeline.isEmpty()) {
            throw new EntityNotFoundException("Pipeline with id " + pipelineId + " not found");
        }

        PipelineEntity pipeline = optionalPipeline.get();
        PipelineBuildEntity pipelineBuild = createBuild(pipeline);

        createEmptyLogFiles(pipelineBuild);

        PipelineBuildEntity savedBuild = buildRepository.save(pipelineBuild);

        // Strictly run executing pipeline after build is commited
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                pipelineExecutor.executePipeline(savedBuild);
            }
        });

        return mapper.entityToDto(savedBuild);
    }

    private void createEmptyLogFiles(PipelineBuildEntity pipelineBuild) throws IOException {
        for (StageBuildEntity stageBuild : pipelineBuild.getStageBuilds()) {
            try {
                stageBuild.setLogPath(pipelineFileService.createLogFile(pipelineBuild.getId(), stageBuild.getId()));
            } catch (IOException e) {
                pipelineFileService.removeLogFiles(pipelineBuild);
            }
        }
    }

    // TODO: abstract this logic
    private PipelineBuildEntity createBuild(final PipelineEntity pipeline) {
        List<StageBuildEntity> stageBuilds = pipeline.getStages()
        .stream()
        .map(stage -> {
            StageBuildEntity stageBuild = new StageBuildEntity(stage);
            stageBuild.setId(UUID.randomUUID());

            return stageBuild;
        }).toList();

        PipelineBuildEntity pipelineBuild = new PipelineBuildEntity(pipeline, stageBuilds);
        pipelineBuild.setId(UUID.randomUUID());

        return pipelineBuild;
    }
}