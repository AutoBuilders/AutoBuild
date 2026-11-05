package com.autobuild.pipeline.executor.execution.observer.impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.autobuild.pipeline.executor.entity.PipelineBuildEntity;
import com.autobuild.pipeline.executor.execution.observer.PipelineExecutionObservable;
import com.autobuild.pipeline.executor.execution.observer.PipelineExecutionObserver;

/**
 * Pipeline ongoing execution observer implementation.
 * 
 * @author Suvabrata Chowdhury
 */

public class PipelineExecutionObservableImpl implements PipelineExecutionObservable {

    private Map<UUID, List<PipelineExecutionObserver>> specificSubscribers = new ConcurrentHashMap<>();
    private List<PipelineExecutionObserver> allSubscribers = new CopyOnWriteArrayList<>();

    // Todo: may not be required
    @Override
    public void attachExecutionForObservation(PipelineBuildEntity pipelineBuild) {
        if (specificSubscribers.containsKey(pipelineBuild.getId())) {
            throw new IllegalArgumentException("Build is already being observed");
        }

        specificSubscribers.putIfAbsent(pipelineBuild.getId(), new CopyOnWriteArrayList<>());
    }

    // todo: may not be required
    @Override
    public void removeExecutionForObservation(PipelineBuildEntity pipelineBuild) {
        specificSubscribers.remove(pipelineBuild.getId());
    }

    @Override
    public List<UUID> getAllAttachedExecutions() {
        return specificSubscribers.keySet().stream().toList();
    }

    @Override
    public void subscribe(PipelineBuildEntity pipelineBuild, PipelineExecutionObserver subscriber) {
        if (null == subscriber || null == pipelineBuild) {
            throw new IllegalArgumentException("Null arguments given");
        }

        specificSubscribers.computeIfAbsent(
                pipelineBuild.getId(),
                id -> new CopyOnWriteArrayList<>()).add(subscriber);
    }

    @Override
    public void unsubscribe(PipelineBuildEntity pipelineBuild, PipelineExecutionObserver unsubscriber) {
        if (null == unsubscriber || null == pipelineBuild) {
            throw new IllegalArgumentException("Null arguments given");
        }

        specificSubscribers.computeIfPresent(pipelineBuild.getId(), (id, observers) -> {
            observers.remove(unsubscriber);
            return observers.isEmpty() ? null : observers;
        });
    }

    @Override
    public List<PipelineExecutionObserver> getAllSpecificSubscribedObservers(PipelineBuildEntity pipelineBuild) {
        return specificSubscribers.get(pipelineBuild.getId());
    }

    @Override
    public void subscribe(PipelineExecutionObserver subscriber) {
        if (null == subscriber) {
            throw new IllegalArgumentException("Null subscriber given");
        }

        allSubscribers.add(subscriber);
    }

    @Override
    public void unsubscribe(PipelineExecutionObserver unsubscriber) {
        if (null == unsubscriber) {
            throw new IllegalArgumentException("Null unsubscriber given");
        }

        allSubscribers.remove(unsubscriber);
    }

    @Override
    public List<PipelineExecutionObserver> getAllGeneralSubscribedObservers() {
        return List.copyOf(allSubscribers);
    }

    @Override
    public void notify(PipelineBuildEntity pipelineBuild) {
        if (null == pipelineBuild) {
            throw new IllegalArgumentException("Null pipeline build given");
        }

        List<PipelineExecutionObserver> specificObserversForPipeline = specificSubscribers.get(pipelineBuild.getId());

        if (specificObserversForPipeline != null) {
            specificObserversForPipeline.forEach(subscriber -> subscriber.update(pipelineBuild));
        }

        if (allSubscribers != null) {
            allSubscribers.forEach(subscriber -> subscriber.update(pipelineBuild));
        }
    }
}
