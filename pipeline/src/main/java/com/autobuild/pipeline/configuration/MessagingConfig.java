package com.autobuild.pipeline.configuration;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Message queue configuration.
 */
@Configuration
public class MessagingConfig {

    /** Name of the queue used to dispatch pipeline build execution requests. */
    public static final String PIPELINE_BUILD_REQUEST_QUEUE = "pipeline-build-request-queue";

    @Bean
    public Queue pipelineBuildQueue() {
        return new Queue(PIPELINE_BUILD_REQUEST_QUEUE, true);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
