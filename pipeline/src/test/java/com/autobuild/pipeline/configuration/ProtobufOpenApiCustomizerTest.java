package com.autobuild.pipeline.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

class ProtobufOpenApiCustomizerTest {

    private ProtobufOpenApiCustomizer customizer;
    private OpenAPI openApi;

    @BeforeEach
    void setUp() {
        customizer = new ProtobufOpenApiCustomizer();
        openApi = new OpenAPI();
    }

    @Test
    void customise_doesNothing_whenComponentsIsNull() {
        customizer.customise(openApi);
    }

    @Test
    void customise_doesNothing_whenSchemasIsNull() {
        openApi.setComponents(new Components());
        customizer.customise(openApi);
    }

    @Test
    void customise_removesProtoInternalSchemas() {
        openApi.setComponents(new Components());
        openApi.getComponents().setSchemas(new HashMap<>());
        openApi.getComponents().getSchemas().put("Pipeline", new Schema<>());
        openApi.getComponents().getSchemas().put("Descriptor", new Schema<>());
        openApi.getComponents().getSchemas().put("ByteString", new Schema<>());
        openApi.getComponents().getSchemas().put("ParserPipeline", new Schema<>());

        customizer.customise(openApi);

        Map<String, Schema> schemas = openApi.getComponents().getSchemas();
        assertTrue(schemas.containsKey("Pipeline"));
        assertFalse(schemas.containsKey("Descriptor"));
        assertFalse(schemas.containsKey("ByteString"));
        assertFalse(schemas.containsKey("ParserPipeline"));
    }

    @Test
    void customise_stripsProtoInternalPropertiesFromPipelineSchema() {
        Schema<Object> pipelineSchema = new Schema<>();
        pipelineSchema.setProperties(new HashMap<>());
        pipelineSchema.getProperties().put("id", new Schema<>());
        pipelineSchema.getProperties().put("name", new Schema<>());
        pipelineSchema.getProperties().put("stagesList", new Schema<>());
        pipelineSchema.getProperties().put("serializedSize", new Schema<>());
        pipelineSchema.getProperties().put("unknownFields", new Schema<>());

        openApi.setComponents(new Components());
        openApi.getComponents().setSchemas(new HashMap<>());
        openApi.getComponents().getSchemas().put("Pipeline", pipelineSchema);

        customizer.customise(openApi);

        Map<?, ?> props = openApi.getComponents().getSchemas().get("Pipeline").getProperties();
        assertTrue(props.containsKey("id"));
        assertTrue(props.containsKey("name"));
        assertFalse(props.containsKey("serializedSize"));
        assertFalse(props.containsKey("unknownFields"));
    }

    @Test
    void customise_renamesStagesToStagesList() {
        Schema<Object> pipelineSchema = new Schema<>();
        pipelineSchema.setProperties(new HashMap<>());
        Schema<Object> stagesListSchema = new Schema<>();
        pipelineSchema.getProperties().put("stagesList", stagesListSchema);

        openApi.setComponents(new Components());
        openApi.getComponents().setSchemas(new HashMap<>());
        openApi.getComponents().getSchemas().put("Pipeline", pipelineSchema);

        customizer.customise(openApi);

        Map<?, ?> props = openApi.getComponents().getSchemas().get("Pipeline").getProperties();
        assertTrue(props.containsKey("stages"));
        assertFalse(props.containsKey("stagesList"));
        assertEquals(stagesListSchema, props.get("stages"));
    }

    @Test
    void customise_renamesStageBuildsList() {
        Schema<Object> buildSchema = new Schema<>();
        buildSchema.setProperties(new HashMap<>());
        buildSchema.getProperties().put("stageBuildsList", new Schema<>());

        openApi.setComponents(new Components());
        openApi.getComponents().setSchemas(new HashMap<>());
        openApi.getComponents().getSchemas().put("PipelineBuild", buildSchema);

        customizer.customise(openApi);

        Map<?, ?> props = openApi.getComponents().getSchemas().get("PipelineBuild").getProperties();
        assertTrue(props.containsKey("stageBuilds"));
        assertFalse(props.containsKey("stageBuildsList"));
    }

