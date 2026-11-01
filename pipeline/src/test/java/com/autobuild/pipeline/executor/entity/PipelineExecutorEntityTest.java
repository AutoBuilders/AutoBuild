package com.autobuild.pipeline.executor.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.autobuild.pipeline.executor.execution.state.PipelineExecutionState;
import com.autobuild.pipeline.executor.execution.state.StageExecutionState;
import com.autobuild.pipeline.testutility.DummyData;

public class PipelineExecutorEntityTest {

    @Test
    void pipelineBuildSetDefaultStateSetsWaitingWhenNull() {
        PipelineBuild build = new PipelineBuild();
        assertNull(build.getCurrentState());

        build.setDefaultState();

        assertEquals(PipelineExecutionState.WAITING, build.getCurrentState());
    }

    @Test
    void pipelineBuildSetDefaultStateDoesNotOverwriteExistingState() {
        PipelineBuild build = DummyData.getPipelineBuild();
        build.setCurrentState(PipelineExecutionState.SUCCESS);

        build.setDefaultState();

        assertEquals(PipelineExecutionState.SUCCESS, build.getCurrentState());
    }

    @Test
    void stageBuildSetDefaultStateSetsWaitingWhenNull() {
        StageBuild stageBuild = new StageBuild();
        assertNull(stageBuild.getCurrentState());

        stageBuild.setDefaultState();

        assertEquals(StageExecutionState.WAITING, stageBuild.getCurrentState());
    }

    @Test
    void stageBuildSetDefaultStateDoesNotOverwriteExistingState() {
        StageBuild stageBuild = DummyData.getStageBuild();
        stageBuild.setCurrentState(StageExecutionState.SUCCESS);

        stageBuild.setDefaultState();

        assertEquals(StageExecutionState.SUCCESS, stageBuild.getCurrentState());
    }
}
