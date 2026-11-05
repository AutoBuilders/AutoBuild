package com.autobuild.pipeline.definition.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.autobuild.pipeline.definiton.dto.Stage;
import com.autobuild.pipeline.definiton.dto.mapper.StageMapper;
import com.autobuild.pipeline.definiton.entity.StageEntity;
import com.autobuild.pipeline.testutility.DummyData;

@SpringJUnitConfig
@ContextConfiguration(classes = { StageMapper.class })
public class StageMapperTest {

    private StageEntity stage = DummyData.getStage();
    private Stage dto = DummyData.getStageDTO();

    @Autowired
    private StageMapper stageMapper;

    @Test
    public void givenStageEntityGetStageDTO() {
        Stage convertedStageDTO = stageMapper.entityToDto(stage);
        assertDto(convertedStageDTO);
    }

    @Test
    public void givenNullEntityGetStageDTO() {
        assertThrows(IllegalArgumentException.class, () -> stageMapper.entityToDto(null));
    }

    @Test
    public void givenStageDTOGetStageEntity() {
        StageEntity convertedStage = stageMapper.dtoToEntity(dto);
        assertEntity(convertedStage);
    }

    @Test
    public void givenNullDTOGetStageEntity() {
        assertThrows(IllegalArgumentException.class, () -> stageMapper.dtoToEntity(null));
    }

    @Test
    public void multipleMapInvocationTest() {
        StageEntity s1 = stageMapper.dtoToEntity(dto);
        assertEntity(s1);

        StageEntity s2 = stageMapper.dtoToEntity(dto);
        assertEntity(s2);
    }

    private void assertEntity(StageEntity convertedStageEntity) {
        assertEquals(dto.getId(), convertedStageEntity.getId().toString());
        assertEquals(dto.getName(), convertedStageEntity.getName());
        assertEquals(dto.getScriptType(), convertedStageEntity.getScriptType());
    }

    private void assertDto(Stage convertedStageDTO) {
        assertEquals(stage.getId().toString(), convertedStageDTO.getId());
        assertEquals(stage.getName(), convertedStageDTO.getName());
        assertEquals(stage.getScriptType(), convertedStageDTO.getScriptType());
    }
}
