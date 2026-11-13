package com.autobuild.pipeline.definiton.dto.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.autobuild.pipeline.definiton.entity.PipelineEntity;
import com.autobuild.pipeline.definiton.entity.StageEntity;
import com.autobuild.pipeline.definiton.gen.dto.Pipeline;
import com.autobuild.pipeline.definiton.gen.dto.Stage;

import java.util.List;
import java.util.UUID;

/**
 * Used to map Pipeline Entity to Pipeline DTO.
 * @author Suvabrata Chowdhury
 */

@Component
public class PipelineMapper implements Mapper<Pipeline, PipelineEntity>{

    @Autowired
    private StageMapper stageMapper;

    @Override
    public PipelineEntity dtoToEntity(Pipeline pipelineProto) {
        if (null == pipelineProto) {
            throw new IllegalArgumentException("Given null object can not be mapped");
        }

        List<StageEntity> stages = pipelineProto.getStagesList()
            .stream()
            .map(stageMapper::dtoToEntity)
            .toList();

        PipelineEntity pipeline = new PipelineEntity();
        if (!pipelineProto.getId().isBlank()) {
            pipeline.setId(UUID.fromString(pipelineProto.getId()));
        }
        pipeline.setName(pipelineProto.getName());
        pipeline.setStages(stages);
        return pipeline;
    }

    @Override
    public Pipeline entityToDto(PipelineEntity pipeline) {
        if (null == pipeline) {
            throw new IllegalArgumentException("Given null object can not be mapped");
        }

        List<Stage> stagesProto = pipeline.getStages()
            .stream()
            .map(stageMapper::entityToDto)
            .toList();

        return Pipeline.newBuilder()
            .setId(pipeline.getId().toString())
            .setName(pipeline.getName())
            .addAllStages(stagesProto)
            .build();
    }
}
