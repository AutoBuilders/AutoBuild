package com.autobuild.pipeline.testutility;

import java.util.List;
import java.util.UUID;

import com.autobuild.pipeline.definiton.gen.dto.Pipeline;
import com.autobuild.pipeline.definiton.gen.dto.Stage;
import com.autobuild.pipeline.definiton.entity.PipelineEntity;
import com.autobuild.pipeline.definiton.entity.StageEntity;
import com.autobuild.pipeline.executor.gen.dto.PipelineBuild;
import com.autobuild.pipeline.executor.gen.dto.StageBuild;
import com.autobuild.pipeline.executor.dto.PipelineExecuteRequest;
import com.autobuild.pipeline.executor.gen.dto.PipelineExecutionState;
import com.autobuild.pipeline.executor.gen.dto.StageExecutionState;
import com.autobuild.pipeline.executor.entity.PipelineBuildEntity;
import com.autobuild.pipeline.executor.entity.StageBuildEntity;
import com.autobuild.pipeline.executor.execution.state.PipelineEntityExecutionState;
import com.autobuild.pipeline.executor.execution.state.StageEntityExecutionState;

public class DummyData {

    //Pipeline Crud
    public static StageEntity getStage() {
        return new StageEntity(UUID.randomUUID(),"dummyStage1","bash","./dummyFolder/");
    }

    public static  Stage getStageDTO() {
        return Stage.newBuilder()
            .setId(UUID.randomUUID().toString())
            .setName("dummyStage1")
            .setScriptType("bash")
            .setCommand("echo Hello")
            .build();
    }

    public static  PipelineEntity getPipeline() {
        return new PipelineEntity(UUID.randomUUID(), "my pipeline",List.of(getStage()));
    }

    public static  PipelineEntity getPipeline(UUID pipelineId) {
        return new PipelineEntity(pipelineId, "my pipeline",List.of(getStage()));
    }

    public static Pipeline getPipelineDTO() {
        return Pipeline.newBuilder()
            .setId(UUID.randomUUID().toString())
            .setName("my pipeline")
            .addAllStages(List.of(getStageDTO()))
            .build();
    }

    //Pipeline Execution
    public static PipelineExecuteRequest getPipelineRequest() {
        return new PipelineExecuteRequest(UUID.randomUUID());
    }

    public static StageBuildEntity getStageBuild() {
        return new StageBuildEntity(UUID.randomUUID(), DummyData.getStage(), StageEntityExecutionState.WAITING, "./dummyFolder/");
    }

    public static PipelineBuildEntity getPipelineBuild() {
        return new PipelineBuildEntity(UUID.randomUUID(), DummyData.getPipeline(), List.of(DummyData.getStageBuild()), PipelineEntityExecutionState.WAITING);
    }

    public static PipelineBuildEntity getPipelineBuild(UUID pipelineUuid) {
        return new PipelineBuildEntity(UUID.randomUUID(), DummyData.getPipeline(pipelineUuid), List.of(DummyData.getStageBuild()), PipelineEntityExecutionState.WAITING);
    }

    public static StageBuild getStageBuildDTO() {
        return StageBuild.newBuilder()
            .setId(UUID.randomUUID().toString())
            .setStageId(UUID.randomUUID().toString())
            .setStageName("Dummy Stage")
            .setCurrentState(StageExecutionState.STAGE_WAITING)
            .build();
    }

    public static PipelineBuild getPipelineBuildDTO() {
        return PipelineBuild.newBuilder()
            .setId(UUID.randomUUID().toString())
            .setPipelineId(UUID.randomUUID().toString())
            .setPipelineName("Dummy Pipeline")
            .setCurrentState(PipelineExecutionState.PIPELINE_WAITING)
            .addAllStageBuilds(List.of(DummyData.getStageBuildDTO()))
            .build();
    }

    public static PipelineBuild getPipelineBuildDTO(UUID pipelineId) {
        return PipelineBuild.newBuilder()
            .setId(UUID.randomUUID().toString())
            .setPipelineId(pipelineId.toString())
            .setPipelineName("Dummy Pipeline")
            .setCurrentState(PipelineExecutionState.PIPELINE_WAITING)
            .addAllStageBuilds(List.of(DummyData.getStageBuildDTO()))
            .build();
    }
}