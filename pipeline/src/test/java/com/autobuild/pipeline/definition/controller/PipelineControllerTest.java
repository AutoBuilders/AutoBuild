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
import java.util.ArrayList;
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
import com.autobuild.pipeline.definiton.dto.PipelineDTO;
import com.autobuild.pipeline.definiton.dto.StageDTO;
import com.autobuild.pipeline.definiton.exceptions.DuplicateEntryException;
import com.autobuild.pipeline.definiton.exceptions.InvalidIdException;
import com.autobuild.pipeline.definiton.service.PipelineService;
import com.autobuild.pipeline.testutility.DummyData;

import jakarta.persistence.EntityNotFoundException;

public class PipelineControllerTest {

    private PipelineDTO pipelineDTO = DummyData.getPipelineDTO();

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
        ResponseEntity<PipelineDTO> response = controller.getPipelineById("1");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(pipelineDTO, response.getBody());
    }

    @Test
    public void testGetAllPipelines() {
        List<PipelineDTO> pipelines = List.of(pipelineDTO);
        doReturn(pipelines).when(pipelineService).getAllPipelines();
        ResponseEntity<List<PipelineDTO>> response = controller.getAllPipelines();
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
        doReturn(pipelineDTO).when(pipelineService).createPipeline(any(PipelineDTO.class));
        ResponseEntity<PipelineDTO> response = controller.createPipeline(pipelineDTO);
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
        StageDTO newStage = new StageDTO(null, "deploy", "bash", "#!/bin/bash\necho DEPLOY");
        PipelineDTO request = new PipelineDTO();
        request.setStages(new ArrayList<>());
        request.getStages().add(newStage);

        ResponseEntity<PipelineDTO> response = controller.modifyPipeline("pid", request);

        assertEquals(HttpStatus.NOT_IMPLEMENTED, response.getStatusCode());
    }

    @Test
    public void testModifyPipelineStagesCreate() throws Exception {
        StageDTO newStage = new StageDTO(null, "deploy", "bash", "#!/bin/bash\necho DEPLOY");
        PipelineDTO modified = DummyData.getPipelineDTO();
        modified.setStages(new ArrayList<>(modified.getStages()));
        StageDTO createdStage = new StageDTO(UUID.randomUUID(), newStage.getName(),
                newStage.getScriptType(), newStage.getCommand());
        modified.getStages().add(createdStage);

        PipelineDTO request = new PipelineDTO();
        request.setStages(new ArrayList<>());
        request.getStages().add(newStage);

        doReturn(modified).when(pipelineService).modifyPipeline(eq("pid"), any(PipelineDTO.class));

        ResponseEntity<PipelineDTO> response = controller.modifyPipeline("pid", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(modified.getStages().size(), response.getBody().getStages().size());
    }

    @Test
    public void testModifyPipelineStagesUpdate() throws Exception {
        PipelineDTO base = DummyData.getPipelineDTO();
        base.setStages(new ArrayList<>(base.getStages()));
        StageDTO existing = base.getStages().get(0);
        StageDTO updateReq = new StageDTO(existing.getId(), "renamed",
                existing.getScriptType(), "#!/bin/bash\necho UPDATED");

        existing.setName("renamed");
        existing.setCommand(updateReq.getCommand());

        PipelineDTO request = new PipelineDTO();
        request.setStages(new ArrayList<>());
        request.getStages().add(updateReq);

        doReturn(base).when(pipelineService).modifyPipeline(eq("pid"), any(PipelineDTO.class));

        ResponseEntity<PipelineDTO> response = controller.modifyPipeline("pid", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("renamed", response.getBody().getStages().get(0).getName());
    }

    @Test
    public void testModifyPipelineStagesDelete() throws Exception {
        PipelineDTO base = DummyData.getPipelineDTO();
        base.setStages(new ArrayList<>(base.getStages()));
        StageDTO toDelete = base.getStages().get(0);
        base.getStages().remove(0);

        StageDTO deleteReq = new StageDTO(toDelete.getId(), null, null, null);
        PipelineDTO request = new PipelineDTO();
        request.setStages(new ArrayList<>());
        request.getStages().add(deleteReq);

        doReturn(base).when(pipelineService).modifyPipeline(eq("pid"), any(PipelineDTO.class));

        ResponseEntity<PipelineDTO> response = controller.modifyPipeline("pid", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        boolean present = response.getBody().getStages().stream()
                .anyMatch(s -> s.getId().equals(toDelete.getId()));
        assertEquals(false, present);
    }

    @Test
    public void testModifyPipelineStagesDuplicateEntry() throws Exception {
        StageDTO newStage = new StageDTO(null, "duplicate", "bash", "#!/bin/bash\necho X");
        PipelineDTO request = new PipelineDTO();
        request.setStages(new ArrayList<>());
        request.getStages().add(newStage);

        doThrow(new DuplicateEntryException("duplicate")).when(pipelineService)
                .modifyPipeline(eq("pid"), any(PipelineDTO.class));

        assertThrows(DuplicateEntryException.class, () -> controller.modifyPipeline("pid", request));
    }

    @Test
    public void testModifyPipelineStagesInvalidId() throws Exception {
        StageDTO newStage = new StageDTO(null, "stage", "bash", "#!/bin/bash\necho X");
        PipelineDTO request = new PipelineDTO();
        request.setStages(new ArrayList<>());
        request.getStages().add(newStage);

        doThrow(new InvalidIdException("invalid")).when(pipelineService)
                .modifyPipeline(eq("pid"), any(PipelineDTO.class));

        assertThrows(InvalidIdException.class, () -> controller.modifyPipeline("pid", request));
    }

    @Test
    public void testUpdatePipelineName() throws Exception {
        PipelineDTO updated = DummyData.getPipelineDTO();
        updated.setName("New Pipeline Name");

        PipelineDTO request = new PipelineDTO();
        request.setName("New Pipeline Name");

        doReturn(updated).when(pipelineService).modifyPipeline(eq("pid"), any(PipelineDTO.class));

        ResponseEntity<PipelineDTO> response = controller.modifyPipeline("pid", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("New Pipeline Name", response.getBody().getName());
    }

    @Test
    public void testModifyPipelineBadRequestNoNameOrStages() throws Exception {
        PipelineDTO request = new PipelineDTO();

        doThrow(new InvalidIdException("Request must contain either name or stages"))
                .when(pipelineService).modifyPipeline(eq("pid"), any(PipelineDTO.class));

        assertThrows(InvalidIdException.class, () -> controller.modifyPipeline("pid", request));
    }

    @Test
    public void testModifyPipelineStagesBadRequestEmpty() throws Exception {
        PipelineDTO request = new PipelineDTO();
        request.setStages(new ArrayList<>());

        doThrow(new InvalidIdException("Request must contain either name or stages"))
                .when(pipelineService).modifyPipeline(eq("pid"), any(PipelineDTO.class));

        assertThrows(InvalidIdException.class, () -> controller.modifyPipeline("pid", request));
    }

    @Test
    public void testCreatePipelineWithDuplicateStages() throws Exception {
        PipelineDTO dto = new PipelineDTO();
        dto.setName("pipe-x");
        dto.setStages(List.of(
                new StageDTO(null, "build", "bash", "echo 1"),
                new StageDTO(null, "Build", "bash", "echo 2")));

        // Explicitly stub it to throw the expected DuplicateEntryException.
        doThrow(new DuplicateEntryException("Duplicate stage name 'build'"))
                .when(pipelineService).createPipeline(any(PipelineDTO.class));

        assertThrows(DuplicateEntryException.class, () -> controller.createPipeline(dto));
    }

    @Test
    public void testCreatePipelineSuccess() throws Exception {
        PipelineDTO dto = new PipelineDTO();
        dto.setName("pipe-y");
        dto.setStages(List.of(new StageDTO(null, "build", "bash", "echo 1")));
        PipelineDTO saved = new PipelineDTO();
        saved.setId(UUID.randomUUID());
        saved.setName(dto.getName());
        when(pipelineService.createPipeline(dto)).thenReturn(saved);
        ResponseEntity<PipelineDTO> resp = controller.createPipeline(dto);
        assertEquals(saved.getId(), resp.getBody().getId());
    }

    @Test
    public void testUpdatePipelineReturnsForbiddenWhenFlagDisabled() throws Exception {
        when(featureFlagService.getBooleanValue(anyString())).thenReturn(false);
        PipelineDTO request = new PipelineDTO();
        request.setName("should be blocked");

        ResponseEntity<PipelineDTO> response = controller.updatePipeline("pid", request);

        assertEquals(HttpStatus.NOT_IMPLEMENTED, response.getStatusCode());
    }

    @Test
    public void testReplacePipelineStagesCompletely() throws Exception {
        PipelineDTO original = DummyData.getPipelineDTO();
        original.setStages(new ArrayList<>(List.of(
            new StageDTO(UUID.randomUUID(), "build", "bash", "echo build"),
            new StageDTO(UUID.randomUUID(), "test", "bash", "echo test")
        )));

        PipelineDTO replaced = new PipelineDTO();
        replaced.setId(original.getId());
        replaced.setName(original.getName());
        replaced.setStages(List.of(
            new StageDTO(UUID.randomUUID(), "deploy", "bash", "echo deploy")
        ));

        PipelineDTO request = new PipelineDTO();
        request.setStages(List.of(
            new StageDTO(null, "deploy", "bash", "echo deploy")
        ));

        doReturn(replaced).when(pipelineService).replacePipeline(eq("pid"), any(PipelineDTO.class));

        ResponseEntity<PipelineDTO> response = controller.updatePipeline("pid", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getStages().size());
        assertEquals("deploy", response.getBody().getStages().get(0).getName());
    }

    @Test
    public void testReplacePipelineNameAndStages() throws Exception {
        PipelineDTO replaced = new PipelineDTO();
        replaced.setId(UUID.randomUUID());
        replaced.setName("NewPipelineName");
        replaced.setStages(List.of(
            new StageDTO(UUID.randomUUID(), "stage1", "python", "print('hello')"),
            new StageDTO(UUID.randomUUID(), "stage2", "bash", "echo 'world'")
        ));

        PipelineDTO request = new PipelineDTO();
        request.setName("NewPipelineName");
        request.setStages(List.of(
            new StageDTO(null, "stage1", "python", "print('hello')"),
            new StageDTO(null, "stage2", "bash", "echo 'world'")
        ));

        doReturn(replaced).when(pipelineService).replacePipeline(eq("pid"), any(PipelineDTO.class));

        ResponseEntity<PipelineDTO> response = controller.updatePipeline("pid", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("NewPipelineName", response.getBody().getName());
        assertEquals(2, response.getBody().getStages().size());
    }

    @Test
    public void testReplacePipelineInvalidId() throws Exception {
        PipelineDTO request = new PipelineDTO();
        request.setName("test");

        doThrow(new InvalidIdException("Invalid ID")).when(pipelineService)
                .replacePipeline(eq("invalid"), any(PipelineDTO.class));

        assertThrows(InvalidIdException.class, () -> controller.updatePipeline("invalid", request));
    }

    @Test
    public void testReplacePipelineDuplicateName() throws Exception {
        PipelineDTO request = new PipelineDTO();
        request.setName("existingName");

        doThrow(new DuplicateEntryException("Name exists")).when(pipelineService)
                .replacePipeline(eq("pid"), any(PipelineDTO.class));

        assertThrows(DuplicateEntryException.class, () -> controller.updatePipeline("pid", request));
    }
}