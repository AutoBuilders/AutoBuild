package com.autobuild.pipeline.executor.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.autobuild.pipeline.executor.dto.PipelineExecuteRequest;
import com.autobuild.pipeline.executor.service.PipelineExecutorService;
import com.autobuild.pipeline.testutility.DummyData;

/**
 * Unit tests for {@link PipelineExecutorController}.
 */
public class PipelineExecutorControllerTest {

    @Mock
    private PipelineExecutorService service;

    @InjectMocks
    private PipelineExecutorController controller;

    private PipelineExecuteRequest executeRequest = DummyData.getPipelineRequest();
    private UUID buildId = UUID.randomUUID();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        doReturn(buildId).when(service).executePipeline(executeRequest);
    }

    @Test
    public void testExecutePipeline() {
        ResponseEntity<Map<String, UUID>> response = controller.executePipeline(executeRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(buildId, response.getBody().get("buildId"));
    }

    @Test
    public void testExecutePipelineRuntimeError() {
        doThrow(new RuntimeException("Something runtime exception"))
                .when(service).executePipeline(any(PipelineExecuteRequest.class));

        assertThrows(RuntimeException.class, () -> controller.executePipeline(executeRequest));
    }
}
