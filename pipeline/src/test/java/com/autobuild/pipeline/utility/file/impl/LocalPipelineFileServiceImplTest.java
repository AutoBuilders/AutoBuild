package com.autobuild.pipeline.utility.file.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.autobuild.pipeline.definiton.dto.PipelineDTO;
import com.autobuild.pipeline.definiton.dto.StageDTO;
import com.autobuild.pipeline.definiton.entity.Pipeline;
import com.autobuild.pipeline.definiton.entity.Stage;
import com.autobuild.pipeline.testutility.DummyData;
import com.autobuild.pipeline.utility.file.PipelineFileService;

public class LocalPipelineFileServiceImplTest {
    private Pipeline pipeline = DummyData.getPipeline();
    private PipelineDTO pipelineDTO = DummyData.getPipelineDTO();

    private Path pipelinePath = Path.of("..", "pipeline_scripts", pipeline.getId().toString());

    private MockedStatic<Files> filesMockedStatic;

    private PipelineFileService pipelineFileService = new LocalPipelineFileServiceImpl();

    @BeforeEach
    public void setUp() {
        filesMockedStatic = mockStatic(Files.class);

        filesMockedStatic.when(() -> Files.readAllLines(any(Path.class)))
                .thenReturn(List.of(pipelineDTO.getStages().get(0).getCommand()));
        filesMockedStatic.when(() -> Files.exists(any(Path.class))).thenReturn(false);
        filesMockedStatic.when(() -> Files.createDirectory(any(Path.class), any(FileAttribute.class)))
                .thenReturn(pipelinePath);
        filesMockedStatic.when(() -> Files.createDirectories(any(Path.class), any(FileAttribute.class)))
                .thenReturn(pipelinePath);
        filesMockedStatic.when(() -> Files.createFile(any(Path.class), any(FileAttribute.class)))
                .thenReturn(pipelinePath);
        filesMockedStatic.when(() -> Files.write(any(Path.class), any(byte[].class)))
                .thenAnswer(inv -> (Path) inv.getArgument(0));
        filesMockedStatic.when(() -> Files.deleteIfExists(any(Path.class)))
                .thenReturn(true);
        filesMockedStatic.when(() -> Files.delete(any(Path.class)))
                .thenAnswer(inv -> null);
        filesMockedStatic.when(() -> Files.walk(any(Path.class)))
                .thenReturn(Stream.of(pipelinePath));
    }

    @AfterEach
    public void tearDown() {
        filesMockedStatic.close();
    }

    @Test
    public void testReadScriptFiles() throws IOException {
        pipelineFileService.readScriptFiles(pipeline);
        filesMockedStatic.verify(() -> Files.readAllLines(any(Path.class)),
                times(pipeline.getStages().size()));
    }

    @Test
    public void testCreateScriptFiles() throws IOException {
        pipelineFileService.createScriptFiles(pipelineDTO);

        filesMockedStatic.verify(() -> Files.createFile(any(Path.class), any(FileAttribute.class)),
                times(pipeline.getStages().size()));
        filesMockedStatic.verify(() -> Files.write(any(Path.class), any(byte[].class)),
                times(pipeline.getStages().size()));
        filesMockedStatic.verify(() -> Files.createDirectories(any(Path.class), any(FileAttribute.class)),
                times(2));
    }

    @Test
    public void testRemoveScriptFiles() throws IOException {
        // override walk for full removal verification
        Stream<Path> paths = Stream.of(
                pipelinePath.resolve(pipeline.getStages().get(0).getId().toString()),
                pipelinePath);
        filesMockedStatic.when(() -> Files.walk(any(Path.class))).thenReturn(paths);

        pipelineFileService.removeScriptFiles(pipelineDTO);

        filesMockedStatic.verify(() -> Files.delete(any(Path.class)),
                times(pipeline.getStages().size() + 1));
    }

    @Test
    public void testRemoveScriptFilesWithEntity() throws IOException {
        pipelineFileService.removeScriptFiles(pipeline);
        filesMockedStatic.verify(() -> Files.deleteIfExists(any(Path.class)),
                times(pipeline.getStages().size() + 1));
    }

    @Test
    public void testCreateStageScriptFile() throws IOException {
        StageDTO newStage = pipelineDTO.getStages().get(0);
        String path = pipelineFileService.createStageScriptFile(pipeline, newStage);

        // Two exists checks: parent directory + pipeline directory
        filesMockedStatic.verify(() -> Files.exists(any(Path.class)), times(2));
        // Two createDirectories calls (parent + pipeline directory)
        filesMockedStatic.verify(() -> Files.createDirectories(any(Path.class), any(FileAttribute.class)), times(2));
        filesMockedStatic.verify(() -> Files.createFile(any(Path.class), any(FileAttribute.class)), times(1));
        filesMockedStatic.verify(() -> Files.write(any(Path.class), any(byte[].class)), times(1));
        assert path != null && !path.isBlank();
    }

    @Test
    public void testUpdateStageScriptFile() throws IOException {
        Stage stage = pipeline.getStages().get(0);
        stage.setPath(pipelinePath.resolve(stage.getId().toString()).toString());

        pipelineFileService.updateStageScriptFile(stage, "#!/bin/bash\necho UPDATED");

        filesMockedStatic.verify(() -> Files.write(any(Path.class), any(byte[].class)), times(1));
    }

    @Test
    public void testRemoveStageScriptFile() throws IOException {
        Stage stage = pipeline.getStages().get(0);
        stage.setPath(pipelinePath.resolve(stage.getId().toString()).toString());

        pipelineFileService.removeStageScriptFile(stage);

        filesMockedStatic.verify(() -> Files.deleteIfExists(any(Path.class)), times(1));
    }

    @Test
    public void testReadStageScriptFile() throws IOException {
        Stage stage = pipeline.getStages().get(0);
        stage.setPath(pipelinePath.resolve(stage.getId().toString()).toString());

        String content = pipelineFileService.readStageScriptFile(stage);

        filesMockedStatic.verify(() -> Files.readAllLines(any(Path.class)), times(1));
        assert content.equals(pipelineDTO.getStages().get(0).getCommand());
    }

    @Test
    public void testReadStageScriptFileBlankPath() throws IOException {
        Stage stage = new Stage();
        stage.setPath("");
        String content = pipelineFileService.readStageScriptFile(stage);
        assert content.isEmpty();
        filesMockedStatic.verify(() -> Files.readAllLines(any(Path.class)), times(0));
    }
}