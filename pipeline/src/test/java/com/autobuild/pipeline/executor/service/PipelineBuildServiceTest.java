package com.autobuild.pipeline.executor.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.autobuild.pipeline.executor.dto.mapper.PipelineBuildMapper;
import com.autobuild.pipeline.executor.entity.PipelineBuild;
import com.autobuild.pipeline.executor.execution.state.PipelineExecutionState;
import com.autobuild.pipeline.executor.repository.PipelineBuildRepository;
import com.autobuild.pipeline.testutility.DummyData;
import com.autobuild.pipeline.utility.file.PipelineFileService;

public class PipelineBuildServiceTest {
    @Mock
    private PipelineBuildRepository repository;

    @Mock
    private PipelineBuildMapper mapper;

    @Mock
    private PipelineFileService pipelineFileService;

    @InjectMocks
    private PipelineBuildService pipelineBuildService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void deletePipelineBuildTest() throws IOException {
        UUID buildId = UUID.randomUUID();

        PipelineBuild pipelineBuild = DummyData.getPipelineBuild(buildId);
        pipelineBuild.setCurrentState(PipelineExecutionState.SUCCESS);
        doReturn(Optional.of(pipelineBuild)).when(repository).findById(any(UUID.class));

        pipelineBuildService.deletePipelineBuild(buildId);

        verify(pipelineFileService, times(1)).removeLogFiles(eq(pipelineBuild));
        verify(repository, times(1)).deleteById(eq(buildId));

    }

    @Test
    public void deletePipelineBuildIllegalStateTest() throws IOException {
        UUID buildId = UUID.randomUUID();

        PipelineBuild pipelineBuild = DummyData.getPipelineBuild(buildId);
        doReturn(Optional.of(pipelineBuild)).when(repository).findById(any(UUID.class));

        assertThrows(IllegalStateException.class, () -> pipelineBuildService.deletePipelineBuild(buildId));

        pipelineBuild.setCurrentState(PipelineExecutionState.WAITING);

        assertThrows(IllegalStateException.class, () -> pipelineBuildService.deletePipelineBuild(buildId));
    }

    @Test
    public void getAllBuildsTest() {
        doReturn(List.of(DummyData.getPipelineBuild(UUID.randomUUID()),
                DummyData.getPipelineBuild(UUID.randomUUID()))).when(repository).findAll();

        pipelineBuildService.getAllBuilds();

        verify(mapper, times(2)).entityToDto(any(PipelineBuild.class));
    }

    @Test
    public void getPipelineBuildReturnsDto() {
        UUID buildId = UUID.randomUUID();
        PipelineBuild pipelineBuild = DummyData.getPipelineBuild(buildId);
        doReturn(Optional.of(pipelineBuild)).when(repository).findById(buildId);

        pipelineBuildService.getPipelineBuild(buildId);

        verify(mapper, times(1)).entityToDto(pipelineBuild);
    }

    @Test
    public void getPipelineBuildThrowsWhenNotFound() {
        UUID buildId = UUID.randomUUID();
        doReturn(Optional.empty()).when(repository).findById(buildId);

        assertThrows(jakarta.persistence.EntityNotFoundException.class,
                () -> pipelineBuildService.getPipelineBuild(buildId));
    }

    @Test
    public void deletePipelineBuildThrowsWhenNotFound() {
        UUID buildId = UUID.randomUUID();
        doReturn(Optional.empty()).when(repository).findById(buildId);

        assertThrows(jakarta.persistence.EntityNotFoundException.class,
                () -> pipelineBuildService.deletePipelineBuild(buildId));
    }
}
