package com.autobuild.pipeline.definiton.service;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autobuild.pipeline.definiton.dto.mapper.PipelineMapper;
import com.autobuild.pipeline.definiton.dto.mapper.StageMapper;
import com.autobuild.pipeline.definiton.entity.PipelineEntity;
import com.autobuild.pipeline.definiton.entity.StageEntity;
import com.autobuild.pipeline.definiton.exceptions.DuplicateEntryException;
import com.autobuild.pipeline.definiton.exceptions.InvalidIdException;
import com.autobuild.pipeline.definiton.repository.PipelineRepository;
import com.autobuild.pipeline.utility.file.PipelineFileService;
import com.autobuild.pipeline.utility.file.extension.Extensions;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for pipeline CRUD operations.
 * 
 * @author Suvabrata Chowdhury
 */
@Slf4j
@Service
public class PipelineService {

    @Autowired
    private PipelineRepository repository;

    @Autowired
    private PipelineMapper mapper;

    @Autowired
    private PipelineFileService fileService;

    @Autowired
    private StageMapper stageMapper;

    public com.autobuild.pipeline.definiton.dto.Pipeline getPipelineById(final String pipelineId)
            throws InvalidIdException, IOException {
        validatePipelineId(pipelineId);
        try {
            PipelineEntity pipeline = getPipelineFromId(pipelineId);
            Map<UUID, String> stageContents = fileService.readScriptFiles(pipeline);
            com.autobuild.pipeline.definiton.dto.Pipeline pipelineDTO = mapper.entityToDto(pipeline);
            // pipelineDTO.getStages()
            //         .forEach(stage -> stage.setCommand(stageContents.get(stage.getId())));
            com.autobuild.pipeline.definiton.dto.Pipeline pipelineWithCommands = pipelineDTO.toBuilder()
    .clearStages()
    .addAllStages(
        pipelineDTO.getStagesList().stream()
            .map(s -> s.toBuilder()
                .setCommand(stageContents.getOrDefault(UUID.fromString(s.getId()), ""))
                .build())
            .toList()
    )
    .build();
            log.info("Pipeline with id {} successfully obtained", pipelineDTO.getId());
            return pipelineWithCommands;
        } catch (IllegalArgumentException e) {
            throw new InvalidIdException("Pipeline Id is invalid");
        }
    }

    public List<com.autobuild.pipeline.definiton.dto.Pipeline> getAllPipelines() {
        List<PipelineEntity> pipelines = repository.findAll();
        log.info("Fetched all pipelines, count: {}", pipelines.size());
        return pipelines.stream()
                .map(pipeline -> {
                    com.autobuild.pipeline.definiton.dto.Pipeline dto = mapper.entityToDto(pipeline); // converting each fetched pipeline into PiplineDTO
                    return dto;
                })
                .toList();
    }

    public com.autobuild.pipeline.definiton.dto.Pipeline createPipeline(final com.autobuild.pipeline.definiton.dto.Pipeline pipelineDto)
            throws IOException, DuplicateEntryException, InvalidIdException {
        if (repository.existsByName(pipelineDto.getName())) {
            throw new DuplicateEntryException(
                    "Pipeline with name '" + pipelineDto.getName() + "' already exists");
        }

        for (com.autobuild.pipeline.definiton.dto.Stage stage : pipelineDto.getStagesList()) {
            validateScriptType(stage.getScriptType());
        }

        // pipelineDto.setId(UUID.randomUUID());
        // pipelineDto.getStages().forEach(stage -> stage.setId(UUID.randomUUID()));

        com.autobuild.pipeline.definiton.dto.Pipeline pipelineWithIds = pipelineDto.toBuilder()
                .setId(UUID.randomUUID().toString())
                .clearStages()
                .addAllStages(pipelineDto.getStagesList().stream()
                        .map(s -> s.toBuilder().setId(UUID.randomUUID().toString()).build()).toList())
                .build();

        PipelineEntity pipeline = mapper.dtoToEntity(pipelineWithIds);
        Map<UUID, String> scriptLocations = createScriptFiles(pipelineWithIds);
        pipeline.getStages()
                .forEach(stage -> stage.setPath(scriptLocations.get(stage.getId())));
        PipelineEntity createdPipeline = repository.save(pipeline);
        log.info("Pipeline with id {} successfully created", createdPipeline.getId());
        return mapper.entityToDto(createdPipeline);
    }

    public void deletePipelineById(String pipelineId)
            throws IOException, InvalidIdException {
        validatePipelineId(pipelineId);
        try {
            PipelineEntity pipeline = getPipelineFromId(pipelineId);
            deletePipeline(pipeline);
            log.info("Pipeline with id {} successfully deleted", pipelineId);
        } catch (IllegalArgumentException e) {
            throw new InvalidIdException("Pipeline Id is invalid");
        }
    }

