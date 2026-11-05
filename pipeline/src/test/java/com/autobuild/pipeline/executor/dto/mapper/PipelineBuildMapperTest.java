package com.autobuild.pipeline.executor.dto.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.autobuild.pipeline.executor.dto.PipelineBuild;
import com.autobuild.pipeline.executor.dto.StageBuild;
import com.autobuild.pipeline.executor.entity.PipelineBuildEntity;
import com.autobuild.pipeline.executor.entity.StageBuildEntity;
import com.autobuild.pipeline.testutility.DummyData;

public class PipelineBuildMapperTest {

    @Mock
    private StageBuildMapper stageBuildMapper;

    @InjectMocks
    private PipelineBuildMapper pipelineBuildMapper;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void entityToDtoTest() {
        PipelineBuildEntity dummyBuild = DummyData.getPipelineBuild();
        StageBuild dummyStageBuild = DummyData.getStageBuildDTO();

        doReturn(dummyStageBuild).when(stageBuildMapper).entityToDto(any(StageBuildEntity.class));

        PipelineBuild result = pipelineBuildMapper.entityToDto(dummyBuild);

        assertEquals(dummyBuild.getId().toString(), result.getId());
        assertEquals(dummyBuild.getPipeline().getId().toString(), result.getPipelineId());
        assertEquals(dummyBuild.getPipeline().getName(), result.getPipelineName());
        verify(stageBuildMapper, times(dummyBuild.getStageBuilds().size())).entityToDto(any(StageBuildEntity.class));
    }

    @Test
    public void entityToDtoErrorTest() {
        assertThrows(IllegalArgumentException.class, () -> pipelineBuildMapper.entityToDto(null));
    }
}
