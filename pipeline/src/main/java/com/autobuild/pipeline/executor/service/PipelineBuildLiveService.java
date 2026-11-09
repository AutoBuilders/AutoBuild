package com.autobuild.pipeline.executor.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.autobuild.pipeline.executor.dto.mapper.PipelineBuildMapper;
import com.autobuild.pipeline.executor.entity.PipelineBuildEntity;
import com.autobuild.pipeline.executor.execution.observer.PipelineExecutionObservable;
import com.autobuild.pipeline.executor.execution.observer.PipelineExecutionObserver;
import com.autobuild.pipeline.executor.execution.state.PipelineEntityExecutionState;
import com.autobuild.pipeline.executor.repository.PipelineBuildRepository;

import jakarta.annotation.PreDestroy;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for sending SSE on change of pipeline or it's stages. Implemented as
 * an Observer.
 *
 * @author Suvabrata Chowdhury
 */
@Slf4j
@Service
public class PipelineBuildLiveService implements PipelineExecutionObserver {

    private static final int MAX_LIVE_SUBSCRIBERS = 50;

    private final ConcurrentHashMap<UUID, List<SseEmitter>> liveSubscribers = new ConcurrentHashMap<>();

    @Autowired
    private PipelineBuildRepository repository;

    @Autowired
    private PipelineExecutionObservable executionObservable;

    @Autowired
    private PipelineBuildMapper mapper;

    public void addSubscriber(SseEmitter newSubscriber, UUID pipelineBuildId) {
        List<SseEmitter> liveSubscribersForBuild = liveSubscribers.compute(pipelineBuildId, (id, emitters) -> {
            if (emitters == null) {
                emitters = new CopyOnWriteArrayList<>();
            }

            if (MAX_LIVE_SUBSCRIBERS <= emitters.size()) {
                UnsupportedOperationException exception = new UnsupportedOperationException(
                        "Maximum number of subscribers exceeded");
                newSubscriber.completeWithError(exception);
                throw exception;
            }

            return emitters;
        });

        Optional<PipelineBuildEntity> optionalPipelineBuild = repository.findById(pipelineBuildId);

        if (optionalPipelineBuild.isEmpty()) {
            EntityNotFoundException entityNotFoundException = new EntityNotFoundException(
                    "Pipeline Build with id: " + pipelineBuildId + " does not exist");
            newSubscriber.completeWithError(entityNotFoundException);
            throw entityNotFoundException;
        }

        if (!optionalPipelineBuild.get().getCurrentState().equals(PipelineEntityExecutionState.RUNNING)) {
            UnsupportedOperationException exception = new UnsupportedOperationException(
                    "Live broadcast is not suppoerted as build is not in running state");
            newSubscriber.completeWithError(exception);
            throw exception;
        }

        liveSubscribersForBuild.add(newSubscriber);

        newSubscriber.onCompletion(() -> {
            liveSubscribersForBuild.remove(newSubscriber);
            executionObservable.unsubscribe(optionalPipelineBuild.get(), this);
        });

        newSubscriber.onError(throwable -> {
            log.error(throwable.getMessage(), throwable);
            liveSubscribersForBuild.remove(newSubscriber);
            executionObservable.unsubscribe(optionalPipelineBuild.get(), this);
        });

        newSubscriber.onTimeout(() -> {
            liveSubscribersForBuild.remove(newSubscriber);
            executionObservable.unsubscribe(optionalPipelineBuild.get(), this);
        });

        executionObservable.subscribe(optionalPipelineBuild.get(), this);
    }

    @Override
    public void update(PipelineBuildEntity pipelineBuild) {
        List<SseEmitter> subscriberEmitters = liveSubscribers.get(pipelineBuild.getId());

        List<SseEmitter> deadEmitters = new ArrayList<>();

        if (subscriberEmitters != null) {
            for (SseEmitter subscriberEmitter : subscriberEmitters) {
                try {
                    subscriberEmitter.send(SseEmitter.event().data(mapper.entityToDto(pipelineBuild)));

                    if (pipelineBuild.getCurrentState().equals(PipelineEntityExecutionState.SUCCESS)
                            || pipelineBuild.getCurrentState().equals(PipelineEntityExecutionState.FAILED)) {
                        subscriberEmitter.complete();
                        deadEmitters.add(subscriberEmitter);
                    }

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    deadEmitters.add(subscriberEmitter);
                }
            }

            liveSubscribers.computeIfPresent(pipelineBuild.getId(), (id, emitters) -> {
                if (null == emitters) {
                    return null;
                }
                emitters.removeAll(deadEmitters);
                return emitters.isEmpty() ? null : emitters;
            });
        } else {
            executionObservable.unsubscribe(pipelineBuild, this);
        }
    }

    // TODO: Better implementation as this method is executed unconditionally
    @Scheduled(fixedRate = 15000)
    public void sentHeartBeat() {

        liveSubscribers.forEach((id, subscriberEmitters) -> {
            List<SseEmitter> deadEmitters = new ArrayList<>();

            for (SseEmitter sseEmitter : subscriberEmitters) {
                try {
                    sseEmitter.send(SseEmitter.event().comment("heartbeat pulse to keep connection open"));
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    deadEmitters.add(sseEmitter);
                }
            }

            liveSubscribers.computeIfPresent(id, (key, emitters) -> {
                emitters.removeAll(deadEmitters);
                return emitters.isEmpty() ? null : emitters;
            });
        });
    }

    @PreDestroy
    public void forceCloseAllEmitters() {
        liveSubscribers.forEach((id, emitters) -> {
            emitters.forEach(emitter -> {
                try {
                    emitter.complete();
                } catch (Exception ignored) {
                }
            });
        });
        liveSubscribers.clear();
    }
}
