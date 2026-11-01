package com.autobuild.pipeline.executor.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.autobuild.pipeline.executor.entity.StageBuild;
import com.autobuild.pipeline.executor.repository.StageBuildRepository;
import com.autobuild.pipeline.testutility.DummyData;
import com.autobuild.pipeline.utility.file.PipelineFileService;

import jakarta.persistence.EntityNotFoundException;

public class StageBuildServiceTest {

    @Mock
    private StageBuildRepository repository;

    @Mock
    private PipelineFileService pipelineFileService;

    @InjectMocks
    private StageBuildService stageBuildService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getStageBuildLogsReturnsLogMap() throws IOException {
        UUID stageBuildId = UUID.randomUUID();
        StageBuild stageBuild = DummyData.getStageBuild();
        doReturn(Optional.of(stageBuild)).when(repository).findById(stageBuildId);
        doReturn("log line 1\nlog line 2").when(pipelineFileService).readStageBuildLogFile(stageBuild);

        var result = stageBuildService.getStageBuildLogs(stageBuildId);

        verify(pipelineFileService, times(1)).readStageBuildLogFile(any(StageBuild.class));
        assert result.containsKey("log");
    }

    @Test
    void getStageBuildLogsThrowsWhenNotFound() {
        UUID stageBuildId = UUID.randomUUID();
        doReturn(Optional.empty()).when(repository).findById(stageBuildId);

        assertThrows(EntityNotFoundException.class,
                () -> stageBuildService.getStageBuildLogs(stageBuildId));
    }
}