    public com.autobuild.pipeline.definiton.dto.Pipeline modifyPipeline(String pipelineId, com.autobuild.pipeline.definiton.dto.Pipeline patchRequest)
            throws InvalidIdException, IOException, DuplicateEntryException {

        validatePipelineId(pipelineId);
        validateModifyRequest(patchRequest);

        PipelineEntity pipeline = getPipelineFromId(pipelineId);

        boolean nameChange = patchRequest.getName() != null
                && !patchRequest.getName().isBlank();
        boolean stagesChange = patchRequest.getStagesList() != null
                && !patchRequest.getStagesList().isEmpty();

        if (nameChange) {
            if (!patchRequest.getName().equals(pipeline.getName())
                    && repository.existsByName(patchRequest.getName())) {
                throw new DuplicateEntryException(
                        "Pipeline with name '" + patchRequest.getName() + "' already exists");
            }
            pipeline.setName(patchRequest.getName());
        }

        if (stagesChange) {
            processStageChanges(pipeline, patchRequest.getStagesList());
        }

        repository.save(pipeline);

        Map<UUID, String> finalScripts = fileService.readScriptFiles(pipeline);
        com.autobuild.pipeline.definiton.dto.Pipeline response = mapper.entityToDto(pipeline);
        // response.getStages().forEach(s -> s.setCommand(finalScripts.get(s.getId())));
        com.autobuild.pipeline.definiton.dto.Pipeline pipelineWithCommands = response.toBuilder()
    .clearStages()
    .addAllStages(
        response.getStagesList().stream()
            .map(s -> s.toBuilder()
                .setCommand(finalScripts.getOrDefault(UUID.fromString(s.getId()), ""))
                .build())
            .toList()
    )
    .build();
        log.info("Pipeline with id {} successfully modified", pipelineId);
        return pipelineWithCommands;
    }

    private void validatePipelineId(String pipelineId) throws InvalidIdException {
        if (pipelineId == null || pipelineId.isBlank()) {
            throw new InvalidIdException("Pipeline Id is empty");
        }
    }

    private void validateModifyRequest(com.autobuild.pipeline.definiton.dto.Pipeline request) throws InvalidIdException {
        boolean hasName = request.getName() != null && !request.getName().isBlank();
        boolean hasStages = request.getStagesList() != null && !request.getStagesList().isEmpty();

        if (!hasName && !hasStages) {
            throw new InvalidIdException("At least name or stages must be provided");
        }
    }

    private void validateScriptType(String scriptType) throws InvalidIdException {
        if (scriptType == null || scriptType.isBlank()) {
            throw new InvalidIdException("Script type is required");
        }
        try {
            Extensions.nameFor(scriptType);
        } catch (IllegalArgumentException e) {
            throw new InvalidIdException("Unsupported script type: " + scriptType);
        }
    }

    private void processStageChanges(PipelineEntity pipeline, List<com.autobuild.pipeline.definiton.dto.Stage> stageRequests)
            throws InvalidIdException, IOException, DuplicateEntryException {

        validateStageRequests(stageRequests);

        for (com.autobuild.pipeline.definiton.dto.Stage req : stageRequests) {
            if (isCreateOperation(req)) {
                createStage(pipeline, req);
            } else if (isDeleteOperation(req)) {
                deleteStage(pipeline, UUID.fromString(req.getId()));
            } else {
                updateStage(pipeline, req);
            }
        }
    }

    private boolean isCreateOperation(com.autobuild.pipeline.definiton.dto.Stage req) {
        return req.getId().isBlank();
    }

    private boolean isDeleteOperation(com.autobuild.pipeline.definiton.dto.Stage req) {
        return !req.getId().isBlank()
                && req.getName().isBlank()
                && req.getScriptType().isBlank()
                && req.getCommand().isBlank();
    }

    private void createStage(PipelineEntity pipeline, com.autobuild.pipeline.definiton.dto.Stage req)
            throws IOException, InvalidIdException {
        validateScriptType(req.getScriptType());
        com.autobuild.pipeline.definiton.dto.Stage reqWithId = req.toBuilder()
    .setId(UUID.randomUUID().toString())
    .build();
        StageEntity entity = stageMapper.dtoToEntity(reqWithId);
        String path = fileService.createStageScriptFile(pipeline, reqWithId);
        entity.setPath(path);
        pipeline.getStages().add(entity);
    }

