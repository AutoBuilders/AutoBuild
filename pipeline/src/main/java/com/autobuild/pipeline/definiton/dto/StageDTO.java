package com.autobuild.pipeline.definiton.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object for Stage.
 * @author Suvabrata Chowdhury
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(name = "Stage")
public class StageDTO {
    private UUID id;

    @NotEmpty
    private String name;

    @NotEmpty
    private String scriptType;

    @NotEmpty
    private String command;

    private int stageOrder;

    public StageDTO(UUID id, String name, String scriptType, String command) {
        this(id, name, scriptType, command, 0);
    }
}
