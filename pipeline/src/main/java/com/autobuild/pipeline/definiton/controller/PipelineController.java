package com.autobuild.pipeline.definiton.controller;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autobuild.pipeline.configuration.FeatureFlag;
import com.autobuild.pipeline.configuration.FeatureFlagService;
import com.autobuild.pipeline.definiton.dto.PipelineDTO;
import com.autobuild.pipeline.definiton.exceptions.DuplicateEntryException;
import com.autobuild.pipeline.definiton.exceptions.InvalidIdException;
import com.autobuild.pipeline.definiton.service.PipelineService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controller for all CRUD operations on Pipeline.
 * 
 * @author Suvabrata Chowdhury
 */
@Tag(name = "Pipeline", description = "Operations related to pipeline")
@SecurityRequirement(name = "bearerAuth")
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT authentication. Enter token as: Bearer <token>"
)
@RestController
@RequestMapping("/api/v1/pipeline")
public class PipelineController {

    @Autowired
    private PipelineService pipelineService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Operation(summary = "Get a pipeline")
    @GetMapping(value = "/{pipelineId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PipelineDTO> getPipelineById(@PathVariable String pipelineId)
            throws InvalidIdException, IOException {
        return ResponseEntity.ok(pipelineService.getPipelineById(pipelineId));
    }

    @Operation(summary = "Get all pipelines")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PipelineDTO>> getAllPipelines() {
        return ResponseEntity.ok(pipelineService.getAllPipelines());
    }

    @Operation(
        summary = "Create a pipeline",
        responses = @ApiResponse(responseCode = "201")
    )
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PipelineDTO> createPipeline(@RequestBody @Valid PipelineDTO pipelineRequest)
            throws DuplicateEntryException, IOException, InvalidIdException {
        PipelineDTO createdPipeline = pipelineService.createPipeline(pipelineRequest);
        URI location = URI.create("/api/v1/pipeline/" + createdPipeline.getId());
        return ResponseEntity.created(location).body(createdPipeline);
    }

    @Operation(
        summary = "[Experimental]: Update a pipeline",
        description = "API to update pipeline. It's currently under development. Do not use it!",
        deprecated = true
    )
    @PatchMapping(value = "/{pipelineId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PipelineDTO> modifyPipeline(
            @PathVariable String pipelineId,
            @RequestBody PipelineDTO patchRequest)
            throws InvalidIdException, IOException, DuplicateEntryException {
        if (!featureFlagService.getBooleanValue(FeatureFlag.ENABLE_EDIT_PIPELINE.getKey())) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
        }
        return ResponseEntity.ok(pipelineService.modifyPipeline(pipelineId, patchRequest));
    }

    @Operation(
        summary = "Delete a pipeline",
        responses = @ApiResponse(responseCode = "204")
    )
    @DeleteMapping(value = "/{pipelineId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deletePipeline(@PathVariable String pipelineId)
            throws IOException, InvalidIdException {
        pipelineService.deletePipelineById(pipelineId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "[Experimental]: Update a pipeline",
        description = "API to update pipeline. It's currently under development. Do not use it!",
        deprecated = true
    )
    @PutMapping(value = "/{pipelineId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PipelineDTO> updatePipeline(
            @PathVariable String pipelineId,
            @RequestBody @Valid PipelineDTO putRequest)
            throws InvalidIdException, IOException, DuplicateEntryException {
        if (!featureFlagService.getBooleanValue(FeatureFlag.ENABLE_EDIT_PIPELINE.getKey())) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
        }
        return ResponseEntity.ok(pipelineService.replacePipeline(pipelineId, putRequest));
    }
}