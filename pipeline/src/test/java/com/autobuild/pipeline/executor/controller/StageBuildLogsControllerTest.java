package com.autobuild.pipeline.executor.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.autobuild.pipeline.executor.service.StageBuildService;

import jakarta.persistence.EntityNotFoundException;

public class StageBuildLogsControllerTest {
    @Mock
    private StageBuildService service;

    @InjectMocks
    private StageBuildLogsController controller;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void getStageBuildLogsTest() throws IOException {
        doReturn(Map.of("log","hello world")).when(service).getStageBuildLogs(any(UUID.class));

        ResponseEntity<Map<String, String>> response = controller.getStageBuildLogs(UUID.randomUUID());

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        assertNotNull(response.getBody());
    }

    @Test
    public void getStageBuildLogsErrorTest() throws IOException {
        doThrow(new EntityNotFoundException("dummy exception")).when(service).getStageBuildLogs(any(UUID.class));
        
        assertThrows(EntityNotFoundException.class, () -> controller.getStageBuildLogs(UUID.randomUUID()));
    }
}
