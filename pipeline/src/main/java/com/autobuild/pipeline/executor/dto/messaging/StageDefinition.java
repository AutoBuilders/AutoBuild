package com.autobuild.pipeline.executor.dto.messaging;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a single stage within a pipeline build request message.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class StageDefinition {

    private String name;
    private String scriptType;
    private String command;
    private int stageOrder;
}
