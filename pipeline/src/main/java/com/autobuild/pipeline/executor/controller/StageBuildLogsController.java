package com.autobuild.pipeline.executor.controller;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autobuild.pipeline.executor.service.StageBuildService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * Controllers for stage build logs.
 * 
 * @author Suvabrata Chowdhury
 */

@Slf4j
@Tag(name = "Stage Build")
@RestController
@RequestMapping("/api/v1/stage/build/logs")
public class StageBuildLogsController {

    @Autowired
    private StageBuildService service;

    // @GetMapping("/sse/subscribe/{stageBuildId}")
    // public SseEmitter getLiveStageBuildLogs(@PathVariable UUID stageBuildId) {
    // SseEmitter emitter = new SseEmitter(600000L);

    // service.getStageBuildLiveLogs(emitter,stageBuildId);

    // return emitter;
    // }

    @Operation(summary = "Get the logs related to a stage build")
    @GetMapping(value = "/{stageBuildId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> getStageBuildLogs(@PathVariable UUID stageBuildId) throws IOException {
        return ResponseEntity.ok(service.getStageBuildLogs(stageBuildId));
    }
}
