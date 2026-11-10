package com.autobuild.pipeline.configuration;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

/**
 * Removes proto internal schemas (Descriptor, Parser*, OrBuilder, ByteString, etc.)
 * from the generated OpenAPI spec and sorts remaining schemas alphabetically.
 * Without this, HashMap iteration order in proto classes causes a different spec
 * on every JVM run, which breaks the CI "spec is out of sync" check.
 */
@Component
public class ProtobufOpenApiCustomizer implements OpenApiCustomizer {

    private static final Set<String> KEEP_SCHEMAS = Set.of(
            "Pipeline",
            "PipelineBuild",
            "PipelineExecuteRequest",
            "Stage",
            "StageBuild"
    );

    @Override
    public void customise(OpenAPI openApi) {
        if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {
            return;
        }

        Map<String, Schema> schemas = openApi.getComponents().getSchemas();
        schemas.entrySet().removeIf(entry -> !KEEP_SCHEMAS.contains(entry.getKey()));

        Map<String, Schema> sorted = new TreeMap<>(schemas);
        schemas.clear();
        schemas.putAll(sorted);
    }
}
