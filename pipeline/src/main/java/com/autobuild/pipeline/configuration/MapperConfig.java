package com.autobuild.pipeline.configuration;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.autobuild.pipeline.definiton.dto.PipelineDTO;
import com.autobuild.pipeline.definiton.dto.StageDTO;
import com.autobuild.pipeline.definiton.entity.Pipeline;
import com.autobuild.pipeline.definiton.entity.Stage;
import com.autobuild.pipeline.executor.dto.PipelineBuildDTO;
import com.autobuild.pipeline.executor.dto.StageBuildDTO;
import com.autobuild.pipeline.executor.entity.PipelineBuild;
import com.autobuild.pipeline.executor.entity.StageBuild;

/**
 * Bean Configurations for the application.
 * 
 * @author Suvabrata Chowdhury
 */

@Configuration
public class MapperConfig {
    @Bean
    public ModelMapper getModelMapper() {
        ModelMapper mapper = new ModelMapper();

        setStageMappings(mapper);
        setPipelineMapping(mapper);

        setStageBuildMappings(mapper);
        setPipelineBuildMappings(mapper);

        return mapper;
    }

    private void setStageMappings(ModelMapper mapper) {
        setStageDTOEntityMapping(mapper);
        setStageEntityDTOMapping(mapper);
    }

    private void setStageDTOEntityMapping(ModelMapper mapper) {
        TypeMap<StageDTO, Stage> typeMapper = mapper.createTypeMap(StageDTO.class, Stage.class);
        typeMapper.addMapping(StageDTO::getId, Stage::setId);
        typeMapper.addMapping(StageDTO::getName, Stage::setName);
        typeMapper.addMapping(StageDTO::getScriptType, Stage::setScriptType);
    }

    private void setStageEntityDTOMapping(ModelMapper mapper) {
        TypeMap<Stage, StageDTO> typeMapper = mapper.createTypeMap(Stage.class, StageDTO.class);
        typeMapper.addMapping(Stage::getId, StageDTO::setId);
        typeMapper.addMapping(Stage::getName, StageDTO::setName);
        typeMapper.addMapping(Stage::getScriptType, StageDTO::setScriptType);
    }

    private void setPipelineMapping(ModelMapper mapper) {
        setPipelineDTOEntityMapping(mapper);
        setPipelineEntityDTOMapping(mapper);
    }

    private void setPipelineDTOEntityMapping(ModelMapper mapper) {
        TypeMap<PipelineDTO, Pipeline> typeMapper = mapper.createTypeMap(PipelineDTO.class, Pipeline.class);
        typeMapper.addMapping(PipelineDTO::getId, Pipeline::setId);
        typeMapper.addMapping(PipelineDTO::getName, Pipeline::setName);
    }

    private void setPipelineEntityDTOMapping(ModelMapper mapper) {
        TypeMap<Pipeline, PipelineDTO> typeMapper = mapper.createTypeMap(Pipeline.class, PipelineDTO.class);
        typeMapper.addMapping(Pipeline::getName, PipelineDTO::setName);
        typeMapper.addMapping(Pipeline::getId, PipelineDTO::setId);
    }

    private void setStageBuildMappings(ModelMapper mapper) {
        setStageBuildEntityDTOMapping(mapper);
        setStageBuildDTOEntityMapping(mapper);
    }

    private void setStageBuildEntityDTOMapping(ModelMapper mapper) {
        TypeMap<StageBuild, StageBuildDTO> typeMapper = mapper.createTypeMap(StageBuild.class, StageBuildDTO.class);
        typeMapper.addMapping(StageBuild::getId, StageBuildDTO::setId);
        typeMapper.addMapping(stageBuild -> stageBuild.getStage().getId(), StageBuildDTO::setStageId);
        typeMapper.addMapping(stageBuild -> stageBuild.getStage().getName(), StageBuildDTO::setStageName);
        typeMapper.addMapping(stageBuild -> stageBuild.getStage().getStageOrder(), StageBuildDTO::setStageOrder);
        typeMapper.addMapping(StageBuild::getCurrentState, StageBuildDTO::setCurrentState);
    }

    private void setStageBuildDTOEntityMapping(ModelMapper mapper) {
        TypeMap<StageBuildDTO, StageBuild> typeMapper = mapper.createTypeMap(StageBuildDTO.class, StageBuild.class);
        typeMapper.addMapping(StageBuildDTO::getId, StageBuild::setId);
        typeMapper.addMapping(StageBuildDTO::getCurrentState, StageBuild::setCurrentState);
    }

    private void setPipelineBuildMappings(ModelMapper mapper) {
        setPipelineBuildEntityDTOMapping(mapper);
        setPipelineBuildDTOEntityMapping(mapper);
    }

    private void setPipelineBuildEntityDTOMapping(ModelMapper mapper) {
        TypeMap<PipelineBuild, PipelineBuildDTO> typeMapper = mapper.createTypeMap(PipelineBuild.class,
                PipelineBuildDTO.class);
        typeMapper.addMapping(PipelineBuild::getId, PipelineBuildDTO::setId);
        typeMapper.addMapping(pipelineBuild -> pipelineBuild.getPipeline().getId(), PipelineBuildDTO::setPipelineId);
        typeMapper.addMapping(pipelineBuild -> pipelineBuild.getPipeline().getName(),
                PipelineBuildDTO::setPipelineName);
        typeMapper.addMapping(PipelineBuild::getCurrentState, PipelineBuildDTO::setCurrentState);
        typeMapper.addMapping(PipelineBuild::getStartedAt, PipelineBuildDTO::setStartedAt);
    }

    private void setPipelineBuildDTOEntityMapping(ModelMapper mapper) {
        TypeMap<PipelineBuildDTO, PipelineBuild> typeMapper = mapper.createTypeMap(PipelineBuildDTO.class,
                PipelineBuild.class);
        typeMapper.addMapping(PipelineBuildDTO::getId, PipelineBuild::setId);
        typeMapper.addMapping(PipelineBuildDTO::getCurrentState, PipelineBuild::setCurrentState);
    }
}
