package com.autobuild.pipeline.executor.entity;

import java.util.UUID;

import com.autobuild.pipeline.definiton.entity.Stage;
import com.autobuild.pipeline.executor.execution.state.StageExecutionState;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * StageBuild entity for holding stage execution metadata/data in table.
 * 
 * @author Suvabrata Chowdhury
 */

//TODO: Add timestamps
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class StageBuild {
    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "stage_id", referencedColumnName = "id", nullable = false)
    private Stage stage;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage_build_current_state", nullable = false)
    private StageExecutionState currentState;

    @Column(name = "stage_order", nullable = false)
    private int stageOrder;

    @Column(name = "log_path", nullable = false)
    private String logPath;

    public StageBuild(Stage stage) {
        this.stage = stage;
        this.stageOrder = stage.getStageOrder();
    }

    public StageBuild(UUID id, Stage stage, StageExecutionState currentState, String logPath) {
        this(id, stage, currentState, stage.getStageOrder(), logPath);
    }

    @PrePersist
    public void setDefaultState() {
        if (null == this.currentState) {
            this.currentState = StageExecutionState.WAITING;
        }
    }
}