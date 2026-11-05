package com.autobuild.pipeline.executor.job.impl;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

import com.autobuild.pipeline.definiton.entity.PipelineEntity;
import com.autobuild.pipeline.executor.entity.PipelineBuildEntity;
import com.autobuild.pipeline.executor.entity.StageBuildEntity;
import com.autobuild.pipeline.executor.execution.observer.PipelineExecutionObservable;
import com.autobuild.pipeline.executor.execution.state.PipelineExecutionStateInternal;
import com.autobuild.pipeline.executor.execution.state.StageExecutionStateInternal;
import com.autobuild.pipeline.executor.job.PipelineExecutor;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementation for asynchronously start executing pipeline build.
 *
 * @author Suvabrata Chowdhury
 */

@Slf4j
public class PipelineExecutorImpl implements PipelineExecutor {

    @Autowired
    private PipelineExecutionObservable pipelineExecutionObservable;

    @Async("executorService")
    @Override
    public void executePipeline(PipelineBuildEntity pipelineBuild) {
        try {
            log.info("inside executor impl submit");
            pipelineBuild.setCurrentState(PipelineExecutionStateInternal.WAITING);
            pipelineExecutionObservable.attachExecutionForObservation(pipelineBuild);

            this.startBuild(pipelineBuild);

            pipelineExecutionObservable.removeExecutionForObservation(pipelineBuild);
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void startBuild(PipelineBuildEntity pipelineBuild) throws IOException, InterruptedException {
        PipelineEntity pipeline = pipelineBuild.getPipeline();
        log.info("Starting Build for: " + pipeline.getId());

        startPipelineStageBuilds(pipelineBuild);
    }

    // TODO: Ugly design for now. Refactor later.
    private void startPipelineStageBuilds(PipelineBuildEntity pipelineBuild) throws IOException, InterruptedException {

        // TODO: with state manager. Finite State Machine
        setPipelineBuildState(pipelineBuild, PipelineExecutionStateInternal.RUNNING);

        boolean anyStageBuildFailed = false;

        for (final StageBuildEntity stageBuild : pipelineBuild.getStageBuilds()) {

            if (anyStageBuildFailed) {
                stageBuild.setCurrentState(StageExecutionStateInternal.STOPPED);

                continue;
            }

            // String logPath = pipelineFileService.createLogFile(pipelineBuild.getId(), stageBuild.getId());
            Process stageProcess = startStageProcess(stageBuild);

            setStageBuildState(pipelineBuild, stageBuild, StageExecutionStateInternal.RUNNING);

            log.info("process started with id: " + stageProcess.pid());

            int processExitCode = stageProcess.waitFor();
            if (processExitCode != 0) {
                anyStageBuildFailed = true;
                stageBuild.setCurrentState(StageExecutionStateInternal.FAILED);

                log.error("process exited with id: " + stageProcess.pid());
                continue;
            }

            setStageBuildState(pipelineBuild, stageBuild, StageExecutionStateInternal.SUCCESS);
        }

        if (anyStageBuildFailed) {
            pipelineBuild.setCurrentState(PipelineExecutionStateInternal.FAILED);
        } else {
            pipelineBuild.setCurrentState(PipelineExecutionStateInternal.SUCCESS);
        }

        pipelineExecutionObservable.notify(pipelineBuild);
    }

    private Process startStageProcess(final StageBuildEntity stageBuild) throws IOException {
        ProcessBuilder stageProcessBuilder = new ProcessBuilder(stageBuild.getStage().getPath());

        stageProcessBuilder.redirectOutput(Redirect.to(Path.of(stageBuild.getLogPath()).toFile()));
        stageProcessBuilder.redirectErrorStream(true);

        Process stageProcess = stageProcessBuilder.start();
        return stageProcess;
    }

    private void setStageBuildState(PipelineBuildEntity pipelineBuild, final StageBuildEntity stageBuild,
            final StageExecutionStateInternal stageExecutionState) {
        stageBuild.setCurrentState(stageExecutionState);
        pipelineExecutionObservable.notify(pipelineBuild);
    }

    private void setPipelineBuildState(PipelineBuildEntity pipelineBuild,
            PipelineExecutionStateInternal pipelineExecutionState) {
        pipelineBuild.setCurrentState(pipelineExecutionState);
        pipelineExecutionObservable.notify(pipelineBuild);
    }
}