package com.autobuild.pipeline.executor.execution.state;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Pipeline Build / execution state.
 *
 * @author Suvabrata Chowdhury
 */
@AllArgsConstructor
@Getter
public enum PipelineEntityExecutionState {
    WAITING("Pipeline is waiting to be executed."),
    RUNNING("Pipeline execution has started."),
    SUCCESS("Pipeline execution has finished and it succeeded."),
    FAILED("Pipeline execution has finished and it failed.");

    private String description;
}
