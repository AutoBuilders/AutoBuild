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
import com.autobuild.pipeline.definiton.dto.PipelineDTO;
import com.autobuild.pipeline.definiton.dto.StageDTO;
import com.autobuild.pipeline.definiton.dto.mapper.PipelineMapper;
import com.autobuild.pipeline.definiton.dto.mapper.StageMapper;
import com.autobuild.pipeline.definiton.entity.Pipeline;
import com.autobuild.pipeline.definiton.entity.Stage;
import com.autobuild.pipeline.definiton.exceptions.DuplicateEntryException;
import com.autobuild.pipeline.definiton.exceptions.InvalidIdException;
import com.autobuild.pipeline.definiton.repository.PipelineRepository;
import com.autobuild.pipeline.definiton.service.PipelineService;
import com.autobuild.pipeline.testutility.DummyData;
import com.autobuild.pipeline.utility.file.PipelineFileService;
import jakarta.persistence.EntityNotFoundException;

public class PipelineServiceTest {

    private PipelineDTO pipelineDTO = DummyData.getPipelineDTO();
    private Pipeline pipeline = DummyData.getPipeline();

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

        // Mapper behavior to reflect pipeline changes
        doAnswer(inv -> {
            Pipeline p = inv.getArgument(0);
            PipelineDTO dto = new PipelineDTO();
            dto.setId(p.getId());
            dto.setName(p.getName());
            List<StageDTO> stages = p.getStages().stream().map(s -> {
                StageDTO sd = new StageDTO();
                sd.setId(s.getId());
                sd.setName(s.getName());
                sd.setScriptType(s.getScriptType());
                sd.setCommand("");
                return sd;
            }).collect(Collectors.toList());
            dto.setStages(stages);
            return dto;
        }).when(mapper).entityToDto(any(Pipeline.class));

        doAnswer(inv -> {
            PipelineDTO dto = inv.getArgument(0);
            Pipeline p = new Pipeline();
            p.setId(dto.getId());
            p.setName(dto.getName());
            p.setStages(dto.getStages().stream().map(st -> {
                Stage s = new Stage();
                s.setId(st.getId());
                s.setName(st.getName());
                s.setScriptType(st.getScriptType());
                return s;
            }).collect(Collectors.toList()));
            return p;
        }).when(mapper).dtoToEntity(any(PipelineDTO.class));

