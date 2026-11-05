package com.autobuild.pipeline.executor.controller;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autobuild.pipeline.executor.dto.PipelineExecuteRequest;
import com.autobuild.pipeline.executor.service.PipelineExecutorService;

import com.autobuild.pipeline.executor.dto.PipelineBuild;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controller for executing a pipeline.
 *
 * @author Suvabrata Chowdhury
 */
@Tag(name = "Pipeline")
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT", description = "JWT authentication. Enter token as: Bearer <token>")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/execute")
public class PipelineExecutorController {

    @Autowired
    private PipelineExecutorService service;

    @Operation(summary = "Execute a pipeline", responses = @ApiResponse(responseCode = "203"))
    @PostMapping(value = "/pipeline", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PipelineBuild> executePipeline(@RequestBody @Valid PipelineExecuteRequest request)
            throws IOException {
        PipelineBuild build = service.executePipeline(request);

        return ResponseEntity.accepted().body(build);
    }
}