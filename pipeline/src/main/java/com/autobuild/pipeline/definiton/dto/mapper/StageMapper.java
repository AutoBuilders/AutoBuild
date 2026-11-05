package com.autobuild.pipeline.definiton.dto.mapper;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.autobuild.pipeline.definiton.entity.StageEntity;
import com.autobuild.pipeline.definiton.dto.Stage;

/**
 * Used to map Stage Entity to Stage DTO.
 * @author Suvabrata Chowdhury
 */

@Component
public class StageMapper implements Mapper<
        Stage,
        StageEntity> {

    @Override
    public StageEntity dtoToEntity(Stage dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Given null object can not be mapped");
        }

        StageEntity entity = new StageEntity();
        if (!dto.getId().isBlank()) {
            entity.setId(UUID.fromString(dto.getId()));
        }
        entity.setName(dto.getName());
        entity.setScriptType(dto.getScriptType());
        return entity;
    }

    @Override
    public Stage entityToDto(StageEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Given null object can not be mapped");
        }

        return Stage.newBuilder()
            .setId(entity.getId().toString())
            .setName(entity.getName())
            .setScriptType(entity.getScriptType())
            .build();
    }
}
