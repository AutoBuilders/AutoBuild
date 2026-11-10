package com.autobuild.pipeline.definition.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import com.autobuild.pipeline.definiton.gen.dto.Pipeline;
import com.autobuild.pipeline.definiton.gen.dto.Stage;
import com.autobuild.pipeline.definiton.dto.mapper.PipelineMapper;
import com.autobuild.pipeline.definiton.dto.mapper.StageMapper;
import com.autobuild.pipeline.definiton.entity.PipelineEntity;
import com.autobuild.pipeline.definiton.entity.StageEntity;
import com.autobuild.pipeline.definiton.exceptions.DuplicateEntryException;
import com.autobuild.pipeline.definiton.exceptions.InvalidIdException;
import com.autobuild.pipeline.definiton.repository.PipelineRepository;
import com.autobuild.pipeline.definiton.service.PipelineService;
import com.autobuild.pipeline.testutility.DummyData;
import com.autobuild.pipeline.utility.file.PipelineFileService;

import jakarta.persistence.EntityNotFoundException;

public class PipelineServiceTest {

    private Pipeline pipelineDTO = DummyData.getPipelineDTO();
    private PipelineEntity pipeline = DummyData.getPipeline();

    @Mock
    private PipelineRepository repository;

    @Mock
    private PipelineMapper mapper;

    @Mock
    private PipelineFileService fileService;

    @Mock
    private StageMapper stageMapper;

    @InjectMocks
    private PipelineService service;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        doAnswer(inv -> {
            PipelineEntity p = inv.getArgument(0);
            List<Stage> stages = p.getStages().stream().map(s ->
                Stage.newBuilder()
                    .setId(s.getId().toString())
                    .setName(s.getName())
                    .setScriptType(s.getScriptType())
                    .setCommand("")
                    .build()
            ).collect(Collectors.toList());
            return Pipeline.newBuilder()
                .setId(p.getId().toString())
                .setName(p.getName())
                .addAllStages(stages)
                .build();
        }).when(mapper).entityToDto(any(PipelineEntity.class));

        doAnswer(inv -> {
            Pipeline dto = inv.getArgument(0);
            PipelineEntity p = new PipelineEntity();
            if (!dto.getId().isBlank()) p.setId(UUID.fromString(dto.getId()));
            p.setName(dto.getName());
            p.setStages(dto.getStagesList().stream().map(st -> {
                StageEntity s = new StageEntity();
                if (!st.getId().isBlank()) s.setId(UUID.fromString(st.getId()));
                s.setName(st.getName());
                s.setScriptType(st.getScriptType());
                return s;
            }).collect(Collectors.toList()));
            return p;
        }).when(mapper).dtoToEntity(any(Pipeline.class));

