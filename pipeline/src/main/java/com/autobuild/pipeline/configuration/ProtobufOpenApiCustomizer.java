package com.autobuild.pipeline.configuration;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

/**
 * Cleans up the generated OpenAPI spec from proto internals.
 *
 * Proto-generated classes expose many framework internals (Descriptor, ByteString, Parser, etc.)
 * via reflection. Springdoc picks these up and adds them to the spec. Problems caused:
 *   1. Non-determinism: HashMap iteration order changes each JVM run → CI "spec out of sync" fails.
 *   2. Wrong field names: proto uses getStageBuildsList() → "stageBuildsList"; frontend expects "stageBuilds".
 *   3. Prefixed enum values: proto uses PIPELINE_SUCCESS, STAGE_SUCCESS; frontend expects SUCCESS.
 *
 * This customizer runs after spec generation and fixes all three.
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

    // Only the business fields from the .proto definitions (proto snake_case → Java camelCase).
    private static final Map<String, Set<String>> SCHEMA_ALLOWED_PROPERTIES = Map.of(
            "Pipeline",      Set.of("id", "name", "stagesList"),
            "Stage",         Set.of("id", "name", "scriptType", "stageOrder"),
            "PipelineBuild", Set.of("id", "pipelineId", "pipelineName", "currentState", "stageBuildsList"),
            "StageBuild",    Set.of("id", "stageId", "stageName", "stageOrder", "currentState")
    );

    // Proto list accessors use a "List" suffix — rename them to clean API names for the frontend.
    private static final Map<String, Map<String, String>> PROPERTY_RENAMES = Map.of(
            "Pipeline",      Map.of("stagesList", "stages"),
            "PipelineBuild", Map.of("stageBuildsList", "stageBuilds")
    );

    // Proto enums include a package prefix (PIPELINE_, STAGE_) and an UNRECOGNIZED catch-all.
    // Strip the prefix and remove UNRECOGNIZED so the frontend sees SUCCESS not PIPELINE_SUCCESS.
    private static final Map<String, Map<String, String>> ENUM_PREFIXES = Map.of(
            "PipelineBuild", Map.of("currentState", "PIPELINE_"),
            "StageBuild",    Map.of("currentState", "STAGE_")
    );

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void customise(OpenAPI openApi) {
        if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {
            return;
        }

        Map<String, Schema> schemas = openApi.getComponents().getSchemas();

        // Step 1: remove all proto internal top-level schemas
        schemas.entrySet().removeIf(entry -> !KEEP_SCHEMAS.contains(entry.getKey()));

        // Step 2: strip proto-internal properties, keep only declared business fields
        schemas.forEach((name, schema) -> {
            Set<String> allowedProps = SCHEMA_ALLOWED_PROPERTIES.get(name);
            if (allowedProps != null && schema.getProperties() != null) {
                Map<String, Object> props = (Map<String, Object>) schema.getProperties();
                props.entrySet().removeIf(e -> !allowedProps.contains(e.getKey()));
            }
        });

        // Step 3: rename proto list accessor names to clean frontend-facing names
        schemas.forEach((schemaName, schema) -> {
            Map<String, String> renames = PROPERTY_RENAMES.get(schemaName);
            if (renames != null && schema.getProperties() != null) {
                Map<String, Object> props = (Map<String, Object>) schema.getProperties();
                renames.forEach((oldName, newName) -> {
                    Object val = props.remove(oldName);
                    if (val != null) {
                        props.put(newName, val);
                    }
                });
            }
        });

        // Step 4: strip proto enum prefixes and remove UNRECOGNIZED catch-all value
        schemas.forEach((schemaName, schema) -> {
            Map<String, String> enumPrefixes = ENUM_PREFIXES.get(schemaName);
            if (enumPrefixes != null && schema.getProperties() != null) {
                Map<String, Schema> props = (Map<String, Schema>) schema.getProperties();
                enumPrefixes.forEach((fieldName, prefix) -> {
                    Schema<?> fieldSchema = props.get(fieldName);
                    if (fieldSchema != null && fieldSchema.getEnum() != null) {
                        List<Object> enumValues = (List<Object>) fieldSchema.getEnum();
                        enumValues.removeIf(v -> "UNRECOGNIZED".equals(v.toString()));
                        enumValues.replaceAll(v -> {
                            String s = v.toString();
                            return s.startsWith(prefix) ? s.substring(prefix.length()) : s;
                        });
                    }
                });
            }
        });

        // Step 5: sort schemas alphabetically and sort properties within each schema
        schemas.forEach((name, schema) -> {
            if (schema.getProperties() != null) {
                schema.setProperties(new TreeMap<>(schema.getProperties()));
            }
        });
        openApi.getComponents().setSchemas(new TreeMap<>(schemas));
    }
}
