package com.autobuild.pipeline.executor.entity;

import java.util.List;
import java.util.UUID;

import com.autobuild.pipeline.definiton.entity.PipelineEntity;
import com.autobuild.pipeline.executor.execution.state.PipelineExecutionStateInternal;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * PipelineBuild entity to hold data/metadata about execution in table.
 *
 * @author Suvabrata Chowdhury
 */

// TODO: Add timestamps
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "pipeline_build")
public class PipelineBuildEntity {
    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "pipeline_id", referencedColumnName = "id", nullable = false)
    private PipelineEntity pipeline;

    @NotEmpty
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "pipeline_stage_builds", joinColumns = @JoinColumn(name = "pipeline_build_id"), inverseJoinColumns = @JoinColumn(name = "stage_build_id"))
    private List<StageBuildEntity> stageBuilds;

    @Enumerated(EnumType.STRING)
    @Column(name = "pipeline_build_current_state", nullable = false)
    private PipelineExecutionStateInternal currentState;

    public PipelineBuildEntity(PipelineEntity pipeline, List<StageBuildEntity> stageBuilds) {
        this.pipeline = pipeline;
        this.stageBuilds = stageBuilds;
    }

    @PrePersist
    public void setDefaultState() {
        if (null == this.currentState) {
            this.currentState = PipelineExecutionStateInternal.WAITING;
        }
    }
}