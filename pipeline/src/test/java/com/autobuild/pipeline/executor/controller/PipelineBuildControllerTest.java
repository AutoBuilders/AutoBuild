package com.autobuild.pipeline.executor.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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

import com.autobuild.pipeline.executor.gen.dto.PipelineBuild;
import com.autobuild.pipeline.executor.service.PipelineBuildService;
import com.autobuild.pipeline.executor.gen.dto.PipelineExecutionState;
import com.autobuild.pipeline.testutility.DummyData;

import jakarta.persistence.EntityNotFoundException;

public class PipelineBuildControllerTest {
    @Mock
    private PipelineBuildService service;

    @InjectMocks
    private PipelineBuildController controller;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void getPipelineBuildTest() {
        UUID pipelineBuildId = UUID.randomUUID();

        PipelineBuild dummyPipelineBuild = PipelineBuild.newBuilder()
                .setId(pipelineBuildId.toString())
                .setPipelineId(UUID.randomUUID().toString())
                .setPipelineName("Dummy Pipeline")
                .setCurrentState(PipelineExecutionState.PIPELINE_WAITING)
                .addAllStageBuilds(List.of(DummyData.getStageBuildDTO()))
                .build();

        doReturn(dummyPipelineBuild).when(service).getPipelineBuild(pipelineBuildId);

        ResponseEntity<PipelineBuild> response = controller.getPipelineBuild(pipelineBuildId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(pipelineBuildId.toString(), response.getBody().getId());
    }

    @Test
    public void getPipelineBuildWithInvalidUUIDTest() {
        doThrow(new EntityNotFoundException("dummy exception")).when(service).getPipelineBuild(any(UUID.class));

        assertThrows(EntityNotFoundException.class, () -> controller.getPipelineBuild(UUID.randomUUID()));
    }

    @Test
    public void getAllBuildsTest() {
        List<PipelineBuild> dummyBuilds = List.of(
                PipelineBuild.newBuilder()
                        .setId(UUID.randomUUID().toString())
                        .setPipelineId(UUID.randomUUID().toString())
                        .setPipelineName("Dummy Pipeline")
                        .setCurrentState(PipelineExecutionState.PIPELINE_WAITING)
                        .addAllStageBuilds(List.of(DummyData.getStageBuildDTO()))
                        .build(),
                PipelineBuild.newBuilder()
                        .setId(UUID.randomUUID().toString())
                        .setPipelineId(UUID.randomUUID().toString())
                        .setPipelineName("Dummy Pipeline")
                        .setCurrentState(PipelineExecutionState.PIPELINE_SUCCESS)
                        .addAllStageBuilds(List.of(DummyData.getStageBuildDTO()))
                        .build());

        doReturn(dummyBuilds).when(service).getAllBuilds();

        ResponseEntity<List<PipelineBuild>> response = controller.getAllBuilds();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    public void deletePipelineBuild() throws IOException {
        UUID pipelineBuildId = UUID.randomUUID();

        controller.deletePipelineBuild(pipelineBuildId);
        verify(service, times(1)).deletePipelineBuild(pipelineBuildId);
    }
}
