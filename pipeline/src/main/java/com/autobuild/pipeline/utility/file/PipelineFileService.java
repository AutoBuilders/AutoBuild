package com.autobuild.pipeline.utility.file;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import com.autobuild.pipeline.definiton.entity.PipelineEntity;
import com.autobuild.pipeline.definiton.entity.StageEntity;
import com.autobuild.pipeline.executor.entity.PipelineBuildEntity;
import com.autobuild.pipeline.executor.entity.StageBuildEntity;
import com.autobuild.pipeline.definiton.dto.Pipeline;
import com.autobuild.pipeline.definiton.dto.Stage;

/**
 * General Interface for any file related operations for pipeline.
 * 
 * @author Suvabrata Chowdhury
 */
public interface PipelineFileService {
    public Map<UUID, String> readScriptFiles(final PipelineEntity pipeline) throws IOException;
    public Map<UUID, String> createScriptFiles(final Pipeline pipeline) throws IOException;
    public void removeScriptFiles(final Pipeline pipeline) throws IOException;
    public void removeScriptFiles(final PipelineEntity pipeline) throws IOException;

    public String createStageScriptFile(final PipelineEntity pipeline, final Stage stage) throws IOException;
    public void updateStageScriptFile(final StageEntity stage, final String command) throws IOException;
    public void removeStageScriptFile(final StageEntity stage) throws IOException;
    String readStageScriptFile(final StageEntity stage) throws IOException;

    public String createLogFile(final UUID pipelineBuildId, final UUID stageBuildId) throws IOException;
    public void removeLogFiles(final PipelineBuildEntity pipelineBuid) throws IOException;
    public String readStageBuildLogFile(StageBuildEntity stageBuild) throws IOException;
}
