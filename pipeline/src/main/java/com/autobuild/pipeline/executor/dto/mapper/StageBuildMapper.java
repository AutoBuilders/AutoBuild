package com.autobuild.pipeline.executor.dto.mapper;

import org.springframework.stereotype.Component;

import com.autobuild.pipeline.definiton.dto.mapper.Mapper;
import com.autobuild.pipeline.executor.execution.state.StageExecutionStateInternal;
import com.autobuild.pipeline.executor.entity.StageBuildEntity;
import com.autobuild.pipeline.executor.dto.StageBuild;
import com.autobuild.pipeline.executor.dto.StageExecutionState;

/**
 * Used to map StageBuild to StageBuildDTO and vice versa.
 *
 * @author Suvabrata Chowdhury
 */

@Component
public class StageBuildMapper implements Mapper<StageBuild, StageBuildEntity> {

    @Override
    public StageBuildEntity dtoToEntity(StageBuild dto) {
        throw new UnsupportedOperationException("DTO is not persistable");
    }

    @Override
    public StageBuild entityToDto(StageBuildEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Given null object can not be mapped");
        }

        return StageBuild.newBuilder().setId(entity.getId().toString()).setStageId(entity.getStage().getId().toString())
                .setStageName(entity.getStage().getName()).setCurrentState(toProtoState(entity.getCurrentState()))
                .build();
    }

    private StageExecutionState toProtoState(StageExecutionStateInternal state) {
        switch (state) {
        case WAITING:
            return StageExecutionState.STAGE_WAITING;
        case RUNNING:
            return StageExecutionState.STAGE_RUNNING;
        case SUCCESS:
            return StageExecutionState.STAGE_SUCCESS;
        case FAILED:
            return StageExecutionState.STAGE_FAILED;
        case STOPPED:
            return StageExecutionState.STAGE_STOPPED;
        default:
            throw new IllegalArgumentException("Unknown state: " + state);
        }
    }
}
