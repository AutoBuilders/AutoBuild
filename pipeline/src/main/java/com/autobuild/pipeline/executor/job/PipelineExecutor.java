package com.autobuild.pipeline.executor.job;

import org.springframework.stereotype.Component;

import com.autobuild.pipeline.executor.entity.PipelineBuildEntity;

/**
 * Implementation for asynchronously executing a pipeline.
 *
 * @author Suvabrata Chowdhury
 */

@Component
public interface PipelineExecutor {
    public void executePipeline(PipelineBuildEntity pipelineBuild);
}
