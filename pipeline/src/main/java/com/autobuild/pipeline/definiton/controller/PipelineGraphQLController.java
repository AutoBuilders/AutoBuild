package com.autobuild.pipeline.definiton.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.autobuild.pipeline.definiton.dto.PipelineDTO;
import com.autobuild.pipeline.definiton.exceptions.InvalidIdException;
import com.autobuild.pipeline.definiton.service.PipelineService;

/**
 * GraphQL controller for read-only pipeline queries.
 *
 * @author Suvabrata Chowdhury
 */
@Controller
public class PipelineGraphQLController {

    @Autowired
    private PipelineService pipelineService;

    /**
     * Fetch a single pipeline by its ID.
     *
     * @param pipelineId the pipeline identifier
     * @return the matching pipeline
     * @throws InvalidIdException if no pipeline exists for the given ID
     * @throws IOException if the pipeline script files cannot be read
     */
    @QueryMapping
    public PipelineDTO getPipelineById(@Argument String pipelineId)
            throws InvalidIdException, IOException {
        return pipelineService.getPipelineById(pipelineId);
    }

    /**
     * Fetch all pipelines.
     *
     * @return list of all pipelines
     */
    @QueryMapping
    public List<PipelineDTO> getAllPipelines() {
        return pipelineService.getAllPipelines();
    }
}
