package com.autobuild.pipeline.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.protobuf.ProtobufJsonFormatHttpMessageConverter;

/**
 * Registers a message converter that handles JSON serialization/deserialization
 * for proto-generated classes using protobuf-java-util's JsonFormat.
 *
 * Without this, Spring falls back to standard Jackson which reads getter names
 * (e.g. getStageBuildsList() → "stageBuildsList") instead of proto field names
 * (stage_builds → "stageBuilds"). It also causes 415 Unsupported Media Type
 * when a client POSTs JSON to an endpoint that takes a proto class as @RequestBody.
 */
@Configuration
public class ProtobufConverterConfig {

    @Bean
    public ProtobufJsonFormatHttpMessageConverter protobufJsonFormatHttpMessageConverter() {
        return new ProtobufJsonFormatHttpMessageConverter();
    }
}
