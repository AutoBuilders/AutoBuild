package com.autobuild.pipeline.executor.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.UUID;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;

import com.autobuild.pipeline.executor.dto.mapper.PipelineBuildMapper;
import com.autobuild.pipeline.executor.entity.PipelineBuildEntity;
import com.autobuild.pipeline.executor.execution.observer.PipelineExecutionObservable;
import com.autobuild.pipeline.executor.execution.state.PipelineExecutionStateInternal;
import com.autobuild.pipeline.executor.repository.PipelineBuildRepository;
import com.autobuild.pipeline.testutility.DummyData;

import jakarta.persistence.EntityNotFoundException;

public class PipelineBuildLiveServiceTest {
    @Mock
    private PipelineBuildRepository repository;

    @Mock
    private PipelineExecutionObservable executionObservable;

    @Mock
    private PipelineBuildMapper mapper;

    @InjectMocks
    private PipelineBuildLiveService pipelineBuildService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void addSubscriberTest() {
        UUID pipelineId = UUID.randomUUID();

        PipelineBuildEntity pipelineBuild = DummyData.getPipelineBuild();
        pipelineBuild.setId(pipelineId);
        pipelineBuild.setCurrentState(PipelineExecutionStateInternal.RUNNING);

        doReturn(Optional.of(pipelineBuild)).when(repository).findById(pipelineId);

        SseEmitter newSubscriber = mock(SseEmitter.class);

        pipelineBuildService.addSubscriber(newSubscriber, pipelineId);

        verify(newSubscriber, times(1)).onCompletion(any(Runnable.class));
        verify(newSubscriber, times(1)).onError(any(Consumer.class));
        verify(newSubscriber, times(1)).onTimeout(any(Runnable.class));
    }

    @Test
    public void addSubscriberNotRunningPipelineTest() {
        UUID pipelineId = UUID.randomUUID();

        PipelineBuildEntity pipelineBuild = DummyData.getPipelineBuild();
        pipelineBuild.setId(pipelineId);
        // pipelineBuild.setCurrentState(PipelineExecutionState.RUNNING);

        doReturn(Optional.of(pipelineBuild)).when(repository).findById(pipelineId);

        SseEmitter newSubscriber = mock(SseEmitter.class);

        assertThrows(UnsupportedOperationException.class,
                () -> pipelineBuildService.addSubscriber(newSubscriber, pipelineId));
    }

    @Test
    public void addSubscriberNoPipelineTest() {
        UUID pipelineId = UUID.randomUUID();
        // pipelineBuild.setCurrentState(PipelineExecutionState.RUNNING);

        doReturn(Optional.empty()).when(repository).findById(pipelineId);

        SseEmitter newSubscriber = mock(SseEmitter.class);

        assertThrows(EntityNotFoundException.class,
                () -> pipelineBuildService.addSubscriber(newSubscriber, pipelineId));
    }

    @Test
    public void updateTest() throws IOException {
        UUID pipelineId = UUID.randomUUID();

        PipelineBuildEntity pipelineBuild = DummyData.getPipelineBuild();
        pipelineBuild.setId(pipelineId);
        pipelineBuild.setCurrentState(PipelineExecutionStateInternal.RUNNING);

        doReturn(Optional.of(pipelineBuild)).when(repository).findById(pipelineId);

        SseEmitter newSubscriber = mock(SseEmitter.class);

        pipelineBuildService.addSubscriber(newSubscriber, pipelineId);

        pipelineBuildService.update(pipelineBuild);

        verify(newSubscriber, times(1)).send(any(SseEventBuilder.class));
    }

    @Test
    public void sentHeartBeatTest() throws IOException {
        UUID pipelineId = UUID.randomUUID();

        PipelineBuildEntity pipelineBuild = DummyData.getPipelineBuild();
        pipelineBuild.setId(pipelineId);
        pipelineBuild.setCurrentState(PipelineExecutionStateInternal.RUNNING);

        doReturn(Optional.of(pipelineBuild)).when(repository).findById(pipelineId);

        SseEmitter newSubscriber = mock(SseEmitter.class);

        pipelineBuildService.addSubscriber(newSubscriber, pipelineId);

        pipelineBuildService.sentHeartBeat();

        verify(newSubscriber, times(1)).send(any(SseEventBuilder.class));
    }
}