    @Test
    void customise_stripsEnumPrefixAndRemovesUnrecognized() {
        Schema<Object> buildSchema = new Schema<>();
        buildSchema.setProperties(new HashMap<>());
        Schema<Object> stateSchema = new Schema<>();
        List<Object> enumValues = new ArrayList<>();
        enumValues.add("PIPELINE_WAITING");
        enumValues.add("PIPELINE_RUNNING");
        enumValues.add("PIPELINE_SUCCESS");
        enumValues.add("PIPELINE_FAILED");
        enumValues.add("UNRECOGNIZED");
        stateSchema.setEnum(enumValues);
        buildSchema.getProperties().put("currentState", stateSchema);

        openApi.setComponents(new Components());
        openApi.getComponents().setSchemas(new HashMap<>());
        openApi.getComponents().getSchemas().put("PipelineBuild", buildSchema);

        customizer.customise(openApi);

        @SuppressWarnings("unchecked")
        List<?> result = ((Schema<?>) openApi.getComponents().getSchemas()
                .get("PipelineBuild").getProperties().get("currentState")).getEnum();
        assertTrue(result.contains("SUCCESS"));
        assertTrue(result.contains("RUNNING"));
        assertFalse(result.contains("PIPELINE_SUCCESS"));
        assertFalse(result.contains("UNRECOGNIZED"));
    }

    @Test
    void customise_sortsSchemasAlphabetically() {
        openApi.setComponents(new Components());
        openApi.getComponents().setSchemas(new LinkedHashMap<>());
        openApi.getComponents().getSchemas().put("Stage", new Schema<>());
        openApi.getComponents().getSchemas().put("Pipeline", new Schema<>());
        openApi.getComponents().getSchemas().put("StageBuild", new Schema<>());
        openApi.getComponents().getSchemas().put("PipelineBuild", new Schema<>());

        customizer.customise(openApi);

        List<String> keys = new ArrayList<>(openApi.getComponents().getSchemas().keySet());
        assertEquals("Pipeline", keys.get(0));
        assertEquals("PipelineBuild", keys.get(1));
        assertEquals("Stage", keys.get(2));
        assertEquals("StageBuild", keys.get(3));
    }

    @Test
    @SuppressWarnings("unchecked")
    void customise_sortsPropertiesWithinSchemaAlphabetically() {
        Schema<Object> stageSchema = new Schema<>();
        stageSchema.setProperties(new LinkedHashMap<>());
        stageSchema.getProperties().put("stageOrder", new Schema<>());
        stageSchema.getProperties().put("id", new Schema<>());
        stageSchema.getProperties().put("name", new Schema<>());
        stageSchema.getProperties().put("scriptType", new Schema<>());

        openApi.setComponents(new Components());
        openApi.getComponents().setSchemas(new HashMap<>());
        openApi.getComponents().getSchemas().put("Stage", stageSchema);

        customizer.customise(openApi);

        List<String> propKeys = new ArrayList<>(openApi.getComponents().getSchemas().get("Stage").getProperties().keySet());
        assertEquals("id", propKeys.get(0));
        assertEquals("name", propKeys.get(1));
        assertEquals("scriptType", propKeys.get(2));
        assertEquals("stageOrder", propKeys.get(3));
    }

    @Test
    void customise_handlesSchemaWithNullProperties() {
        openApi.setComponents(new Components());
        openApi.getComponents().setSchemas(new HashMap<>());
        openApi.getComponents().getSchemas().put("Pipeline", new Schema<>());

        customizer.customise(openApi);

        assertNull(openApi.getComponents().getSchemas().get("Pipeline").getProperties());
    }
}
