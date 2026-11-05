package com.autobuild.pipeline.executor.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autobuild.pipeline.executor.entity.PipelineBuildEntity;
import com.autobuild.pipeline.executor.execution.observer.PipelineExecutionObservable;
import com.autobuild.pipeline.executor.execution.observer.PipelineExecutionObserver;
import com.autobuild.pipeline.executor.repository.PipelineBuildRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for continuously updating pipeline on it's stage change. Implementated as an observer.
 * 
 * @author Suvabrata Chowdhury
 */

@Slf4j
@Service
public class PipelineBuildContinuousUpdatorService implements PipelineExecutionObserver{

    @Autowired
    private PipelineBuildRepository repository;

    private PipelineExecutionObservable pipelineExecutionObservable;

    //TODO: check this dependency injection
    @Autowired
    public PipelineBuildContinuousUpdatorService(PipelineExecutionObservable pipelineExecutionObservable) {
        this.pipelineExecutionObservable = pipelineExecutionObservable;
        this.pipelineExecutionObservable.subscribe(this);
    }

    public PipelineBuildContinuousUpdatorService() {
        pipelineExecutionObservable.subscribe(this);
    }

    @Transactional
    @Override
    public void update(PipelineBuildEntity pipelineBuild) {
        log.info("Updating build with state: " + pipelineBuild.getCurrentState());
        
        Optional<PipelineBuildEntity> optionalBuild = repository.findById(pipelineBuild.getId());

        if (optionalBuild.isEmpty()) {
            throw new EntityNotFoundException("Pipeline build with id " + pipelineBuild.getId() + " does not exist");
        }

        repository.save(pipelineBuild);

        log.info("Updated build: " + pipelineBuild.getCurrentState());
    }

    //TODO: check for predestroy unsubscribe inclusion
}
