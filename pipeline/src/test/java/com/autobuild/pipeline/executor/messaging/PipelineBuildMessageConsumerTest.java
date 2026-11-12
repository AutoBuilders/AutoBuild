package com.autobuild.pipeline.executor.messaging;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
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

import com.autobuild.pipeline.executor.entity.PipelineBuild;
import com.autobuild.pipeline.executor.job.PipelineExecutor;
import com.autobuild.pipeline.executor.repository.PipelineBuildRepository;
import com.autobuild.pipeline.testutility.DummyData;

import jakarta.persistence.EntityNotFoundException;

/**
 * Unit tests for {@link PipelineBuildMessageConsumer}.
 *
 * @author Suvabrata Chowdhury
 */
public class PipelineBuildMessageConsumerTest {

    @Mock
    private PipelineBuildRepository buildRepository;

    @Mock
    private PipelineExecutor pipelineExecutor;

    @InjectMocks
    private PipelineBuildMessageConsumer consumer;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void consume_whenBuildExists_executesIt() {
        UUID pipelineId = UUID.randomUUID();
        PipelineBuild build = DummyData.getPipelineBuild(pipelineId);

        when(buildRepository.findByIdWithStageBuilds(build.getId())).thenReturn(Optional.of(build));

        consumer.consume(build.getId().toString());

        verify(pipelineExecutor, times(1)).executePipeline(eq(build));
    }

    @Test
    public void consume_whenBuildNotFound_throwsEntityNotFoundException() {
        UUID buildId = UUID.randomUUID();

        when(buildRepository.findByIdWithStageBuilds(buildId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> consumer.consume(buildId.toString()));
    }
}