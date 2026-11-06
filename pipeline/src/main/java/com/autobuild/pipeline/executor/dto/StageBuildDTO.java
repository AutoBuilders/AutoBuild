package com.autobuild.pipeline.executor.dto;

import java.util.UUID;

import com.autobuild.pipeline.executor.execution.state.StageExecutionState;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object for StageBuild.
 * 
 * @author Suvabrata Chowdhury
 */

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema(name = "StageBuild")
public class StageBuildDTO {
    private UUID id;

    private UUID stageId;

    private String stageName;

    private int stageOrder;

    private StageExecutionState currentState;

    public StageBuildDTO(UUID id, UUID stageId, String stageName, StageExecutionState currentState) {
        this(id, stageId, stageName, 0, currentState);
    }
}