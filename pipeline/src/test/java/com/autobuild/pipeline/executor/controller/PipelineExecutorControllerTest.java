package com.autobuild.pipeline.executor.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.autobuild.pipeline.executor.dto.PipelineBuild;
import com.autobuild.pipeline.executor.dto.PipelineExecuteRequest;
import com.autobuild.pipeline.executor.service.PipelineExecutorService;
import com.autobuild.pipeline.testutility.DummyData;

public class PipelineExecutorControllerTest {
    @Mock
    private PipelineExecutorService service;

    @InjectMocks
    private PipelineExecutorController controller;

    private PipelineExecuteRequest executeRequest = DummyData.getPipelineRequest();
    private PipelineBuild pipelineBuild = DummyData.getPipelineBuildDTO(executeRequest.getPipelineId());

    @BeforeEach
    public void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);

        doReturn(pipelineBuild).when(service).executePipeline(executeRequest);
    }

    @Test
    public void testExecutePipeline() throws IOException {
        ResponseEntity<com.autobuild.pipeline.executor.dto.PipelineBuild> response = controller.executePipeline(executeRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());

        PipelineBuild responseDto = response.getBody();

        assertNotNull(responseDto);
        assertEquals(executeRequest.getPipelineId().toString(), responseDto.getPipelineId());

        assertTrue(responseDto.getStageBuildsList().size() > 0);
    }

    @Test
    public void testExecutePipelineRuntimeError() throws IOException {
        doThrow(new RuntimeException("Something runtime exception")).when(service).executePipeline(any(PipelineExecuteRequest.class));

        assertThrows(RuntimeException.class, () -> controller.executePipeline(executeRequest));
    }
}