        doAnswer(inv -> {
            StageDTO sd = inv.getArgument(0);
            Stage s = new Stage();
            s.setId(sd.getId());
            s.setName(sd.getName());
            s.setScriptType(sd.getScriptType());
            return s;
        }).when(stageMapper).dtoToEntity(any(StageDTO.class));
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
        List<Pipeline> pipelines = List.of(pipeline);
        doReturn(pipelines).when(repository).findAll();
        List<PipelineDTO> result = service.getAllPipelines();
        assertEquals(1, result.size());
        assertEquals(pipeline.getId(), result.get(0).getId());
    }

    @Test
    public void testGetPipelineByIdWithValidId() throws Exception {
        UUID id = UUID.randomUUID();
        pipeline.setId(id);
        doReturn(Optional.of(pipeline)).when(repository).findById(id);
        doReturn(pipelineDTO).when(mapper).entityToDto(pipeline);
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
        Stage target = pipeline.getStages().get(0);
        String otherName = pipeline.getStages().get(1).getName();
        StageDTO update = new StageDTO(target.getId(), otherName, null, null);
        PipelineDTO request = new PipelineDTO();
        request.setStages(List.of(update));
        assertThrows(DuplicateEntryException.class,
                () -> service.modifyPipeline(pipeline.getId().toString(), request));
    }

    @Test
    public void testModifyPipelineStagesEmptyList() {
        ensurePipelineId();
        PipelineDTO request = new PipelineDTO();
        request.setStages(List.of());
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
        PipelineDTO dto = new PipelineDTO();
        dto.setName("pipe-two");
        dto.setStages(List.of(
                new StageDTO(null, "build", "bash", "echo build", 0),
                new StageDTO(null, "Build", "bash", "echo build2", 1))); // same name case-insensitive
        doReturn(false).when(repository).existsByName("pipe-two");
        doAnswer(inv -> {
            Pipeline p = inv.getArgument(0);
            p.setId(p.getId() == null ? UUID.randomUUID() : p.getId());
            return p;
        }).when(repository).save(any(Pipeline.class));
        doReturn(new HashMap<UUID, String>()).when(fileService).createScriptFiles(dto);
        doReturn(new HashMap<UUID, String>()).when(fileService).readScriptFiles(any(Pipeline.class));

        PipelineDTO created = service.createPipeline(dto);
        assertEquals(2, created.getStages().size());
        // both names present
        List<String> names = created.getStages().stream().map(StageDTO::getName).toList();
        assertEquals(true, names.contains("build"));
        assertEquals(true, names.contains("Build"));
    }

    @Test
    public void testModifyPipelineStagesDuplicateCreateNameAllowed() throws Exception {
        ensurePipelineId();
        pipeline.setStages(new ArrayList<>(pipeline.getStages()));
        doReturn(Optional.of(pipeline)).when(repository).findById(pipeline.getId());
        doReturn(pipeline).when(repository).save(any(Pipeline.class));
        doReturn(new HashMap<UUID, String>()).when(fileService).readScriptFiles(any(Pipeline.class));
        doReturn("path").when(fileService).createStageScriptFile(any(Pipeline.class), any(StageDTO.class));

        String existingName = pipeline.getStages().get(0).getName();
        StageDTO duplicate = new StageDTO(null, existingName, "bash", "echo X");
        PipelineDTO request = new PipelineDTO();
        request.setStages(List.of(duplicate));

        int originalSize = pipeline.getStages().size();
        PipelineDTO result = service.modifyPipeline(pipeline.getId().toString(), request);
        assertEquals(originalSize + 1, result.getStages().size());
    }

    @Test
    public void testModifyPipelineStagesDuplicateUpdateNameAllowed() throws Exception {
        ensurePipelineId();
        pipeline.setStages(new ArrayList<>(pipeline.getStages()));
        if (pipeline.getStages().size() < 2)
            return;
        Stage target = pipeline.getStages().get(0);
        String otherName = pipeline.getStages().get(1).getName();

        doReturn(Optional.of(pipeline)).when(repository).findById(pipeline.getId());
        doReturn(pipeline).when(repository).save(any(Pipeline.class));
        doReturn(new HashMap<UUID, String>()).when(fileService).readScriptFiles(any(Pipeline.class));

        StageDTO update = new StageDTO(target.getId(), otherName, null, null);
        PipelineDTO request = new PipelineDTO();
        request.setStages(List.of(update));

        PipelineDTO result = service.modifyPipeline(pipeline.getId().toString(), request);
        String updatedName = result.getStages().stream()
                .filter(s -> s.getId().equals(target.getId()))
                .findFirst()
                .orElseThrow()
                .getName();
        assertEquals(otherName, updatedName);
    }

    @Test
    public void testModifyPipelineNameDuplicate() throws Exception {
        ensurePipelineId();
        pipeline.setName("original");
        doReturn(Optional.of(pipeline)).when(repository).findById(pipeline.getId());
        doReturn(true).when(repository).existsByName("newName");
        PipelineDTO patch = new PipelineDTO();
        patch.setName("newName");
        assertThrows(DuplicateEntryException.class,
                () -> service.modifyPipeline(pipeline.getId().toString(), patch));
    }

    @Test
    public void testModifyPipelineNameUpdateSuccess() throws Exception {
        ensurePipelineId();
        pipeline.setName("oldName");
        doReturn(Optional.of(pipeline)).when(repository).findById(pipeline.getId());
        doReturn(false).when(repository).existsByName("updatedName");
        doReturn(pipeline).when(repository).save(any(Pipeline.class));
        doReturn(new HashMap<UUID, String>()).when(fileService).readScriptFiles(any(Pipeline.class));

        PipelineDTO patch = new PipelineDTO();
        patch.setName("updatedName");
        PipelineDTO result = service.modifyPipeline(pipeline.getId().toString(), patch);
        assertEquals("updatedName", result.getName());
    }

    @Test
    public void testModifyPipelineNoNameNoStages() {
        ensurePipelineId();
        PipelineDTO patch = new PipelineDTO();
        assertThrows(InvalidIdException.class,
                () -> service.modifyPipeline(pipeline.getId().toString(), patch));
    }

    @Test
    public void testModifyPipelineStageCreateMissingFields() throws Exception {
        ensurePipelineId();
        pipeline.setStages(new ArrayList<>(pipeline.getStages()));
        doReturn(Optional.of(pipeline)).when(repository).findById(pipeline.getId());

        StageDTO badCreate = new StageDTO(null, "build", null, "echo hi"); // missing scriptType
        PipelineDTO patch = new PipelineDTO();
        patch.setStages(List.of(badCreate));

        assertThrows(InvalidIdException.class,
                () -> service.modifyPipeline(pipeline.getId().toString(), patch));
    }

    @Test
    public void testModifyPipelineStageUpdateNoChanges() throws Exception {
        ensurePipelineId();
        pipeline.setStages(new ArrayList<>(pipeline.getStages()));
        Stage existing = pipeline.getStages().get(0);
        doReturn(Optional.of(pipeline)).when(repository).findById(pipeline.getId());
        doReturn(pipeline).when(repository).save(any(Pipeline.class));
        doReturn(new HashMap<UUID, String>()).when(fileService).readScriptFiles(any(Pipeline.class));

        // Use existing name to avoid triggering delete (all nulls -> delete in
        // service).
        StageDTO update = new StageDTO(existing.getId(), existing.getName(), null, null);
        PipelineDTO patch = new PipelineDTO();
        patch.setStages(List.of(update));

        PipelineDTO result = service.modifyPipeline(pipeline.getId().toString(), patch);
        assertEquals(existing.getName(),
                result.getStages().stream()
                        .filter(s -> s.getId().equals(existing.getId()))
                        .findFirst()
                        .orElseThrow()
                        .getName());
    }

    @Test
    public void testModifyPipelineDeleteStageNotFound() {
        ensurePipelineId();
        pipeline.setStages(new ArrayList<>(pipeline.getStages()));
        doReturn(Optional.of(pipeline)).when(repository).findById(pipeline.getId());

        // Deletion request for non-existent stage
        StageDTO del = new StageDTO(UUID.randomUUID(), null, null, null);
        PipelineDTO patch = new PipelineDTO();
        patch.setStages(List.of(del));

        assertThrows(InvalidIdException.class,
                () -> service.modifyPipeline(pipeline.getId().toString(), patch));
    }

    @Test
    public void testCreatePipelineSuccess() throws Exception {
        PipelineDTO dto = new PipelineDTO();
        dto.setName("pipe-one");
        dto.setStages(List.of(
                new StageDTO(null, "build", "bash", "echo build", 0),
                new StageDTO(null, "test", "bash", "echo test", 1)));
        doReturn(false).when(repository).existsByName("pipe-one");
        doAnswer(inv -> {
            Pipeline p = inv.getArgument(0);
            p.setId(p.getId() == null ? UUID.randomUUID() : p.getId());
            return p;
        }).when(repository).save(any(Pipeline.class));
        doReturn(new HashMap<UUID, String>()).when(fileService).createScriptFiles(dto);
        doReturn(new HashMap<UUID, String>()).when(fileService).readScriptFiles(any(Pipeline.class));
        PipelineDTO created = service.createPipeline(dto);
        assertEquals("pipe-one", created.getName());
        assertEquals(2, created.getStages().size());
    }

    @Test
    public void testCreatePipelineDuplicatePipelineName() {
        PipelineDTO dto = new PipelineDTO();
        dto.setName("existing");
        doReturn(true).when(repository).existsByName("existing");
        assertThrows(DuplicateEntryException.class, () -> service.createPipeline(dto));
    }

    @Test
    public void testCreatePipelineRejectsDuplicateStageOrder() {
        PipelineDTO dto = new PipelineDTO();
        dto.setName("pipe");
        dto.setStages(List.of(
                new StageDTO(null, "build", "bash", "echo build", 0),
                new StageDTO(null, "test", "bash", "echo test", 0)));
        doReturn(false).when(repository).existsByName("pipe");
        assertThrows(DuplicateEntryException.class, () -> service.createPipeline(dto));
    }

    @Test
    public void testCreatePipelineRejectsNegativeStageOrder() {
        PipelineDTO dto = new PipelineDTO();
        dto.setName("pipe");
        dto.setStages(List.of(
                new StageDTO(null, "build", "bash", "echo build", -1)));
        doReturn(false).when(repository).existsByName("pipe");
        assertThrows(InvalidIdException.class, () -> service.createPipeline(dto));
    }

    @Test
    public void testDeletePipelineSuccess() throws Exception {
        ensurePipelineId();
        pipeline.setStages(new ArrayList<>(pipeline.getStages()));
        doReturn(Optional.of(pipeline)).when(repository).findById(pipeline.getId());
        doNothing().when(repository).delete(any(Pipeline.class));
        doReturn(new HashMap<UUID, String>()).when(fileService)
                .createScriptFiles(any(PipelineDTO.class));
        doNothing().when(fileService).removeScriptFiles(any(PipelineDTO.class));
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
        Stage toDelete = pipeline.getStages().get(0);
        doReturn(Optional.of(pipeline)).when(repository).findById(pipeline.getId());
        doReturn(pipeline).when(repository).save(any(Pipeline.class));
        doNothing().when(fileService).removeStageScriptFile(any(Stage.class));
        doReturn(new HashMap<UUID, String>()).when(fileService).readScriptFiles(any(Pipeline.class));

        // Delete by providing only the ID (all other fields null).
        StageDTO del = new StageDTO(toDelete.getId(), null, null, null);
        PipelineDTO patch = new PipelineDTO();
        patch.setStages(List.of(del));

        PipelineDTO result = service.modifyPipeline(pipeline.getId().toString(), patch);
        assertThrows(NoSuchElementException.class,
                () -> result.getStages().stream()
                        .filter(s -> s.getId().equals(toDelete.getId()))
                        .findFirst()
                        .orElseThrow());
    }

    @Test
    public void testModifyPipelineMixedOperations() throws Exception {
        ensurePipelineId();
        pipeline.setStages(new ArrayList<>(pipeline.getStages()));
        Stage existing = pipeline.getStages().get(0);
        doReturn(Optional.of(pipeline)).when(repository).findById(pipeline.getId());
        doReturn(pipeline).when(repository).save(any(Pipeline.class));
        doReturn(new HashMap<UUID, String>()).when(fileService).readScriptFiles(any(Pipeline.class));
        doNothing().when(fileService).updateStageScriptFile(any(Stage.class), anyString());
        doReturn("path").when(fileService).createStageScriptFile(any(Pipeline.class), any(StageDTO.class));
        StageDTO create = new StageDTO(null, "deploy", "bash", "echo deploy");
        StageDTO updateNameAndScript = new StageDTO(existing.getId(), "renamed", null, "echo changed");
        PipelineDTO patch = new PipelineDTO();
        patch.setStages(List.of(create, updateNameAndScript));
        PipelineDTO result = service.modifyPipeline(pipeline.getId().toString(), patch);
        assertEquals(pipeline.getStages().size(), result.getStages().size());
        assertEquals("renamed",
                result.getStages().stream().filter(s -> s.getId().equals(existing.getId())).findFirst().orElseThrow()
                        .getName());
    }

    @Test
    public void testModifyPipelineStageUpdateScriptOnly() throws Exception {
        ensurePipelineId();
        pipeline.setStages(new ArrayList<>(pipeline.getStages()));
        Stage existing = pipeline.getStages().get(0);
        doReturn(Optional.of(pipeline)).when(repository).findById(pipeline.getId());
        doReturn(pipeline).when(repository).save(any(Pipeline.class));
        doReturn(new HashMap<UUID, String>()).when(fileService).readScriptFiles(any(Pipeline.class));
        doNothing().when(fileService).updateStageScriptFile(any(Stage.class), anyString());
        // scriptType must be null (updates are forbidden)
        StageDTO update = new StageDTO(existing.getId(), existing.getName(), null, "echo new");
        PipelineDTO patch = new PipelineDTO();
        patch.setStages(List.of(update));
        PipelineDTO result = service.modifyPipeline(pipeline.getId().toString(), patch);
        assertEquals(existing.getName(),
                result.getStages().stream().filter(s -> s.getId().equals(existing.getId())).findFirst().orElseThrow()
                        .getName());
    }

    @Test
    public void testReplacePipelineStagesCompletely() throws Exception {
        ensurePipelineId();
        pipeline.setName("existing-name"); // Add this line
        pipeline.setStages(new ArrayList<>(List.of(
            createStage("build", "bash"),
            createStage("test", "bash")
        )));
        
        doReturn(Optional.of(pipeline)).when(repository).findById(pipeline.getId());
        doReturn(false).when(repository).existsByName("existing-name"); // Add this line
        doReturn(pipeline).when(repository).save(any(Pipeline.class));
        doReturn(new HashMap<UUID,String>()).when(fileService).readScriptFiles(any(Pipeline.class));
        doReturn("path").when(fileService).createStageScriptFile(any(Pipeline.class), any(StageDTO.class));
        doNothing().when(fileService).removeStageScriptFile(any(Stage.class));

        PipelineDTO request = new PipelineDTO();
        request.setName("existing-name"); // Add this line - PUT requires name
        request.setStages(List.of(new StageDTO(null, "deploy", "bash", "echo deploy")));

        PipelineDTO result = service.replacePipeline(pipeline.getId().toString(), request);
        
        assertEquals(1, result.getStages().size());
        assertEquals("deploy", result.getStages().get(0).getName());
    }

    @Test
    public void testReplacePipelineMissingScriptType() throws Exception {
        ensurePipelineId();
        pipeline.setName("test-pipeline"); // Add this line
        pipeline.setStages(new ArrayList<>());
        doReturn(Optional.of(pipeline)).when(repository).findById(pipeline.getId());
        doReturn(false).when(repository).existsByName("test-pipeline"); // Add this line

        PipelineDTO request = new PipelineDTO();
        request.setName("test-pipeline"); // Add this line - PUT requires name
        request.setStages(List.of(new StageDTO(null, "stage", null, "echo test")));

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
        doReturn(pipeline).when(repository).save(any(Pipeline.class));
        doReturn(new HashMap<UUID,String>()).when(fileService).readScriptFiles(any(Pipeline.class));
        doReturn("path").when(fileService).createStageScriptFile(any(Pipeline.class), any(StageDTO.class));
        doNothing().when(fileService).removeStageScriptFile(any(Stage.class));

        PipelineDTO request = new PipelineDTO();
        request.setName("newName");
        request.setStages(List.of(
            new StageDTO(null, "stage2", "bash", "echo world")
        ));

        PipelineDTO result = service.replacePipeline(pipeline.getId().toString(), request);
        
        assertEquals("newName", result.getName());
        assertEquals(1, result.getStages().size());
    }

    @Test
    public void testReplacePipelineDuplicateName() throws Exception {
        ensurePipelineId();
        pipeline.setName("original");
        doReturn(Optional.of(pipeline)).when(repository).findById(pipeline.getId());
        doReturn(true).when(repository).existsByName("existingName");

        PipelineDTO request = new PipelineDTO();
        request.setName("existingName");

        assertThrows(DuplicateEntryException.class,
                () -> service.replacePipeline(pipeline.getId().toString(), request));
    }
    private Stage createStage(String name, String scriptType) {
        Stage stage = new Stage();
        stage.setId(UUID.randomUUID());
        stage.setName(name);
        stage.setScriptType(scriptType);
        return stage;
    }
}
