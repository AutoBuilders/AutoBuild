package com.autobuild.pipeline.executor.execution.observer.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.autobuild.pipeline.executor.entity.PipelineBuildEntity;
import com.autobuild.pipeline.executor.execution.observer.PipelineExecutionObservable;
import com.autobuild.pipeline.executor.execution.observer.PipelineExecutionObserver;
import com.autobuild.pipeline.testutility.DummyData;

public class PipelineExecutionObservableImplTest {
    private PipelineExecutionObservable pipelineExecutionObservable;

    private PipelineBuildEntity dummyBuild = DummyData.getPipelineBuild();

    @BeforeEach
    public void setup() {
        pipelineExecutionObservable = new PipelineExecutionObservableImpl();
    }

    @Test
    public void attachExecutionForObservationTest() {
        pipelineExecutionObservable.attachExecutionForObservation(dummyBuild);

        List<UUID> allExecutions = pipelineExecutionObservable.getAllAttachedExecutions();

        assertEquals(1, allExecutions.size());
        assertEquals(allExecutions.get(0), dummyBuild.getId());
    }

    @Test
    public void attachExecutionForObservationErrorTest() {
        pipelineExecutionObservable.attachExecutionForObservation(dummyBuild);

        assertThrows(IllegalArgumentException.class,
                () -> pipelineExecutionObservable.attachExecutionForObservation(dummyBuild));
    }

    @Test
    public void removeExecutionForObservationTest() {
        pipelineExecutionObservable.attachExecutionForObservation(dummyBuild);

        pipelineExecutionObservable.removeExecutionForObservation(dummyBuild);
        List<UUID> allExecutions = pipelineExecutionObservable.getAllAttachedExecutions();

        assertTrue(allExecutions.isEmpty());
    }

    @Test
    public void removeExecutionForObservationErrorTest() {
        pipelineExecutionObservable.removeExecutionForObservation(dummyBuild);
    }

    @Test
    public void subscribeTest() {
        pipelineExecutionObservable.subscribe(dummyBuild, mock(PipelineExecutionObserver.class));

        List<PipelineExecutionObserver> subscribers = pipelineExecutionObservable
                .getAllSpecificSubscribedObservers(dummyBuild);

        assertNotNull(subscribers);
        assertEquals(1, subscribers.size());
    }

    @Test
    public void subscribeErrorTest() {
        assertThrows(IllegalArgumentException.class, () -> pipelineExecutionObservable.subscribe(null, null));
    }

    @Test
    public void unsubscribeTest() {
        PipelineExecutionObserver subscriber = mock(PipelineExecutionObserver.class);
        pipelineExecutionObservable.subscribe(dummyBuild, subscriber);
        pipelineExecutionObservable.unsubscribe(dummyBuild, subscriber);

        List<PipelineExecutionObserver> subscribers = pipelineExecutionObservable
                .getAllSpecificSubscribedObservers(dummyBuild);

        assertNull(subscribers);
    }

    @Test
    public void unSubscribeNullErrorTest() {
        assertThrows(IllegalArgumentException.class, () -> pipelineExecutionObservable.unsubscribe(null, null));
    }

    @Test
    public void subscribeAllTest() {
        PipelineExecutionObserver subscriber = mock(PipelineExecutionObserver.class);
        pipelineExecutionObservable.subscribe(subscriber);

        List<PipelineExecutionObserver> subscribers = pipelineExecutionObservable.getAllGeneralSubscribedObservers();

        assertNotNull(subscribers);
        assertEquals(1, subscribers.size());
        assertEquals(subscriber, subscribers.get(0));
    }

    @Test
    public void subscribeAllErrorTest() {
        assertThrows(IllegalArgumentException.class, () -> pipelineExecutionObservable.subscribe(null));
    }

    @Test
    public void unsubscribeAllTest() {
        PipelineExecutionObserver subscriber = mock(PipelineExecutionObserver.class);
        pipelineExecutionObservable.subscribe(subscriber);
        pipelineExecutionObservable.unsubscribe(subscriber);

        List<PipelineExecutionObserver> subscribers = pipelineExecutionObservable.getAllGeneralSubscribedObservers();

        assertNotNull(subscribers);
        assertEquals(0, subscribers.size());
    }

    @Test
    public void unsubscribeAllNullErrorTest() {
        assertThrows(IllegalArgumentException.class, () -> pipelineExecutionObservable.unsubscribe(null));
    }

    @Test
    public void notifyTest() {
        PipelineExecutionObserver subscriber = mock(PipelineExecutionObserver.class);
        PipelineExecutionObserver allSubscriber = mock(PipelineExecutionObserver.class);

        pipelineExecutionObservable.subscribe(dummyBuild, subscriber);
        pipelineExecutionObservable.subscribe(allSubscriber);

        pipelineExecutionObservable.notify(dummyBuild);

        verify(subscriber, times(1)).update(eq(dummyBuild));
        verify(allSubscriber, times(1)).update(eq(dummyBuild));
    }
}