package com.autobuild.pipeline.executor.dto.messaging;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Message payload published to the queue when a pipeline execution is requested.
 * Contains the pipeline's definition so the executor can process it independently.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PipelineBuildRequestMessage {

    private UUID buildId;
    private UUID pipelineId;
    private String pipelineName;
    private List<StageDefinition> stages;
}
