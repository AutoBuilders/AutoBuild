package com.autobuild.pipeline.executor.messaging;

import com.autobuild.pipeline.executor.dto.messaging.PipelineBuildRequestMessage;

/**
 * Contract for publishing pipeline build request messages to a message queue.
 */
public interface PipelineBuildMessagePublisher {

    /**
     * Publishes a pipeline build request message.
     *
     * @param message the build request containing the pipeline definition and generated build ID
     */
    void publish(PipelineBuildRequestMessage message);
}
