package com.autobuild.pipeline.executor.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.autobuild.pipeline.executor.service.PipelineBuildLiveService;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for Live Pipeline Build.
 * 
 * @author Suvabrata Chowdhury
 */

@Slf4j
@Hidden // For now hiding SSE from OpenAPI spec
@RestController
@RequestMapping("/api/v1/pipeline/build")
public class PipelineBuildLiveController {

    @Autowired
    private PipelineBuildLiveService service;

    @GetMapping(value = "/sse/subscribe/{pipelineBuildId}", produces = "text/event-stream")
    public SseEmitter getLivePipelineBuild(@PathVariable UUID pipelineBuildId) {
        log.info("Subscription requested");
        SseEmitter emitter = new SseEmitter(600000L);

        service.addSubscriber(emitter, pipelineBuildId);

        log.info("Subscription done. returning emitter");

        return emitter;
    }
}
