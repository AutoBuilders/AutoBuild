package com.autobuild.pipeline.executor.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.autobuild.pipeline.configuration.FeatureFlagService;
import com.autobuild.pipeline.definiton.entity.Pipeline;
import com.autobuild.pipeline.definiton.repository.PipelineRepository;
import com.autobuild.pipeline.executor.dto.PipelineExecuteRequest;
import com.autobuild.pipeline.executor.dto.messaging.PipelineBuildRequestMessage;
import com.autobuild.pipeline.executor.job.PipelineExecutor;
import com.autobuild.pipeline.executor.messaging.PipelineBuildMessagePublisher;
import com.autobuild.pipeline.testutility.DummyData;

import jakarta.persistence.EntityNotFoundException;

/**
 * Unit tests for {@link PipelineExecutorService}.
 */
public class PipelineExecutorServiceTest {

    @Mock
    private PipelineRepository pipelineRepository;

    @Mock
    private FeatureFlagService featureFlagService;

    @Mock
    private PipelineBuildMessagePublisher messagePublisher;

    @Mock
    private PipelineExecutor pipelineExecutor;

    @InjectMocks
    private PipelineExecutorService pipelineExecutorService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void executePipeline_whenFlagEnabled_publishesMessageAndReturnsBuildId() {
        UUID pipelineId = UUID.randomUUID();
        Pipeline pipeline = DummyData.getPipeline(pipelineId);
        PipelineExecuteRequest request = new PipelineExecuteRequest(pipelineId);

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(featureFlagService.getBooleanValue(any())).thenReturn(true);

        UUID buildId = pipelineExecutorService.executePipeline(request);

        assertNotNull(buildId);
        verify(messagePublisher, times(1)).publish(any(PipelineBuildRequestMessage.class));
        verify(pipelineExecutor, never()).executePipeline(any());
    }

    @Test
    public void executePipeline_whenFlagDisabled_callsDirectExecutor() {
        UUID pipelineId = UUID.randomUUID();
        Pipeline pipeline = DummyData.getPipeline(pipelineId);
        PipelineExecuteRequest request = new PipelineExecuteRequest(pipelineId);

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(featureFlagService.getBooleanValue(any())).thenReturn(false);

        UUID buildId = pipelineExecutorService.executePipeline(request);

        assertNotNull(buildId);
        verify(messagePublisher, never()).publish(any());
        verify(pipelineExecutor, times(1)).executePipeline(any());
    }

    @Test
    public void executePipeline_whenPipelineNotFound_throwsEntityNotFoundException() {
        UUID pipelineId = UUID.randomUUID();
        PipelineExecuteRequest request = new PipelineExecuteRequest(pipelineId);

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> pipelineExecutorService.executePipeline(request));
        verify(messagePublisher, never()).publish(any());
        verify(pipelineExecutor, never()).executePipeline(any());
    }
}
