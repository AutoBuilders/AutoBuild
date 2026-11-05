package com.autobuild.pipeline.utility.file.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.autobuild.pipeline.definiton.entity.PipelineEntity;
import com.autobuild.pipeline.definiton.entity.StageEntity;
import com.autobuild.pipeline.executor.entity.PipelineBuildEntity;
import com.autobuild.pipeline.executor.entity.StageBuildEntity;
import com.autobuild.pipeline.definiton.dto.Stage;
import com.autobuild.pipeline.definiton.dto.Pipeline;
import com.autobuild.pipeline.utility.file.PipelineFileService;
import com.autobuild.pipeline.utility.file.extension.Extensions;

/**
 * Pipeline file service implementation for storing script file in local machine.
 *
 * The maintained directory structure is, /pipeline_id/stage_id
 *
 * @author Suvabrata Chowdhury
 */

// TODO: Refactor it. for now its an ugly design
@Component
public class LocalPipelineFileServiceImpl implements PipelineFileService {
    private static final Path DEFAULT_SCRIPT_PATH = Path.of("..", "pipeline_scripts");
    private static final Path DEFAULT_SCRIPT_LOG_PATH = Path.of("..", "pipeline_build_logs");

    private static final FileAttribute<Set<PosixFilePermission>> PERMISSIONS = PosixFilePermissions.asFileAttribute(
            Set.of(PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_READ));

    private static final FileAttribute<Set<PosixFilePermission>> LOG_FILE_PERMISSIONS = PosixFilePermissions
            .asFileAttribute(Set.of(
                    // PosixFilePermission.OWNER_EXECUTE,
                    PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_READ));

    @Override
    public Map<UUID, String> readScriptFiles(final PipelineEntity pipeline) throws IOException {
        Map<UUID, String> scriptContents = new HashMap<>();

        try {
            pipeline.getStages().forEach(stage -> {
                try {
                    String content = StringUtils.join(Files.readAllLines(Path.of(stage.getPath())), "\n");
                    scriptContents.put(stage.getId(), content);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });

            return scriptContents;
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    @Override
    public Map<UUID, String> createScriptFiles(final Pipeline pipeline) throws IOException {
        createParentDirectory();

        Path directoryPath = createPipelineDirectory(pipeline);

        Map<UUID, String> scriptPaths = new HashMap<>();
        for (Stage stage : pipeline.getStagesList()) {
            String path = createStageFile(directoryPath, stage);

            scriptPaths.put(UUID.fromString(stage.getId()), path);
        }

        return scriptPaths;
    }

    /**
     * Used when parent directory is known but the path to script is not known.
     */

    @Override
    public void removeScriptFiles(final Pipeline pipeline) throws IOException {
        Path directoryPath = getPipelineDirectoryPath(pipeline.getId());

        try (Stream<Path> paths = Files.walk(directoryPath)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    /**
     * Used when both parent directory and the path to script is known.
     */

    @Override
    public void removeScriptFiles(final PipelineEntity pipeline) throws IOException {
        try {
            pipeline.getStages().forEach(stage -> {
                try {
                    Files.deleteIfExists(Path.of(stage.getPath()));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });

            Files.deleteIfExists(DEFAULT_SCRIPT_PATH.resolve(pipeline.getId().toString()));
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    @Override
    public String createStageScriptFile(PipelineEntity pipeline, Stage stage) throws IOException {
        createParentDirectory();

        Path pipelineDirectoryPath = getPipelineDirectoryPath(pipeline.getId().toString());
        if (!Files.exists(pipelineDirectoryPath)) {
            Files.createDirectories(pipelineDirectoryPath, PERMISSIONS);
        }

        return createStageFile(pipelineDirectoryPath, stage);
    }

    @Override
    public void updateStageScriptFile(StageEntity stage, String command) throws IOException {
        Path filePath = Path.of(stage.getPath());
        Files.write(filePath, command.getBytes());
    }

    @Override
    public void removeStageScriptFile(StageEntity stage) throws IOException {
        Files.deleteIfExists(Path.of(stage.getPath()));
    }

    @Override
    public String readStageScriptFile(final StageEntity stage) throws IOException {
        if (stage == null || StringUtils.isBlank(stage.getPath())) {
            return "";
        }
        return String.join("\n", Files.readAllLines(Path.of(stage.getPath())));
    }

    @Override
    public String createLogFile(UUID pipelineBuildId, UUID stageBuildId) throws IOException {
        createLogsParentDirectory();

        Path buildLogsDirectoryPath = getBuildLogsDirectoryPath(pipelineBuildId.toString());
        if (!Files.exists(buildLogsDirectoryPath)) {
            Files.createDirectories(buildLogsDirectoryPath, PERMISSIONS);
        }

        return createStageLogFile(buildLogsDirectoryPath, stageBuildId.toString());
    }

    @Override
    public void removeLogFiles(final PipelineBuildEntity pipelineBuid) throws IOException {
        for (StageBuildEntity stageBuild : pipelineBuid.getStageBuilds()) {
            Files.deleteIfExists(Path.of(stageBuild.getLogPath()));
        }

        Files.deleteIfExists(DEFAULT_SCRIPT_LOG_PATH.resolve(pipelineBuid.getId().toString()));
    }

    @Override
    public String readStageBuildLogFile(StageBuildEntity stageBuild) throws IOException {
        if (stageBuild == null || StringUtils.isBlank(stageBuild.getLogPath())) {
            throw new IllegalArgumentException("Invalid stageBuild given for log reading");
        }

        return String.join("\n", Files.readAllLines(Path.of(stageBuild.getLogPath())));
    }

    private String createStageLogFile(Path buildLogsDirectoryPath, String stageBuildId) throws IOException {
        String logFileName = stageBuildId + Extensions.nameFor("log");
        Path logFilePath = buildLogsDirectoryPath.resolve(logFileName);

        Files.createFile(logFilePath, LOG_FILE_PERMISSIONS);
        return logFilePath.toString();
    }

    private Path getBuildLogsDirectoryPath(String pipelineBuildId) {
        return DEFAULT_SCRIPT_LOG_PATH.resolve(pipelineBuildId);
    }

    private void createLogsParentDirectory() throws IOException {
        if (!Files.exists(DEFAULT_SCRIPT_LOG_PATH)) {
            Files.createDirectories(DEFAULT_SCRIPT_LOG_PATH, PERMISSIONS);
        }
    }

    private void createParentDirectory() throws IOException {
        if (!Files.exists(DEFAULT_SCRIPT_PATH)) {
            Files.createDirectories(DEFAULT_SCRIPT_PATH, PERMISSIONS);
        }
    }

    private Path createPipelineDirectory(Pipeline pipeline) throws IOException {
        Path directoryPath = getPipelineDirectoryPath(pipeline.getId());
        directoryPath = Files.createDirectories(directoryPath, PERMISSIONS);

        return directoryPath;
    }

    private String createStageFile(Path directoryPath, Stage stage) throws IOException {
        String scriptName = stage.getId() + Extensions.nameFor(stage.getScriptType());
        Path filePath = directoryPath.resolve(scriptName);

        Files.createFile(filePath, PERMISSIONS);
        Files.write(filePath, stage.getCommand().getBytes());

        return filePath.toString();
    }

    private Path getPipelineDirectoryPath(String pipelineId) {
        return DEFAULT_SCRIPT_PATH.resolve(pipelineId);
    }
}
