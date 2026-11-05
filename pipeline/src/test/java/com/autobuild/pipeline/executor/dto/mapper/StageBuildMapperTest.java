package com.autobuild.pipeline.executor.dto.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import com.autobuild.pipeline.executor.dto.StageBuild;
import com.autobuild.pipeline.executor.entity.StageBuildEntity;
import com.autobuild.pipeline.testutility.DummyData;

public class StageBuildMapperTest {

    @InjectMocks
    private StageBuildMapper stageBuildMapper;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void entityToDtoTest() {
        StageBuildEntity dummyBuild = DummyData.getStageBuild();

        StageBuild result = stageBuildMapper.entityToDto(dummyBuild);

        assertEquals(dummyBuild.getId().toString(), result.getId());
        assertEquals(dummyBuild.getStage().getId().toString(), result.getStageId());
        assertEquals(dummyBuild.getStage().getName(), result.getStageName());
    }

    @Test
    public void entityToDtoErrorTest() {
        assertThrows(IllegalArgumentException.class, () -> stageBuildMapper.entityToDto(null));
    }
}
