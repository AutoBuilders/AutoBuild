package com.autobuild.pipeline.definition.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.autobuild.pipeline.configuration.FeatureFlagService;
import com.autobuild.pipeline.definiton.controller.PipelineController;
import com.autobuild.pipeline.definiton.dto.Pipeline;
import com.autobuild.pipeline.definiton.dto.Stage;
import com.autobuild.pipeline.definiton.exceptions.DuplicateEntryException;
import com.autobuild.pipeline.definiton.exceptions.InvalidIdException;
import com.autobuild.pipeline.definiton.service.PipelineService;
import com.autobuild.pipeline.testutility.DummyData;

import jakarta.persistence.EntityNotFoundException;

public class PipelineControllerTest {

    private Pipeline pipelineDTO = DummyData.getPipelineDTO();

    @Mock
    private PipelineService pipelineService;

    @Mock
    private FeatureFlagService featureFlagService;

    @InjectMocks
    private PipelineController controller;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        when(featureFlagService.getBooleanValue(anyString())).thenReturn(true);
    }

    @Test
    public void testGetPipelineWithNoPipeline() throws InvalidIdException, IOException {
        doThrow(new EntityNotFoundException("Dummy Exception")).when(pipelineService).getPipelineById(anyString());
        assertThrows(EntityNotFoundException.class, () -> controller.getPipelineById("1"));
    }

    @Test
    public void testGetPipelineWithPipeline() throws InvalidIdException, IOException {
        doReturn(pipelineDTO).when(pipelineService).getPipelineById(anyString());
        ResponseEntity<Pipeline> response = controller.getPipelineById("1");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(pipelineDTO, response.getBody());
    }

    @Test
    public void testGetAllPipelines() {
        List<Pipeline> pipelines = List.of(pipelineDTO);
        doReturn(pipelines).when(pipelineService).getAllPipelines();
        ResponseEntity<List<Pipeline>> response = controller.getAllPipelines();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(pipelines, response.getBody());
    }

    @Test
    public void testGetPipelineWithInvalidId() throws InvalidIdException, IOException {
        doThrow(new InvalidIdException("Dummy Exception")).when(pipelineService).getPipelineById(anyString());
        assertThrows(InvalidIdException.class, () -> controller.getPipelineById("1"));
    }

    @Test
    public void testCreatePipelineWithPipeline() throws DuplicateEntryException, IOException, InvalidIdException {
        doReturn(pipelineDTO).when(pipelineService).createPipeline(any(Pipeline.class));
        ResponseEntity<Pipeline> response = controller.createPipeline(pipelineDTO);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(pipelineDTO, response.getBody());
        assertEquals("/api/v1/pipeline/" + pipelineDTO.getId(), response.getHeaders().get("location").get(0));
    }

    @Test
    public void testDeletePipeline() throws IOException, InvalidIdException {
        doNothing().when(pipelineService).deletePipelineById(anyString());
        ResponseEntity<Void> response = controller.deletePipeline("abc");
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void testModifyPipelineReturnsForbiddenWhenFlagDisabled() throws Exception {
        when(featureFlagService.getBooleanValue(anyString())).thenReturn(false);
        Pipeline request = Pipeline.newBuilder()
            .addAllStages(List.of(Stage.newBuilder().setName("deploy").setScriptType("bash")
                .setCommand("#!/bin/bash\necho DEPLOY").build()))
            .build();

        ResponseEntity<Pipeline> response = controller.modifyPipeline("pid", request);

        assertEquals(HttpStatus.NOT_IMPLEMENTED, response.getStatusCode());
    }

    @Test
    public void testModifyPipelineStagesCreate() throws Exception {
        Stage newStage = Stage.newBuilder().setName("deploy").setScriptType("bash")
            .setCommand("#!/bin/bash\necho DEPLOY").build();

        Stage createdStage = Stage.newBuilder().setId(UUID.randomUUID().toString())
            .setName(newStage.getName()).setScriptType(newStage.getScriptType())
            .setCommand(newStage.getCommand()).build();

        Pipeline modified = pipelineDTO.toBuilder().addStages(createdStage).build();

        Pipeline request = Pipeline.newBuilder().addAllStages(List.of(newStage)).build();

        doReturn(modified).when(pipelineService).modifyPipeline(eq("pid"), any(Pipeline.class));

        ResponseEntity<Pipeline> response = controller.modifyPipeline("pid", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(modified.getStagesList().size(), response.getBody().getStagesList().size());
    }

    @Test
    public void testModifyPipelineStagesUpdate() throws Exception {
        Stage existing = pipelineDTO.getStagesList().get(0);
        Stage updated = existing.toBuilder().setName("renamed").setCommand("#!/bin/bash\necho UPDATED").build();
        Pipeline modified = Pipeline.newBuilder()
            .setId(pipelineDTO.getId()).setName(pipelineDTO.getName())
            .addAllStages(List.of(updated)).build();

        Pipeline request = Pipeline.newBuilder()
            .addAllStages(List.of(Stage.newBuilder().setId(existing.getId()).setName("renamed")
                .setScriptType(existing.getScriptType()).setCommand("#!/bin/bash\necho UPDATED").build()))
            .build();

        doReturn(modified).when(pipelineService).modifyPipeline(eq("pid"), any(Pipeline.class));

        ResponseEntity<Pipeline> response = controller.modifyPipeline("pid", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("renamed", response.getBody().getStagesList().get(0).getName());
    }

    @Test
    public void testModifyPipelineStagesDelete() throws Exception {
        Stage toDelete = pipelineDTO.getStagesList().get(0);
        Pipeline modified = Pipeline.newBuilder()
            .setId(pipelineDTO.getId()).setName(pipelineDTO.getName())
            .build();

        Pipeline request = Pipeline.newBuilder()
            .addAllStages(List.of(Stage.newBuilder().setId(toDelete.getId()).build()))
            .build();

        doReturn(modified).when(pipelineService).modifyPipeline(eq("pid"), any(Pipeline.class));

        ResponseEntity<Pipeline> response = controller.modifyPipeline("pid", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        boolean present = response.getBody().getStagesList().stream()
            .anyMatch(s -> s.getId().equals(toDelete.getId()));
        assertEquals(false, present);
    }

    @Test
    public void testModifyPipelineStagesDuplicateEntry() throws Exception {
        Pipeline request = Pipeline.newBuilder()
            .addAllStages(List.of(Stage.newBuilder().setName("duplicate").setScriptType("bash")
                .setCommand("#!/bin/bash\necho X").build()))
            .build();

        doThrow(new DuplicateEntryException("duplicate")).when(pipelineService)
            .modifyPipeline(eq("pid"), any(Pipeline.class));

        assertThrows(DuplicateEntryException.class, () -> controller.modifyPipeline("pid", request));
    }

    @Test
    public void testModifyPipelineStagesInvalidId() throws Exception {
        Pipeline request = Pipeline.newBuilder()
            .addAllStages(List.of(Stage.newBuilder().setName("stage").setScriptType("bash")
                .setCommand("#!/bin/bash\necho X").build()))
            .build();

        doThrow(new InvalidIdException("invalid")).when(pipelineService)
            .modifyPipeline(eq("pid"), any(Pipeline.class));

        assertThrows(InvalidIdException.class, () -> controller.modifyPipeline("pid", request));
    }

    @Test
    public void testUpdatePipelineName() throws Exception {
        Pipeline updated = pipelineDTO.toBuilder().setName("New Pipeline Name").build();
        Pipeline request = Pipeline.newBuilder().setName("New Pipeline Name").build();

        doReturn(updated).when(pipelineService).modifyPipeline(eq("pid"), any(Pipeline.class));

        ResponseEntity<Pipeline> response = controller.modifyPipeline("pid", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("New Pipeline Name", response.getBody().getName());
    }

    @Test
    public void testModifyPipelineBadRequestNoNameOrStages() throws Exception {
        Pipeline request = Pipeline.newBuilder().build();

        doThrow(new InvalidIdException("Request must contain either name or stages"))
            .when(pipelineService).modifyPipeline(eq("pid"), any(Pipeline.class));

        assertThrows(InvalidIdException.class, () -> controller.modifyPipeline("pid", request));
    }

    @Test
    public void testModifyPipelineStagesBadRequestEmpty() throws Exception {
        Pipeline request = Pipeline.newBuilder().build();

        doThrow(new InvalidIdException("Request must contain either name or stages"))
            .when(pipelineService).modifyPipeline(eq("pid"), any(Pipeline.class));

        assertThrows(InvalidIdException.class, () -> controller.modifyPipeline("pid", request));
    }

    @Test
    public void testCreatePipelineWithDuplicateStages() throws Exception {
        Pipeline dto = Pipeline.newBuilder()
            .setName("pipe-x")
            .addAllStages(List.of(
                Stage.newBuilder().setName("build").setScriptType("bash").setCommand("echo 1").build(),
                Stage.newBuilder().setName("Build").setScriptType("bash").setCommand("echo 2").build()))
            .build();

        doThrow(new DuplicateEntryException("Duplicate stage name 'build'"))
            .when(pipelineService).createPipeline(any(Pipeline.class));

        assertThrows(DuplicateEntryException.class, () -> controller.createPipeline(dto));
    }

    @Test
    public void testCreatePipelineSuccess() throws Exception {
        Pipeline dto = Pipeline.newBuilder()
            .setName("pipe-y")
            .addAllStages(List.of(Stage.newBuilder().setName("build").setScriptType("bash").setCommand("echo 1").build()))
            .build();
        Pipeline saved = dto.toBuilder().setId(UUID.randomUUID().toString()).build();

        when(pipelineService.createPipeline(dto)).thenReturn(saved);
        ResponseEntity<Pipeline> resp = controller.createPipeline(dto);
        assertEquals(saved.getId(), resp.getBody().getId());
    }

    @Test
    public void testUpdatePipelineReturnsForbiddenWhenFlagDisabled() throws Exception {
        when(featureFlagService.getBooleanValue(anyString())).thenReturn(false);
        Pipeline request = Pipeline.newBuilder().setName("should be blocked").build();

        ResponseEntity<Pipeline> response = controller.updatePipeline("pid", request);

        assertEquals(HttpStatus.NOT_IMPLEMENTED, response.getStatusCode());
    }

    @Test
    public void testReplacePipelineStagesCompletely() throws Exception {
        Pipeline replaced = Pipeline.newBuilder()
            .setId(UUID.randomUUID().toString()).setName(pipelineDTO.getName())
            .addAllStages(List.of(Stage.newBuilder().setId(UUID.randomUUID().toString())
                .setName("deploy").setScriptType("bash").setCommand("echo deploy").build()))
            .build();

        Pipeline request = Pipeline.newBuilder()
            .addAllStages(List.of(Stage.newBuilder().setName("deploy").setScriptType("bash").setCommand("echo deploy").build()))
            .build();

        doReturn(replaced).when(pipelineService).replacePipeline(eq("pid"), any(Pipeline.class));

        ResponseEntity<Pipeline> response = controller.updatePipeline("pid", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getStagesList().size());
        assertEquals("deploy", response.getBody().getStagesList().get(0).getName());
    }

    @Test
    public void testReplacePipelineNameAndStages() throws Exception {
        Pipeline replaced = Pipeline.newBuilder()
            .setId(UUID.randomUUID().toString()).setName("NewPipelineName")
            .addAllStages(List.of(
                Stage.newBuilder().setId(UUID.randomUUID().toString()).setName("stage1").setScriptType("python").setCommand("print('hello')").build(),
                Stage.newBuilder().setId(UUID.randomUUID().toString()).setName("stage2").setScriptType("bash").setCommand("echo 'world'").build()))
            .build();

        Pipeline request = Pipeline.newBuilder()
            .setName("NewPipelineName")
            .addAllStages(List.of(
                Stage.newBuilder().setName("stage1").setScriptType("python").setCommand("print('hello')").build(),
                Stage.newBuilder().setName("stage2").setScriptType("bash").setCommand("echo 'world'").build()))
            .build();

        doReturn(replaced).when(pipelineService).replacePipeline(eq("pid"), any(Pipeline.class));

        ResponseEntity<Pipeline> response = controller.updatePipeline("pid", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("NewPipelineName", response.getBody().getName());
        assertEquals(2, response.getBody().getStagesList().size());
    }

    @Test
    public void testReplacePipelineInvalidId() throws Exception {
        Pipeline request = Pipeline.newBuilder().setName("test").build();

        doThrow(new InvalidIdException("Invalid ID")).when(pipelineService)
            .replacePipeline(eq("invalid"), any(Pipeline.class));

        assertThrows(InvalidIdException.class, () -> controller.updatePipeline("invalid", request));
    }

    @Test
    public void testReplacePipelineDuplicateName() throws Exception {
        Pipeline request = Pipeline.newBuilder().setName("existingName").build();

        doThrow(new DuplicateEntryException("Name exists")).when(pipelineService)
            .replacePipeline(eq("pid"), any(Pipeline.class));

        assertThrows(DuplicateEntryException.class, () -> controller.updatePipeline("pid", request));
    }
}
