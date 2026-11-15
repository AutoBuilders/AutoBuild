package com.autobuild.pipeline.executor.service;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autobuild.pipeline.executor.entity.StageBuildEntity;
import com.autobuild.pipeline.executor.repository.StageBuildRepository;
import com.autobuild.pipeline.utility.file.PipelineFileService;

import jakarta.persistence.EntityNotFoundException;

/**
 * Service layer for stage build.
 * 
 * @author Suvabrata Chowdhury
 */

@Service
public class StageBuildService {

    @Autowired
    private PipelineFileService pipelineFileService;

    @Autowired
    private StageBuildRepository repository;

    public Map<String, String> getStageBuildLogs(UUID stageBuildId) throws IOException {
        Optional<StageBuildEntity> optionalStageBuild = repository.findById(stageBuildId);

        if (optionalStageBuild.isEmpty()) {
            throw new EntityNotFoundException(
                    "Stage Build with id: " + stageBuildId + " does not exist");
        }

        String logs = pipelineFileService.readStageBuildLogFile(optionalStageBuild.get());

        return Map.of("log", logs);
    }

    // @Async
    // public void getStageBuildLiveLogs(final SseEmitter emitter, final UUID stageBuildId) {
    //     Optional<StageBuild> optionalStageBuild = repository.findById(stageBuildId);

    //     while (optionalStageBuild.isPresent()) {
    //         if (!optionalStageBuild.get().getCurrentState().equals(StageExecutionState.RUNNING)) 
    //             emitter.complete();
    //         }

    //         String logs = pipelineFileService.readStageBuildLogs(optionalStageBuild.get());

    //         emitter.send(Map.of("log",logs));

    //         Thread.sleep();
    //     }
    // }
}
