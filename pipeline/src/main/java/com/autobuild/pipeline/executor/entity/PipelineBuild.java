package com.autobuild.pipeline.executor.entity;

import java.util.List;
import java.util.UUID;

import com.autobuild.pipeline.definiton.entity.Pipeline;
import com.autobuild.pipeline.executor.execution.state.PipelineExecutionState;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
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

//TODO: Add timestamps
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class PipelineBuild {
    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "pipeline_id", referencedColumnName = "id", nullable = false)
    private Pipeline pipeline;

    @NotEmpty
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(
        name = "pipeline_stage_builds",
        joinColumns = @JoinColumn(name = "pipeline_build_id"),
        inverseJoinColumns = @JoinColumn(name = "stage_build_id")
    )
    @OrderBy("stageOrder ASC")
    private List<StageBuild> stageBuilds;

    @Enumerated(EnumType.STRING)
    @Column(name = "pipeline_build_current_state", nullable = false)
    private PipelineExecutionState currentState;

    public PipelineBuild(Pipeline pipeline, List<StageBuild> stageBuilds) {
        this.pipeline = pipeline;
        this.stageBuilds = stageBuilds;
    }

    @PrePersist
    public void setDefaultState() {
        if (null == this.currentState) {
            this.currentState = PipelineExecutionState.WAITING;
        }
    }
}