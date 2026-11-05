package com.autobuild.pipeline.executor.execution.observer;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.autobuild.pipeline.executor.entity.PipelineBuildEntity;

/**
 * Pipeline ongoing build (or execution) observable interface.
 *
 * @author Suvabrata Chowdhury
 */

@Component
public interface PipelineExecutionObservable {
    public void attachExecutionForObservation(PipelineBuildEntity pipelineBuild);

    public void removeExecutionForObservation(PipelineBuildEntity pipelineBuild);

    public List<UUID> getAllAttachedExecutions();

    // subscribe for specific build
    public void subscribe(PipelineBuildEntity pipelineBuild, PipelineExecutionObserver subscriber);

    public void unsubscribe(PipelineBuildEntity pipelineBuild, PipelineExecutionObserver unsubscriber);

    public List<PipelineExecutionObserver> getAllSpecificSubscribedObservers(PipelineBuildEntity pipelineBuild);

    // subscribe for any build
    public void subscribe(PipelineExecutionObserver subscriber);

    public void unsubscribe(PipelineExecutionObserver unsubscriber);

    public List<PipelineExecutionObserver> getAllGeneralSubscribedObservers();

    // notify observers
    public void notify(PipelineBuildEntity pipelineBuild);
}
