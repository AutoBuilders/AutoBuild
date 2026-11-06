package com.autobuild.pipeline.executor.entity;

import java.util.UUID;

import com.autobuild.pipeline.definiton.entity.StageEntity;
import com.autobuild.pipeline.executor.execution.state.StageExecutionStateInternal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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
@Table(name = "stage_build")
public class StageBuildEntity {
    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "stage_id", referencedColumnName = "id", nullable = false)
    private StageEntity stage;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage_build_current_state", nullable = false)
    private StageExecutionStateInternal currentState;

    @Column(name = "log_path", nullable = false)
    private String logPath;

    public StageBuildEntity(StageEntity stage) {
        this.stage = stage;
    }

    @PrePersist
    public void setDefaultState() {
        if (null == this.currentState) {
            this.currentState = StageExecutionStateInternal.WAITING;
        }
    }
}