        doAnswer(inv -> {
            Stage sd = inv.getArgument(0);
            StageEntity s = new StageEntity();
            if (!sd.getId().isBlank()) s.setId(UUID.fromString(sd.getId()));
            s.setName(sd.getName());
            s.setScriptType(sd.getScriptType());
            return s;
        }).when(stageMapper).dtoToEntity(any(Stage.class));
    }

    @Test
    public void testGetPipelineByIdWithEmptyId() {
        assertThrows(InvalidIdException.class, () -> service.getPipelineById(null));
        assertThrows(InvalidIdException.class, () -> service.getPipelineById(""));
        assertThrows(InvalidIdException.class, () -> service.getPipelineById(" "));
    }

    @Test
    public void testGetPipelineByIdWithInvalidId() {
        try (MockedStatic<UUID> uuid = mockStatic(UUID.class)) {
            uuid.when(() -> UUID.fromString(anyString()))
                    .thenThrow(new IllegalArgumentException("bad"));
            assertThrows(InvalidIdException.class, () -> service.getPipelineById("not-a-uuid"));
        }
    }

    @Test
    public void testGetAllPipelines() {
        List<PipelineEntity> pipelines = List.of(pipeline);
        doReturn(pipelines).when(repository).findAll();
        List<Pipeline> result = service.getAllPipelines();
        assertEquals(1, result.size());
        assertEquals(pipeline.getId().toString(), result.get(0).getId());
    }

    @Test
    public void testGetPipelineByIdWithValidId() throws Exception {
        UUID id = UUID.randomUUID();
        pipeline.setId(id);
        Map<UUID, String> stageContents = new HashMap<>();
        pipelineDTO.getStagesList().forEach(s -> stageContents.put(UUID.fromString(s.getId()), s.getCommand()));
        doReturn(Optional.of(pipeline)).when(repository).findById(id);
        doReturn(pipelineDTO).when(mapper).entityToDto(pipeline);
        doReturn(stageContents).when(fileService).readScriptFiles(pipeline);
        assertEquals(pipelineDTO, service.getPipelineById(id.toString()));
    }

    @Test
    public void testGetPipelineByIdWithValidIdButNoPipeline() {
        UUID id = UUID.randomUUID();
        doReturn(Optional.empty()).when(repository).findById(id);
        assertThrows(EntityNotFoundException.class, () -> service.getPipelineById(id.toString()));
    }

    @Test
    public void testModifyPipelineStagesDuplicateUpdateName() throws Exception {
        ensurePipelineId();
        pipeline.setStages(new ArrayList<>(pipeline.getStages()));
        if (pipeline.getStages().size() < 2)
            return;
        doReturn(Optional.of(pipeline)).when(repository).findById(pipeline.getId());
        StageEntity target = pipeline.getStages().get(0);
        String otherName = pipeline.getStages().get(1).getName();
        Stage update = Stage.newBuilder().setId(target.getId().toString()).setName(otherName).build();
        Pipeline request = Pipeline.newBuilder().addAllStages(List.of(update)).build();
        assertThrows(DuplicateEntryException.class,
                () -> service.modifyPipeline(pipeline.getId().toString(), request));
    }

    @Test
    public void testModifyPipelineStagesEmptyList() {
        ensurePipelineId();
        Pipeline request = Pipeline.newBuilder().build();
        assertThrows(InvalidIdException.class,
                () -> service.modifyPipeline(pipeline.getId().toString(), request));
    }

    private void ensurePipelineId() {
        if (pipeline.getId() == null) {
            pipeline.setId(UUID.randomUUID());
        }
    }

    @Test
    public void testCreatePipelineAllowsDuplicateStageNames() throws Exception {
        Pipeline dto = Pipeline.newBuilder()
            .setName("pipe-two")
            .addAllStages(List.of(
                Stage.newBuilder().setName("build").setScriptType("bash").setCommand("echo build").setStageOrder(0).build(),
                Stage.newBuilder().setName("Build").setScriptType("bash").setCommand("echo build2").setStageOrder(1).build()))
            .build();
        doReturn(false).when(repository).existsByName("pipe-two");
        doAnswer(inv -> {
            PipelineEntity p = inv.getArgument(0);
            p.setId(p.getId() == null ? UUID.randomUUID() : p.getId());
            return p;
        }).when(repository).save(any(PipelineEntity.class));
        doReturn(new HashMap<UUID, String>()).when(fileService).createScriptFiles(dto);
        doReturn(new HashMap<UUID, String>()).when(fileService).readScriptFiles(any(PipelineEntity.class));

        Pipeline created = service.createPipeline(dto);
        assertEquals(2, created.getStagesList().size());
        List<String> names = created.getStagesList().stream().map(Stage::getName).toList();
        assertEquals(true, names.contains("build"));
        assertEquals(true, names.contains("Build"));
    }

    @Test
    public void testModifyPipelineStagesDuplicateCreateNameAllowed() throws Exception {
        ensurePipelineId();
        pipeline.setStages(new ArrayList<>(pipeline.getStages()));
        doReturn(Optional.of(pipeline)).when(repository).findById(pipeline.getId());
        doReturn(pipeline).when(repository).save(any(PipelineEntity.class));
        doReturn(new HashMap<UUID, String>()).when(fileService).readScriptFiles(any(PipelineEntity.class));
        doReturn("path").when(fileService).createStageScriptFile(any(PipelineEntity.class), any(Stage.class));

        String existingName = pipeline.getStages().get(0).getName();
        Stage duplicate = Stage.newBuilder().setName(existingName).setScriptType("bash").setCommand("echo X").build();
        Pipeline request = Pipeline.newBuilder().addAllStages(List.of(duplicate)).build();

        int originalSize = pipeline.getStages().size();
        Pipeline result = service.modifyPipeline(pipeline.getId().toString(), request);
        assertEquals(originalSize + 1, result.getStagesList().size());
    }

    @Test
    public void testModifyPipelineStagesDuplicateUpdateNameAllowed() throws Exception {
        ensurePipelineId();
        pipeline.setStages(new ArrayList<>(pipeline.getStages()));
        if (pipeline.getStages().size() < 2)
            return;
        StageEntity target = pipeline.getStages().get(0);
        String otherName = pipeline.getStages().get(1).getName();

        doReturn(Optional.of(pipeline)).when(repository).findById(pipeline.getId());
        doReturn(pipeline).when(repository).save(any(PipelineEntity.class));
        doReturn(new HashMap<UUID, String>()).when(fileService).readScriptFiles(any(PipelineEntity.class));

        Stage update = Stage.newBuilder().setId(target.getId().toString()).setName(otherName).build();
        Pipeline request = Pipeline.newBuilder().addAllStages(List.of(update)).build();

        Pipeline result = service.modifyPipeline(pipeline.getId().toString(), request);
        String updatedName = result.getStagesList().stream()
                .filter(s -> s.getId().equals(target.getId().toString()))
                .findFirst().orElseThrow().getName();
        assertEquals(otherName, updatedName);
    }

    @Test
    public void testModifyPipelineNameDuplicate() throws Exception {
        ensurePipelineId();
        pipeline.setName("original");
        doReturn(Optional.of(pipeline)).when(repository).findById(pipeline.getId());
        doReturn(true).when(repository).existsByName("newName");
        Pipeline patch = Pipeline.newBuilder().setName("newName").build();
        assertThrows(DuplicateEntryException.class,
                () -> service.modifyPipeline(pipeline.getId().toString(), patch));
    }

    @Test
    public void testModifyPipelineNameUpdateSuccess() throws Exception {
        ensurePipelineId();
        pipeline.setName("oldName");
        doReturn(Optional.of(pipeline)).when(repository).findById(pipeline.getId());
        doReturn(false).when(repository).existsByName("updatedName");
        doReturn(pipeline).when(repository).save(any(PipelineEntity.class));
        doReturn(new HashMap<UUID, String>()).when(fileService).readScriptFiles(any(PipelineEntity.class));

        Pipeline patch = Pipeline.newBuilder().setName("updatedName").build();
        Pipeline result = service.modifyPipeline(pipeline.getId().toString(), patch);
        assertEquals("updatedName", result.getName());
    }

    @Test
    public void testModifyPipelineNoNameNoStages() {
        ensurePipelineId();
        Pipeline patch = Pipeline.newBuilder().build();
        assertThrows(InvalidIdException.class,
                () -> service.modifyPipeline(pipeline.getId().toString(), patch));
    }

    @Test
    public void testModifyPipelineStageCreateMissingFields() throws Exception {
        ensurePipelineId();
        pipeline.setStages(new ArrayList<>(pipeline.getStages()));
        doReturn(Optional.of(pipeline)).when(repository).findById(pipeline.getId());

        Stage badCreate = Stage.newBuilder().setName("build").setCommand("echo hi").build();
        Pipeline patch = Pipeline.newBuilder().addAllStages(List.of(badCreate)).build();

        assertThrows(InvalidIdException.class,
                () -> service.modifyPipeline(pipeline.getId().toString(), patch));
    }

    @Test
    public void testModifyPipelineStageUpdateNoChanges() throws Exception {
        ensurePipelineId();
        pipeline.setStages(new ArrayList<>(pipeline.getStages()));
        StageEntity existing = pipeline.getStages().get(0);
        doReturn(Optional.of(pipeline)).when(repository).findById(pipeline.getId());
        doReturn(pipeline).when(repository).save(any(PipelineEntity.class));
        doReturn(new HashMap<UUID, String>()).when(fileService).readScriptFiles(any(PipelineEntity.class));

        Stage update = Stage.newBuilder().setId(existing.getId().toString()).setName(existing.getName()).build();
        Pipeline patch = Pipeline.newBuilder().addAllStages(List.of(update)).build();

        Pipeline result = service.modifyPipeline(pipeline.getId().toString(), patch);
        assertEquals(existing.getName(),
                result.getStagesList().stream()
                        .filter(s -> s.getId().equals(existing.getId().toString()))
                        .findFirst().orElseThrow().getName());
    }

    @Test
    public void testModifyPipelineDeleteStageNotFound() {
        ensurePipelineId();
        pipeline.setStages(new ArrayList<>(pipeline.getStages()));
        doReturn(Optional.of(pipeline)).when(repository).findById(pipeline.getId());

        Stage del = Stage.newBuilder().setId(UUID.randomUUID().toString()).build();
        Pipeline patch = Pipeline.newBuilder().addAllStages(List.of(del)).build();

        assertThrows(InvalidIdException.class,
                () -> service.modifyPipeline(pipeline.getId().toString(), patch));
    }

    @Test
    public void testCreatePipelineSuccess() throws Exception {
        Pipeline dto = Pipeline.newBuilder()
            .setName("pipe-one")
            .addAllStages(List.of(
                Stage.newBuilder().setName("build").setScriptType("bash").setCommand("echo build").setStageOrder(0).build(),
                Stage.newBuilder().setName("test").setScriptType("bash").setCommand("echo test").setStageOrder(1).build()))
            .build();
        doReturn(false).when(repository).existsByName("pipe-one");
        doAnswer(inv -> {
            PipelineEntity p = inv.getArgument(0);
            p.setId(p.getId() == null ? UUID.randomUUID() : p.getId());
            return p;
        }).when(repository).save(any(PipelineEntity.class));
        doReturn(new HashMap<UUID, String>()).when(fileService).createScriptFiles(dto);
        doReturn(new HashMap<UUID, String>()).when(fileService).readScriptFiles(any(PipelineEntity.class));
        Pipeline created = service.createPipeline(dto);
        assertEquals("pipe-one", created.getName());
        assertEquals(2, created.getStagesList().size());
    }

    @Test
    public void testCreatePipelineDuplicatePipelineName() {
        Pipeline dto = Pipeline.newBuilder().setName("existing").build();
        doReturn(true).when(repository).existsByName("existing");
        assertThrows(DuplicateEntryException.class, () -> service.createPipeline(dto));
    }

    @Test
    public void testDeletePipelineSuccess() throws Exception {
        ensurePipelineId();
        pipeline.setStages(new ArrayList<>(pipeline.getStages()));
        doReturn(Optional.of(pipeline)).when(repository).findById(pipeline.getId());
        doNothing().when(repository).delete(any(PipelineEntity.class));
        doReturn(new HashMap<UUID, String>()).when(fileService).createScriptFiles(any(Pipeline.class));
        doNothing().when(fileService).removeScriptFiles(any(Pipeline.class));
        service.deletePipelineById(pipeline.getId().toString());
        verify(repository).delete(pipeline);
    }

    @Test
    public void testDeletePipelineNotFound() {
        UUID id = UUID.randomUUID();
        doReturn(Optional.empty()).when(repository).findById(id);
        assertThrows(EntityNotFoundException.class, () -> service.deletePipelineById(id.toString()));
    }

    @Test
    public void testModifyPipelineStageDeleteSuccess() throws Exception {
        ensurePipelineId();
        pipeline.setStages(new ArrayList<>(pipeline.getStages()));
        StageEntity toDelete = pipeline.getStages().get(0);
        doReturn(Optional.of(pipeline)).when(repository).findById(pipeline.getId());
        doReturn(pipeline).when(repository).save(any(PipelineEntity.class));
        doNothing().when(fileService).removeStageScriptFile(any(StageEntity.class));
        doReturn(new HashMap<UUID, String>()).when(fileService).readScriptFiles(any(PipelineEntity.class));

        Stage del = Stage.newBuilder().setId(toDelete.getId().toString()).build();
        Pipeline patch = Pipeline.newBuilder().addAllStages(List.of(del)).build();

        Pipeline result = service.modifyPipeline(pipeline.getId().toString(), patch);
        assertThrows(NoSuchElementException.class,
                () -> result.getStagesList().stream()
                        .filter(s -> s.getId().equals(toDelete.getId().toString()))
                        .findFirst().orElseThrow());
    }

    @Test
    public void testModifyPipelineMixedOperations() throws Exception {
        ensurePipelineId();
        pipeline.setStages(new ArrayList<>(pipeline.getStages()));
        StageEntity existing = pipeline.getStages().get(0);
        doReturn(Optional.of(pipeline)).when(repository).findById(pipeline.getId());
        doReturn(pipeline).when(repository).save(any(PipelineEntity.class));
        doReturn(new HashMap<UUID, String>()).when(fileService).readScriptFiles(any(PipelineEntity.class));
        doNothing().when(fileService).updateStageScriptFile(any(StageEntity.class), anyString());
        doReturn("path").when(fileService).createStageScriptFile(any(PipelineEntity.class), any(Stage.class));
        Stage create = Stage.newBuilder().setName("deploy").setScriptType("bash").setCommand("echo deploy").build();
        Stage updateStage = Stage.newBuilder().setId(existing.getId().toString()).setName("renamed").setCommand("echo changed").build();
        Pipeline patch = Pipeline.newBuilder().addAllStages(List.of(create, updateStage)).build();
        Pipeline result = service.modifyPipeline(pipeline.getId().toString(), patch);
        assertEquals(pipeline.getStages().size(), result.getStagesList().size());
        assertEquals("renamed",
                result.getStagesList().stream()
                        .filter(s -> s.getId().equals(existing.getId().toString()))
                        .findFirst().orElseThrow().getName());
    }

    @Test
    public void testModifyPipelineStageUpdateScriptOnly() throws Exception {
        ensurePipelineId();
        pipeline.setStages(new ArrayList<>(pipeline.getStages()));
        StageEntity existing = pipeline.getStages().get(0);
        doReturn(Optional.of(pipeline)).when(repository).findById(pipeline.getId());
        doReturn(pipeline).when(repository).save(any(PipelineEntity.class));
        doReturn(new HashMap<UUID, String>()).when(fileService).readScriptFiles(any(PipelineEntity.class));
        doNothing().when(fileService).updateStageScriptFile(any(StageEntity.class), anyString());
        Stage update = Stage.newBuilder().setId(existing.getId().toString()).setName(existing.getName()).setCommand("echo new").build();
        Pipeline patch = Pipeline.newBuilder().addAllStages(List.of(update)).build();
        Pipeline result = service.modifyPipeline(pipeline.getId().toString(), patch);
        assertEquals(existing.getName(),
                result.getStagesList().stream()
                        .filter(s -> s.getId().equals(existing.getId().toString()))
                        .findFirst().orElseThrow().getName());
    }

    @Test
    public void testReplacePipelineStagesCompletely() throws Exception {
        ensurePipelineId();
        pipeline.setName("existing-name");
        pipeline.setStages(new ArrayList<>(List.of(
            createStage("build", "bash"),
            createStage("test", "bash")
        )));

        doReturn(Optional.of(pipeline)).when(repository).findById(pipeline.getId());
        doReturn(false).when(repository).existsByName("existing-name");
        doReturn(pipeline).when(repository).save(any(PipelineEntity.class));
        doReturn(new HashMap<UUID, String>()).when(fileService).readScriptFiles(any(PipelineEntity.class));
        doReturn("path").when(fileService).createStageScriptFile(any(PipelineEntity.class), any(Stage.class));
        doNothing().when(fileService).removeStageScriptFile(any(StageEntity.class));

        Pipeline request = Pipeline.newBuilder()
            .setName("existing-name")
            .addAllStages(List.of(Stage.newBuilder().setName("deploy").setScriptType("bash").setCommand("echo deploy").build()))
            .build();

        Pipeline result = service.replacePipeline(pipeline.getId().toString(), request);
        assertEquals(1, result.getStagesList().size());
        assertEquals("deploy", result.getStagesList().get(0).getName());
    }

    @Test
    public void testReplacePipelineMissingScriptType() throws Exception {
        ensurePipelineId();
        pipeline.setName("test-pipeline");
        pipeline.setStages(new ArrayList<>());
        doReturn(Optional.of(pipeline)).when(repository).findById(pipeline.getId());
        doReturn(false).when(repository).existsByName("test-pipeline");

        Pipeline request = Pipeline.newBuilder()
            .setName("test-pipeline")
            .addAllStages(List.of(Stage.newBuilder().setName("stage").setCommand("echo test").build()))
            .build();

        assertThrows(InvalidIdException.class,
                () -> service.replacePipeline(pipeline.getId().toString(), request));
    }

    @Test
    public void testReplacePipelineNameAndStages() throws Exception {
        ensurePipelineId();
        pipeline.setName("oldName");
        pipeline.setStages(new ArrayList<>(List.of(createStage("old", "bash"))));

        doReturn(Optional.of(pipeline)).when(repository).findById(pipeline.getId());
        doReturn(false).when(repository).existsByName("newName");
        doReturn(pipeline).when(repository).save(any(PipelineEntity.class));
        doReturn(new HashMap<UUID, String>()).when(fileService).readScriptFiles(any(PipelineEntity.class));
        doReturn("path").when(fileService).createStageScriptFile(any(PipelineEntity.class), any(Stage.class));
        doNothing().when(fileService).removeStageScriptFile(any(StageEntity.class));

        Pipeline request = Pipeline.newBuilder()
            .setName("newName")
            .addAllStages(List.of(Stage.newBuilder().setName("stage2").setScriptType("bash").setCommand("echo world").build()))
            .build();

        Pipeline result = service.replacePipeline(pipeline.getId().toString(), request);
        assertEquals("newName", result.getName());
        assertEquals(1, result.getStagesList().size());
    }

    @Test
    public void testReplacePipelineDuplicateName() throws Exception {
        ensurePipelineId();
        pipeline.setName("original");
        doReturn(Optional.of(pipeline)).when(repository).findById(pipeline.getId());
        doReturn(true).when(repository).existsByName("existingName");

        Pipeline request = Pipeline.newBuilder().setName("existingName").build();

        assertThrows(DuplicateEntryException.class,
                () -> service.replacePipeline(pipeline.getId().toString(), request));
    }

    private StageEntity createStage(String name, String scriptType) {
        StageEntity stage = new StageEntity();
        stage.setId(UUID.randomUUID());
        stage.setName(name);
        stage.setScriptType(scriptType);
        return stage;
    }

    @Test
    public void testCreatePipelineRejectsNegativeStageOrder() {
        Pipeline dto = Pipeline.newBuilder()
            .setName("bad-pipe")
            .addAllStages(List.of(
                Stage.newBuilder().setName("first").setScriptType("bash").setCommand("echo 1").setStageOrder(0).build(),
                Stage.newBuilder().setName("second").setScriptType("bash").setCommand("echo 2").setStageOrder(-1).build()))
            .build();
        doReturn(false).when(repository).existsByName("bad-pipe");
        assertThrows(InvalidIdException.class, () -> service.createPipeline(dto));
    }

    @Test
    public void testCreatePipelineRejectsDuplicateStageOrder() {
        Pipeline dto = Pipeline.newBuilder()
            .setName("dup-pipe")
            .addAllStages(List.of(
                Stage.newBuilder().setName("first").setScriptType("bash").setCommand("echo 1").setStageOrder(0).build(),
                Stage.newBuilder().setName("second").setScriptType("bash").setCommand("echo 2").setStageOrder(0).build()))
            .build();
        doReturn(false).when(repository).existsByName("dup-pipe");
        assertThrows(DuplicateEntryException.class, () -> service.createPipeline(dto));
    }
}
