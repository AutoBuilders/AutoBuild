package com.autobuild.pipeline.definition.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.autobuild.pipeline.definiton.gen.dto.Pipeline;
import com.autobuild.pipeline.definiton.dto.mapper.PipelineMapper;
import com.autobuild.pipeline.definiton.dto.mapper.StageMapper;
import com.autobuild.pipeline.definiton.entity.PipelineEntity;
import com.autobuild.pipeline.testutility.DummyData;

@SpringJUnitConfig
@ContextConfiguration(classes = {PipelineMapper.class, StageMapper.class})
public class PipelineMapperTest {

    private PipelineEntity pipeline = DummyData.getPipeline();
    private Pipeline pipelineDTO = DummyData.getPipelineDTO();

    @Autowired
    private PipelineMapper pipelineMapper;

    @Test
    public void whenGivenPipelineEntityGetPipelineDTO() {
        Pipeline convertedDto = pipelineMapper.entityToDto(pipeline);
        assertDto(convertedDto);
    }

    @Test
    public void whenGivenPipelineDTOGetPipelineEntity() {
        PipelineEntity converted = pipelineMapper.dtoToEntity(pipelineDTO);
        assertEntity(converted);
    }

    @Test
    public void whenGivenNullPipelineGetPipelineDTO() {
        assertThrows(IllegalArgumentException.class, () -> pipelineMapper.entityToDto(null));
    }

    @Test
    public void whenGivenNullDTPGetPipeline() {
        assertThrows(IllegalArgumentException.class, () -> pipelineMapper.dtoToEntity(null));
    }

    @Test
    public void multipleMapInvocationTest() {
        PipelineEntity p1 = pipelineMapper.dtoToEntity(pipelineDTO);
        assertEntity(p1);

        Pipeline pipelineDTO2 = pipelineDTO.toBuilder().setId(UUID.randomUUID().toString()).build();
        PipelineEntity p2 = pipelineMapper.dtoToEntity(pipelineDTO2);
        assertEquals(pipelineDTO2.getId(), p2.getId().toString());
        assertEquals(pipelineDTO2.getName(), p2.getName());
    }

    private void assertDto(Pipeline convertedDto) {
        assertEquals(pipeline.getId().toString(), convertedDto.getId());
        assertEquals(pipeline.getName(), convertedDto.getName());
        assertEquals(pipeline.getStages().size(), convertedDto.getStagesList().size());
        assertEquals(pipeline.getStages().get(0).getName(), convertedDto.getStagesList().get(0).getName());
        assertEquals(pipeline.getStages().get(0).getId().toString(), convertedDto.getStagesList().get(0).getId());
    }

    private void assertEntity(PipelineEntity converted) {
        assertEquals(pipelineDTO.getName(), converted.getName());
        assertEquals(pipelineDTO.getId(), converted.getId().toString());
        assertEquals(pipelineDTO.getStagesList().size(), converted.getStages().size());
        assertEquals(pipelineDTO.getStagesList().get(0).getName(), converted.getStages().get(0).getName());
        assertEquals(pipelineDTO.getStagesList().get(0).getId(), converted.getStages().get(0).getId().toString());
    }
}
