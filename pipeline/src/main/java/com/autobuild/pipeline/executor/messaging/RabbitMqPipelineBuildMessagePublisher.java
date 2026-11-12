package com.autobuild.pipeline.executor.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autobuild.pipeline.configuration.MessagingConfig;
import com.autobuild.pipeline.executor.dto.messaging.PipelineBuildRequestMessage;

import lombok.extern.slf4j.Slf4j;

/**
 * RabbitMQ implementation of {@link PipelineBuildMessagePublisher}.
 */
@Slf4j
@Service
public class RabbitMqPipelineBuildMessagePublisher implements PipelineBuildMessagePublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void publish(PipelineBuildRequestMessage message) {
        rabbitTemplate.convertAndSend(MessagingConfig.PIPELINE_BUILD_REQUEST_QUEUE, message);
        log.info("Published build request {} to queue '{}'",
                message.getBuildId(), MessagingConfig.PIPELINE_BUILD_REQUEST_QUEUE);
    }
}
