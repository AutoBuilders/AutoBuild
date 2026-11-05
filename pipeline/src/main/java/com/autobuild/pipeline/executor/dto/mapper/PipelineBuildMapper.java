package com.autobuild.pipeline.executor.dto.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.autobuild.pipeline.definiton.dto.mapper.Mapper;
import com.autobuild.pipeline.executor.execution.state.PipelineExecutionStateInternal;
import com.autobuild.pipeline.executor.dto.PipelineExecutionState;

import java.util.List;
import com.autobuild.pipeline.executor.entity.PipelineBuildEntity;
import com.autobuild.pipeline.executor.dto.PipelineBuild;
import com.autobuild.pipeline.executor.dto.StageBuild;

/**
 * Used to map PipelineBuild to PipelineBuildDTO and vice versa.
 * 
 * @author Suvabrata Chowdhury
 */

@Component
public class PipelineBuildMapper implements Mapper<
        PipelineBuild,
        PipelineBuildEntity> {

    @Autowired
    private StageBuildMapper stageBuildMapper;

    @Override
    public PipelineBuildEntity dtoToEntity(
            PipelineBuild dto) {
        throw new UnsupportedOperationException("DTO is not persistable");
    }

    @Override
    public PipelineBuild entityToDto(
            PipelineBuildEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Given null object can not be mapped");
        }

        List<StageBuild> protoStageBuilds = entity.getStageBuilds()
            .stream()
            .map(stageBuildMapper::entityToDto)
            .toList();

        return PipelineBuild.newBuilder()
            .setId(entity.getId().toString())
            .setPipelineId(entity.getPipeline().getId().toString())
            .setPipelineName(entity.getPipeline().getName())
            .setCurrentState(toProtoState(entity.getCurrentState()))
            .addAllStageBuilds(protoStageBuilds)
            .build();
    }

    private PipelineExecutionState toProtoState(
            PipelineExecutionStateInternal state) {
        switch (state) {
            case WAITING: return PipelineExecutionState.PIPELINE_WAITING;
            case RUNNING: return PipelineExecutionState.PIPELINE_RUNNING;
            case SUCCESS: return PipelineExecutionState.PIPELINE_SUCCESS;
            case FAILED:  return PipelineExecutionState.PIPELINE_FAILED;
            default: throw new IllegalArgumentException("Unknown state: " + state);
        }
    }
}
