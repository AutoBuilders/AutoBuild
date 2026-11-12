package com.autobuild.pipeline.executor.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.autobuild.pipeline.executor.execution.state.PipelineExecutionState;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object for PipelineBuild.
 * 
 * @author Suvabrata Chowdhury
 */

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema(name = "PipelineBuild")
public class PipelineBuildDTO {
    private UUID id;
    // private int buildNo;

    private UUID pipelineId;

    private String pipelineName;

    private PipelineExecutionState currentState;

	private Instant startedAt;
    
    private List<StageBuildDTO> stageBuilds;
}