    private void updateStage(PipelineEntity pipeline, com.autobuild.pipeline.definiton.dto.Stage req)
            throws InvalidIdException, IOException {

        StageEntity toUpdate = findStage(pipeline, UUID.fromString(req.getId()));

        if (!req.getName().isBlank()) {
            toUpdate.setName(req.getName());
        }

        if (!req.getScriptType().isBlank()) {
            throw new InvalidIdException("Script type updates are not allowed");
        }

        if (!req.getCommand().isBlank()) {
            fileService.updateStageScriptFile(toUpdate, req.getCommand());
        }
    }

    private void validateStageRequests(List<com.autobuild.pipeline.definiton.dto.Stage> requests)
            throws InvalidIdException, DuplicateEntryException {

        Set<UUID> ids = new HashSet<>();

        for (com.autobuild.pipeline.definiton.dto.Stage req : requests) {
            if (isCreateOperation(req)) {
                validateScriptType(req.getScriptType());
                continue;
            }

            if (isDeleteOperation(req)) {
                continue;
            }

            if (!req.getScriptType().isBlank()) {
                throw new InvalidIdException("Script type updates are not allowed");
            }

            if (!ids.add(UUID.fromString(req.getId()))) {
                throw new DuplicateEntryException("Duplicate stage ID in request: " + req.getId());
            }
        }
    }

    private void deleteStage(PipelineEntity pipeline, UUID stageId)
            throws InvalidIdException, IOException {
        StageEntity toDelete = findStage(pipeline, stageId);
        fileService.removeStageScriptFile(toDelete);
        pipeline.getStages().removeIf(s -> s.getId().equals(stageId));
    }

    private StageEntity findStage(PipelineEntity pipeline, UUID id) throws InvalidIdException {
        return pipeline.getStages().stream()
                .filter(s -> s.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new InvalidIdException("Stage not found: " + id));
    }

    private PipelineEntity getPipelineFromId(final String pipelineId) {
        Optional<PipelineEntity> optionalPipeline = repository.findById(UUID.fromString(pipelineId));
        if (!optionalPipeline.isPresent()) {
            throw new EntityNotFoundException(
                    "Pipeline with id " + pipelineId + " not found");
        }
        return optionalPipeline.get();
    }

    private Map<UUID, String> createScriptFiles(com.autobuild.pipeline.definiton.dto.Pipeline pipeline) throws IOException {
        try {
            return fileService.createScriptFiles(pipeline);
        } catch (IOException e) {
            fileService.removeScriptFiles(pipeline);
            throw e;
        }
    }

    public com.autobuild.pipeline.definiton.dto.Pipeline replacePipeline(String pipelineId, com.autobuild.pipeline.definiton.dto.Pipeline putRequest)
            throws InvalidIdException, IOException, DuplicateEntryException {

        validatePipelineId(pipelineId);
        PipelineEntity pipeline = getPipelineFromId(pipelineId);

        // Check for duplicate name
        if (!putRequest.getName().equals(pipeline.getName())
                && repository.existsByName(putRequest.getName())) {
            throw new DuplicateEntryException(
                    "Pipeline with name '" + putRequest.getName() + "' already exists");
        }

        // Replace name
        pipeline.setName(putRequest.getName());

        // Delete all existing stages and their files
        for (StageEntity existingStage : pipeline.getStages()) {
            fileService.removeStageScriptFile(existingStage);
        }
        pipeline.getStages().clear();

        // Create new stages from request
        for (com.autobuild.pipeline.definiton.dto.Stage stageDTO : putRequest.getStagesList()) {
            validateScriptType(stageDTO.getScriptType());
            com.autobuild.pipeline.definiton.dto.Stage stageWithId = stageDTO.toBuilder()
    .setId(UUID.randomUUID().toString())
    .build();

            String path = fileService.createStageScriptFile(pipeline, stageWithId);
            StageEntity newStage = stageMapper.dtoToEntity(stageWithId);
            newStage.setPath(path);
            pipeline.getStages().add(newStage);
        }

        repository.save(pipeline);

        Map<UUID, String> finalScripts = fileService.readScriptFiles(pipeline);
        com.autobuild.pipeline.definiton.dto.Pipeline response = mapper.entityToDto(pipeline);
        com.autobuild.pipeline.definiton.dto.Pipeline responseWithCommands = response.toBuilder()
    .clearStages()
    .addAllStages(
        response.getStagesList().stream()
            .map(s -> s.toBuilder()
                .setCommand(finalScripts.getOrDefault(UUID.fromString(s.getId()), ""))
                .build())
            .toList()
    )
    .build();
        log.info("Pipeline with id {} successfully replaced", pipelineId);
        return responseWithCommands;
    }

    private void deletePipeline(PipelineEntity pipeline) throws IOException {
        fileService.removeScriptFiles(mapper.entityToDto(pipeline));
        repository.delete(pipeline);
    }
}