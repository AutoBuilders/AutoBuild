package com.autobuild.pipeline.executor.messaging;

import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autobuild.pipeline.configuration.MessagingConfig;

import lombok.extern.slf4j.Slf4j;

/**
 * Publishes pipeline build IDs to the message queue for async execution.
 *
 * @author Suvabrata Chowdhury
 */
@Slf4j
@Service
public class PipelineBuildMessagePublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * Sends a pipeline build ID to the queue.
     *
     * @param buildId the ID of the saved {@code PipelineBuild} to execute
     */
    public void publish(UUID buildId) {
        rabbitTemplate.convertAndSend(MessagingConfig.PIPELINE_BUILD_QUEUE, buildId.toString());
        log.info("Published build {} to queue '{}'", buildId, MessagingConfig.PIPELINE_BUILD_QUEUE);
    }
}
