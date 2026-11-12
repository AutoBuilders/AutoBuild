package com.autobuild.pipeline.executor.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autobuild.pipeline.configuration.FeatureFlag;
import com.autobuild.pipeline.configuration.FeatureFlagService;
import com.autobuild.pipeline.definiton.entity.Pipeline;
import com.autobuild.pipeline.definiton.repository.PipelineRepository;
import com.autobuild.pipeline.executor.dto.PipelineExecuteRequest;
import com.autobuild.pipeline.executor.dto.messaging.PipelineBuildRequestMessage;
import com.autobuild.pipeline.executor.dto.messaging.StageDefinition;
import com.autobuild.pipeline.executor.job.PipelineExecutor;
import com.autobuild.pipeline.executor.messaging.PipelineBuildMessagePublisher;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

/**
 * Service layer for triggering pipeline execution.
 */
@Slf4j
@Service
public class PipelineExecutorService {

    @Autowired
    private PipelineRepository pipelineRepository;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private PipelineBuildMessagePublisher messagePublisher;

    @Autowired
    private PipelineExecutor pipelineExecutor;

    /**
     * Triggers execution of a pipeline.
     * When the message queue flag is enabled, publishes the pipeline definition to the queue.
     * Otherwise falls back to direct execution.
     *
     * @param pipelineRequest the execution request containing the pipeline ID
     * @return the generated build ID
     */
    public UUID executePipeline(PipelineExecuteRequest pipelineRequest) {
        UUID pipelineId = pipelineRequest.getPipelineId();

        Optional<Pipeline> optionalPipeline = pipelineRepository.findById(pipelineId);

        if (optionalPipeline.isEmpty()) {
            throw new EntityNotFoundException("Pipeline with id " + pipelineId + " not found");
        }

        Pipeline pipeline = optionalPipeline.get();
        UUID buildId = UUID.randomUUID();

        if (featureFlagService.getBooleanValue(FeatureFlag.ENABLE_NEW_ARCHITECTURE.getKey())) {
            PipelineBuildRequestMessage message = buildMessage(buildId, pipeline);
            messagePublisher.publish(message);
        } else {
            pipelineExecutor.executePipeline(null);
        }

        return buildId;
    }

    private PipelineBuildRequestMessage buildMessage(UUID buildId, Pipeline pipeline) {
        List<StageDefinition> stages = pipeline.getStages()
                .stream()
                .map(stage -> new StageDefinition(
                        stage.getName(),
                        stage.getScriptType(),
                        stage.getPath(),
                        stage.getStageOrder()))
                .toList();

        return new PipelineBuildRequestMessage(buildId, pipeline.getId(), pipeline.getName(), stages);
    }
}
