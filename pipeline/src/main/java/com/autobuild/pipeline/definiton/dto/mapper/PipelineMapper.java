package com.autobuild.pipeline.definiton.dto.mapper;

import org.springframework.stereotype.Component;

import com.autobuild.pipeline.definiton.entity.PipelineEntity;
import com.autobuild.pipeline.definiton.entity.StageEntity;
import com.autobuild.pipeline.definiton.dto.Pipeline;
import com.autobuild.pipeline.definiton.dto.Stage;

import java.util.List;
import java.util.UUID;

/**
 * Used to map Pipeline Entity to Pipeline DTO.
 *
 * @author Suvabrata Chowdhury
 */

@Component
public class PipelineMapper implements Mapper<Pipeline, PipelineEntity> {

    // @Autowired
    // private StageMapper stageMapper;

    @Override
    public PipelineEntity dtoToEntity(Pipeline pipelineProto) {
        if (null == pipelineProto) {
            throw new IllegalArgumentException("Given null object can not be mapped");
        }

        List<StageEntity> stages = pipelineProto.getStagesList().stream().map(stageProto -> {
            StageEntity s = new StageEntity();
            if (!stageProto.getId().isBlank()) {
                s.setId(UUID.fromString(stageProto.getId()));
            }
            s.setName(stageProto.getName());
            s.setScriptType(stageProto.getScriptType());
            return s;
        }).toList();

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

        List<Stage> stagesProto = pipeline.getStages().stream().map(stage -> Stage.newBuilder()
                .setId(stage.getId().toString()).setName(stage.getName()).setScriptType(stage.getScriptType()).build())
                .toList();

        Pipeline pipelineProto = Pipeline.newBuilder().setId(pipeline.getId().toString()).setName(pipeline.getName())
                .addAllStages(stagesProto).build();

        return pipelineProto;
    }
}
