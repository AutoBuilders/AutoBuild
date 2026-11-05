package com.autobuild.pipeline.executor.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Pipeline execute request DTO.
 * 
 * @author Suvabrata Chowdhury
 */

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PipelineExecuteRequest {
    @NotNull
    private UUID pipelineId;
}
