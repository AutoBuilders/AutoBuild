package com.autobuild.pipeline.executor.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import com.autobuild.pipeline.executor.entity.PipelineBuildEntity;
import com.autobuild.pipeline.executor.execution.observer.PipelineExecutionObservable;
import com.autobuild.pipeline.executor.repository.PipelineBuildRepository;
import com.autobuild.pipeline.testutility.DummyData;

import jakarta.persistence.EntityNotFoundException;

public class PipelineBuildContinuousUpdatorServiceTest {
    @Mock
    private PipelineBuildRepository pipelineBuildRepository;

    @Mock
    private PipelineExecutionObservable pipelineExecutionObservable;

    @InjectMocks
    private PipelineBuildContinuousUpdatorService pipelineBuildContinuousUpdatorService;

    private PipelineBuildEntity dummyBuild = DummyData.getPipelineBuild();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        ReflectionTestUtils.setField(pipelineBuildContinuousUpdatorService, "repository", pipelineBuildRepository);
        // pipelineBuildContinuousUpdatorService = new
        // PipelineBuildContinuousUpdatorService(pipelineExecutionObservable);
        // ReflectionTestUtils.setField(pipelineBuildContinuousUpdatorService,
        // "pipelineExecutionObservable", pipelineExecutionObservable);
    }

    @Test
    public void updateTest() {
        doReturn(Optional.of(dummyBuild)).when(pipelineBuildRepository).findById(any(UUID.class));

        pipelineBuildContinuousUpdatorService.update(dummyBuild);

        verify(pipelineBuildRepository, times(1)).findById(any(UUID.class));
        verify(pipelineBuildRepository, times(1)).save(eq(dummyBuild));
    }

    @Test
    public void updateEmptyBuildTest() {
        doReturn(Optional.empty()).when(pipelineBuildRepository).findById(any(UUID.class));

        assertThrows(EntityNotFoundException.class, () -> pipelineBuildContinuousUpdatorService.update(dummyBuild));

        verify(pipelineBuildRepository, times(1)).findById(any(UUID.class));
        verify(pipelineBuildRepository, times(0)).save(eq(dummyBuild));
    }
}
