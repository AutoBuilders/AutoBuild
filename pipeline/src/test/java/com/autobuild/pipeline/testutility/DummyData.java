package com.autobuild.pipeline.testutility;

import java.util.List;
import java.util.UUID;

import com.autobuild.pipeline.definiton.dto.PipelineDTO;
import com.autobuild.pipeline.definiton.dto.StageDTO;
import com.autobuild.pipeline.definiton.entity.Pipeline;
import com.autobuild.pipeline.definiton.entity.Stage;
import com.autobuild.pipeline.executor.dto.PipelineBuildDTO;
import com.autobuild.pipeline.executor.dto.PipelineExecuteRequest;
import com.autobuild.pipeline.executor.dto.StageBuildDTO;
import com.autobuild.pipeline.executor.dto.messaging.PipelineBuildRequestMessage;
import com.autobuild.pipeline.executor.dto.messaging.StageDefinition;
import com.autobuild.pipeline.executor.entity.PipelineBuild;
import com.autobuild.pipeline.executor.entity.StageBuild;
import com.autobuild.pipeline.executor.execution.state.PipelineExecutionState;
import com.autobuild.pipeline.executor.execution.state.StageExecutionState;

public class DummyData {

    //Pipeline Crud
    public static Stage getStage() {
        return new Stage(UUID.randomUUID(),"dummyStage1","bash","./dummyFolder/");
    }

    public static  StageDTO getStageDTO() {
        return new StageDTO(UUID.randomUUID(), "dummyStage1", "bash", "echo Hello");
    }

    public static  Pipeline getPipeline() {
        return new Pipeline(UUID.randomUUID(), "my pipeline",List.of(getStage()));
    }

    public static  Pipeline getPipeline(UUID pipelineId) {
        return new Pipeline(pipelineId, "my pipeline",List.of(getStage()));
    }

    public static PipelineDTO getPipelineDTO() {
        return new PipelineDTO(UUID.randomUUID(), "my pipeline", List.of(getStageDTO()));
    }

    //Pipeline Execution
    public static PipelineExecuteRequest getPipelineRequest() {
        return new PipelineExecuteRequest(UUID.randomUUID());
    }

    public static StageBuild getStageBuild() {
        return new StageBuild(UUID.randomUUID(), DummyData.getStage(), StageExecutionState.WAITING, "./dummyFolder/");
    }

    public static PipelineBuild getPipelineBuild() {
        return new PipelineBuild(UUID.randomUUID(), DummyData.getPipeline(), List.of(DummyData.getStageBuild()), PipelineExecutionState.WAITING);
    }

    public static PipelineBuild getPipelineBuild(UUID pipelineUuid) {
        return new PipelineBuild(UUID.randomUUID(), DummyData.getPipeline(pipelineUuid), List.of(DummyData.getStageBuild()), PipelineExecutionState.WAITING);
    }

    public static StageBuildDTO getStageBuildDTO() {
        return new StageBuildDTO(UUID.randomUUID(), UUID.randomUUID(), "Dummy Stage", StageExecutionState.WAITING);
    }

    public static PipelineBuildDTO getPipelineBuildDTO() {
        return new PipelineBuildDTO(UUID.randomUUID(), UUID.randomUUID(), "Dummy Pipeline", PipelineExecutionState.WAITING, List.of(DummyData.getStageBuildDTO()));
    }

    public static PipelineBuildDTO getPipelineBuildDTO(UUID pipelineId) {
        return new PipelineBuildDTO(UUID.randomUUID(), pipelineId, "Dummy Pipeline", PipelineExecutionState.WAITING, List.of(DummyData.getStageBuildDTO()));
    }

    public static PipelineBuildRequestMessage getPipelineBuildRequestMessage(UUID pipelineId) {
        StageDefinition stage = new StageDefinition("dummyStage1", "bash", "./dummyFolder/", 0);
        return new PipelineBuildRequestMessage(UUID.randomUUID(), pipelineId, "my pipeline", List.of(stage));
    }
}