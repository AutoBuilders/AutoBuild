package com.autobuild.pipeline.executor.job.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.lang.ProcessBuilder.Redirect;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockitoAnnotations;

import com.autobuild.pipeline.executor.entity.PipelineBuildEntity;
import com.autobuild.pipeline.executor.execution.observer.PipelineExecutionObservable;
import com.autobuild.pipeline.executor.execution.state.PipelineExecutionStateInternal;
import com.autobuild.pipeline.executor.execution.state.StageExecutionStateInternal;
import com.autobuild.pipeline.executor.job.PipelineExecutor;
import com.autobuild.pipeline.testutility.DummyData;

public class PipelineExecutorImplTest {
    @Mock
    private PipelineExecutionObservable pipelineExecutionObservable;

    @InjectMocks
    private PipelineExecutor pipelineExecutor = new PipelineExecutorImpl();

    private PipelineBuildEntity pipelineBuild = DummyData.getPipelineBuild();
    
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void executePipelineSuccessTest() throws InterruptedException {
        Process stageProcess = mock(Process.class);
        doReturn(0).when(stageProcess).waitFor();

        try (MockedConstruction<ProcessBuilder> processBuilderMock = mockConstruction(ProcessBuilder.class, (mock, context) -> {
            doReturn(stageProcess).when(mock).start();
        })) {
            pipelineExecutor.executePipeline(pipelineBuild);

            ProcessBuilder mockProcessBuilderInstance = processBuilderMock.constructed().get(0);

            verify(mockProcessBuilderInstance, times(1)).redirectOutput(any(Redirect.class));
            verify(mockProcessBuilderInstance, times(1)).redirectErrorStream(true);
            
            assertTrue(pipelineBuild.getCurrentState().equals(PipelineExecutionStateInternal.SUCCESS) && pipelineBuild.getStageBuilds().stream().filter(stageBuild -> !stageBuild.getCurrentState().equals(StageExecutionStateInternal.SUCCESS)).toList().size() == 0);
        }
    }

    @Test
    public void executePipelineFailureTest() throws InterruptedException {
        Process stageProcess = mock(Process.class);
        doReturn(1).when(stageProcess).waitFor();

        try (MockedConstruction<ProcessBuilder> processBuilderMock = mockConstruction(ProcessBuilder.class, (mock, context) -> {
            doReturn(stageProcess).when(mock).start();
        })) {
            pipelineExecutor.executePipeline(pipelineBuild);

            ProcessBuilder mockProcessBuilderInstance = processBuilderMock.constructed().get(0);

            verify(mockProcessBuilderInstance, times(1)).redirectOutput(any(Redirect.class));
            verify(mockProcessBuilderInstance, times(1)).redirectErrorStream(true);
            
            assertFalse(pipelineBuild.getCurrentState().equals(PipelineExecutionStateInternal.SUCCESS) && pipelineBuild.getStageBuilds().stream().filter(stageBuild -> !stageBuild.getCurrentState().equals(StageExecutionStateInternal.SUCCESS)).toList().size() == 0);
        }
    }

    //TODO: add some success, some failed some stopped stage tests 
}
