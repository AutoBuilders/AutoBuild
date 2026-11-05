package com.autobuild.pipeline.definiton.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autobuild.pipeline.definiton.dto.PipelineDTO;
import com.autobuild.pipeline.definiton.dto.StageDTO;
import com.autobuild.pipeline.definiton.dto.mapper.PipelineMapper;
import com.autobuild.pipeline.definiton.dto.mapper.StageMapper;
import com.autobuild.pipeline.definiton.entity.Pipeline;
import com.autobuild.pipeline.definiton.entity.Stage;
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

    public PipelineDTO getPipelineById(final String pipelineId)
            throws InvalidIdException, IOException {
        validatePipelineId(pipelineId);
        try {
            Pipeline pipeline = getPipelineFromId(pipelineId);
            Map<UUID, String> stageContents = fileService.readScriptFiles(pipeline);
            PipelineDTO pipelineDTO = mapper.entityToDto(pipeline);
            pipelineDTO.getStages()
                    .forEach(stage -> stage.setCommand(stageContents.get(stage.getId())));
            log.info("Pipeline with id {} successfully obtained", pipelineDTO.getId());
            return pipelineDTO;
        } catch (IllegalArgumentException e) {
            throw new InvalidIdException("Pipeline Id is invalid");
        }
    }

    public List<PipelineDTO> getAllPipelines() {
        List<Pipeline> pipelines = repository.findAll();
        log.info("Fetched all pipelines, count: {}", pipelines.size());
        return pipelines.stream()
                .map(pipeline -> {
                    PipelineDTO dto = mapper.entityToDto(pipeline); // converting each fetched pipeline into PiplineDTO
                    return dto;
                })
                .toList();
    }

    public PipelineDTO createPipeline(final PipelineDTO pipelineDto)
            throws IOException, DuplicateEntryException, InvalidIdException {
        if (repository.existsByName(pipelineDto.getName())) {
            throw new DuplicateEntryException(
                    "Pipeline with name '" + pipelineDto.getName() + "' already exists");
        }

        for (StageDTO stage : pipelineDto.getStages()) {
            validateScriptType(stage.getScriptType());
        }

        pipelineDto.setId(UUID.randomUUID());
        List<StageDTO> stages = pipelineDto.getStages();
        for (int i = 0; i < stages.size(); i++) {
            stages.get(i).setId(UUID.randomUUID());
            stages.get(i).setStageOrder(i);
        }

        Pipeline pipeline = mapper.dtoToEntity(pipelineDto);
        Map<UUID, String> scriptLocations = createScriptFiles(pipelineDto);
        pipeline.getStages()
                .forEach(stage -> stage.setPath(scriptLocations.get(stage.getId())));
        Pipeline createdPipeline = repository.save(pipeline);
        log.info("Pipeline with id {} successfully created", createdPipeline.getId());
        return mapper.entityToDto(createdPipeline);
    }

    public void deletePipelineById(String pipelineId)
            throws IOException, InvalidIdException {
        validatePipelineId(pipelineId);
        try {
            Pipeline pipeline = getPipelineFromId(pipelineId);
            deletePipeline(pipeline);
            log.info("Pipeline with id {} successfully deleted", pipelineId);
        } catch (IllegalArgumentException e) {
            throw new InvalidIdException("Pipeline Id is invalid");
        }
    }

    public PipelineDTO modifyPipeline(String pipelineId, PipelineDTO patchRequest)
            throws InvalidIdException, IOException, DuplicateEntryException {

        validatePipelineId(pipelineId);
        validateModifyRequest(patchRequest);

        Pipeline pipeline = getPipelineFromId(pipelineId);

        boolean nameChange = patchRequest.getName() != null
                && !patchRequest.getName().isBlank();
        boolean stagesChange = patchRequest.getStages() != null
                && !patchRequest.getStages().isEmpty();

        if (nameChange) {
            if (!patchRequest.getName().equals(pipeline.getName())
                    && repository.existsByName(patchRequest.getName())) {
                throw new DuplicateEntryException(
                        "Pipeline with name '" + patchRequest.getName() + "' already exists");
            }
            pipeline.setName(patchRequest.getName());
        }

        if (stagesChange) {
            processStageChanges(pipeline, patchRequest.getStages());
        }

        repository.save(pipeline);

        Map<UUID, String> finalScripts = fileService.readScriptFiles(pipeline);
        PipelineDTO response = mapper.entityToDto(pipeline);
        response.getStages().forEach(s -> s.setCommand(finalScripts.get(s.getId())));
        log.info("Pipeline with id {} successfully modified", pipelineId);
        return response;
    }

    private void validatePipelineId(String pipelineId) throws InvalidIdException {
        if (pipelineId == null || pipelineId.isBlank()) {
            throw new InvalidIdException("Pipeline Id is empty");
        }
    }

    private void validateModifyRequest(PipelineDTO request) throws InvalidIdException {
        boolean hasName = request.getName() != null && !request.getName().isBlank();
        boolean hasStages = request.getStages() != null && !request.getStages().isEmpty();

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

    private void processStageChanges(Pipeline pipeline, List<StageDTO> stageRequests)
            throws InvalidIdException, IOException, DuplicateEntryException {

        validateStageRequests(stageRequests);

        for (StageDTO req : stageRequests) {
            if (isCreateOperation(req)) {
                createStage(pipeline, req);
            } else if (isDeleteOperation(req)) {
                deleteStage(pipeline, req.getId());
            } else {
                updateStage(pipeline, req);
            }
        }

        reorderStages(pipeline, stageRequests);
    }

    private void reorderStages(Pipeline pipeline, List<StageDTO> stageRequests) {
        List<Stage> current = pipeline.getStages();

        List<UUID> requestedIds = stageRequests.stream()
                .filter(req -> req.getId() != null && !isDeleteOperation(req))
                .map(StageDTO::getId)
                .toList();

        List<Stage> reordered = new ArrayList<>();
        for (UUID id : requestedIds) {
            current.stream()
                    .filter(s -> s.getId().equals(id))
                    .findFirst()
                    .ifPresent(reordered::add);
        }
        for (Stage s : current) {
            if (!reordered.contains(s)) {
                reordered.add(s);
            }
        }

        for (int i = 0; i < reordered.size(); i++) {
            reordered.get(i).setStageOrder(i);
        }

        current.clear();
        current.addAll(reordered);
    }

    private boolean isCreateOperation(StageDTO req) {
        return req.getId() == null;
    }

    private boolean isDeleteOperation(StageDTO req) {
        return req.getId() != null
                && req.getName() == null
                && req.getScriptType() == null
                && req.getCommand() == null;
    }

    private void createStage(Pipeline pipeline, StageDTO req)
            throws IOException, InvalidIdException {
        validateScriptType(req.getScriptType());
        req.setId(UUID.randomUUID());
        req.setStageOrder(pipeline.getStages().size());
        Stage entity = stageMapper.dtoToEntity(req);
        String path = fileService.createStageScriptFile(pipeline, req);
        entity.setPath(path);
        pipeline.getStages().add(entity);
    }

    private void updateStage(Pipeline pipeline, StageDTO req)
            throws InvalidIdException, IOException {

        Stage toUpdate = findStage(pipeline, req.getId());

        if (req.getName() != null && !req.getName().isBlank()) {
            toUpdate.setName(req.getName());
        }

        if (req.getScriptType() != null) {
            throw new InvalidIdException("Script type updates are not allowed");
        }

        if (req.getCommand() != null && !req.getCommand().isBlank()) {
            fileService.updateStageScriptFile(toUpdate, req.getCommand());
        }
    }

    private void validateStageRequests(List<StageDTO> requests)
            throws InvalidIdException, DuplicateEntryException {

        Set<UUID> ids = new HashSet<>();

        for (StageDTO req : requests) {
            if (isCreateOperation(req)) {
                validateScriptType(req.getScriptType());
                continue;
            }

            if (isDeleteOperation(req)) {
                continue;
            }

            if (req.getScriptType() != null) {
                throw new InvalidIdException("Script type updates are not allowed");
            }

            if (!ids.add(req.getId())) {
                throw new DuplicateEntryException("Duplicate stage ID in request: " + req.getId());
            }
        }
    }

    private void deleteStage(Pipeline pipeline, UUID stageId)
            throws InvalidIdException, IOException {
        Stage toDelete = findStage(pipeline, stageId);
        fileService.removeStageScriptFile(toDelete);
        pipeline.getStages().removeIf(s -> s.getId().equals(stageId));
    }

    private Stage findStage(Pipeline pipeline, UUID id) throws InvalidIdException {
        return pipeline.getStages().stream()
                .filter(s -> s.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new InvalidIdException("Stage not found: " + id));
    }

    private Pipeline getPipelineFromId(final String pipelineId) {
        Optional<Pipeline> optionalPipeline = repository.findById(UUID.fromString(pipelineId));
        if (!optionalPipeline.isPresent()) {
            throw new EntityNotFoundException(
                    "Pipeline with id " + pipelineId + " not found");
        }
        return optionalPipeline.get();
    }

    private Map<UUID, String> createScriptFiles(PipelineDTO pipeline) throws IOException {
        try {
            return fileService.createScriptFiles(pipeline);
        } catch (IOException e) {
            fileService.removeScriptFiles(pipeline);
            throw e;
        }
    }

    public PipelineDTO replacePipeline(String pipelineId, PipelineDTO putRequest)
            throws InvalidIdException, IOException, DuplicateEntryException {

        validatePipelineId(pipelineId);
        Pipeline pipeline = getPipelineFromId(pipelineId);

        // Check for duplicate name
        if (!putRequest.getName().equals(pipeline.getName())
                && repository.existsByName(putRequest.getName())) {
            throw new DuplicateEntryException(
                    "Pipeline with name '" + putRequest.getName() + "' already exists");
        }

        // Replace name
        pipeline.setName(putRequest.getName());

        // Delete all existing stages and their files
        for (Stage existingStage : pipeline.getStages()) {
            fileService.removeStageScriptFile(existingStage);
        }
        pipeline.getStages().clear();

        // Create new stages from request
        for (StageDTO stageDTO : putRequest.getStages()) {
            validateScriptType(stageDTO.getScriptType());
            stageDTO.setId(UUID.randomUUID());
            String path = fileService.createStageScriptFile(pipeline, stageDTO);
            Stage newStage = stageMapper.dtoToEntity(stageDTO);
            newStage.setPath(path);
            pipeline.getStages().add(newStage);
        }

        repository.save(pipeline);

        Map<UUID, String> finalScripts = fileService.readScriptFiles(pipeline);
        PipelineDTO response = mapper.entityToDto(pipeline);
        response.getStages().forEach(s -> s.setCommand(finalScripts.get(s.getId())));
        log.info("Pipeline with id {} successfully replaced", pipelineId);
        return response;
    }

    private void deletePipeline(Pipeline pipeline) throws IOException {
        fileService.removeScriptFiles(mapper.entityToDto(pipeline));
        repository.delete(pipeline);
    }
}