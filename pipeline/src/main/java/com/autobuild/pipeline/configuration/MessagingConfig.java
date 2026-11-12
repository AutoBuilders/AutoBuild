package com.autobuild.pipeline.configuration;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ messaging configuration.
 *
 * @author Suvabrata Chowdhury
 */
@Configuration
public class MessagingConfig {

    /** Name of the queue used to dispatch pipeline build execution requests. */
    public static final String PIPELINE_BUILD_QUEUE = "pipeline-build-queue";

    @Bean
    public Queue pipelineBuildQueue() {
        return new Queue(PIPELINE_BUILD_QUEUE, true);
    }
}
