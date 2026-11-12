package com.autobuild.pipeline.executor.messaging;

import java.util.UUID;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autobuild.pipeline.configuration.MessagingConfig;
import com.autobuild.pipeline.executor.entity.PipelineBuild;
import com.autobuild.pipeline.executor.job.PipelineExecutor;
import com.autobuild.pipeline.executor.repository.PipelineBuildRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

/**
 * Consumes pipeline build messages from the queue and triggers execution.
 *
 * @author Suvabrata Chowdhury
 */
@Slf4j
@Service
public class PipelineBuildMessageConsumer {

    @Autowired
    private PipelineBuildRepository buildRepository;

    @Autowired
    private PipelineExecutor pipelineExecutor;

    /**
     * Receives a build ID string from the queue, loads the build, and executes it.
     *
     * @param buildId UUID string of the {@code PipelineBuild} to execute
     */
    @RabbitListener(queues = MessagingConfig.PIPELINE_BUILD_QUEUE)
    public void consume(String buildId) {
        UUID id = UUID.fromString(buildId);
        PipelineBuild build = buildRepository.findByIdWithStageBuilds(id)
                .orElseThrow(() -> new EntityNotFoundException("PipelineBuild not found: " + id));
        log.info("Consuming build {} from queue", id);
        pipelineExecutor.executePipeline(build);
    }
}