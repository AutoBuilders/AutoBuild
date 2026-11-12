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

import com.autobuild.pipeline.configuration.FeatureFlag;
import com.autobuild.pipeline.configuration.FeatureFlagService;
import com.autobuild.pipeline.definiton.entity.Pipeline;
import com.autobuild.pipeline.definiton.repository.PipelineRepository;
import com.autobuild.pipeline.executor.dto.PipelineBuildDTO;
import com.autobuild.pipeline.executor.dto.PipelineExecuteRequest;
import com.autobuild.pipeline.executor.dto.mapper.PipelineBuildMapper;
import com.autobuild.pipeline.executor.entity.PipelineBuild;
import com.autobuild.pipeline.executor.entity.StageBuild;
import com.autobuild.pipeline.executor.job.PipelineExecutor;
import com.autobuild.pipeline.executor.repository.PipelineBuildRepository;
import com.autobuild.pipeline.executor.messaging.PipelineBuildMessagePublisher;
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

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private PipelineBuildMessagePublisher messagePublisher;

    @Transactional
    public PipelineBuildDTO executePipeline(PipelineExecuteRequest pipelineRequest) throws IOException {
        UUID pipelineId = pipelineRequest.getPipelineId();

        Optional<Pipeline> optionalPipeline = pipelineRepository.findById(pipelineId);

        if (optionalPipeline.isEmpty()) {
            throw new EntityNotFoundException("Pipeline with id " + pipelineId + " not found");
        }

        Pipeline pipeline = optionalPipeline.get();
        PipelineBuild pipelineBuild = createBuild(pipeline);

        createEmptyLogFiles(pipelineBuild);

        PipelineBuild savedBuild = buildRepository.save(pipelineBuild);

        // Strictly run executing pipeline after build is commited
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                if (featureFlagService.getBooleanValue(FeatureFlag.ENABLE_MESSAGE_QUEUE.getKey())) {
                    messagePublisher.publish(savedBuild.getId());
                } else {
                    pipelineExecutor.executePipeline(savedBuild);
                }
            }
        });

        return mapper.entityToDto(savedBuild);
    }

    private void createEmptyLogFiles(PipelineBuild pipelineBuild) throws IOException {
        for (StageBuild stageBuild : pipelineBuild.getStageBuilds()) {
            try {
                stageBuild.setLogPath(pipelineFileService.createLogFile(pipelineBuild.getId(), stageBuild.getId()));
            } catch (IOException e) {
                pipelineFileService.removeLogFiles(pipelineBuild);
            }
        }
    }

    // TODO: abstract this logic
    private PipelineBuild createBuild(final Pipeline pipeline) {
        List<StageBuild> stageBuilds = pipeline.getStages()
                                                .stream()
                                                .map(stage -> {
                                                    StageBuild stageBuild = new StageBuild(stage);
                                                    stageBuild.setId(UUID.randomUUID());

                                                    return stageBuild;
                                                }).toList();

        PipelineBuild pipelineBuild = new PipelineBuild(pipeline, stageBuilds);
        pipelineBuild.setId(UUID.randomUUID());

        return pipelineBuild;
    }
}