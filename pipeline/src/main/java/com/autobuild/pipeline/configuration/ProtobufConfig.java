package com.autobuild.pipeline.configuration;

import java.io.IOException;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.protobuf.ProtobufJsonFormatHttpMessageConverter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;

@Configuration
public class ProtobufConfig {

    // Read-only: handles JSON deserialization of proto request bodies.
    // canWrite is disabled so Jackson handles all serialization — this allows
    // the Jackson customizer below to strip enum prefixes (PIPELINE_RUNNING → RUNNING).
    @Bean
    public ProtobufJsonFormatHttpMessageConverter protobufJsonFormatHttpMessageConverter() {
        return new ProtobufJsonFormatHttpMessageConverter() {
            @Override
            public boolean canWrite(Class<?> clazz, MediaType mediaType) {
                return false;
            }
        };
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer protobufJacksonCustomizer() {
        return builder -> builder.serializerByType(Message.class, new JsonSerializer<Message>() {
            @Override
            public void serialize(Message message, JsonGenerator gen, SerializerProvider serializers)
                    throws IOException {
                String json = JsonFormat.printer().omittingInsignificantWhitespace().print(message);
                // Strip enum prefixes so the JSON matches the OpenAPI spec:
                // PIPELINE_RUNNING → RUNNING, STAGE_RUNNING → RUNNING
                json = json.replaceAll("\"(PIPELINE_|STAGE_)([A-Z_]+)\"", "\"$2\"");
                gen.writeRawValue(json);
            }
        });
    }
}
