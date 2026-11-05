package com.autobuild.pipeline.definiton.entity;

import java.util.UUID;

import org.hibernate.annotations.Check;
import org.hibernate.annotations.Checks;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Stage Entity Definition.
 * 
 * @author Suvabrata Chowdhury
 */

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "stage")
@Checks({
    @Check(name = "CHK_STAGE_NAME", constraints = "name != '' "),
    @Check(name = "CHK_STAGE_SCRIPT_TYPE", constraints = "script_type != '' "),
    @Check(name = "CHK_PATH_COMMAND", constraints = "path != '' ")
})
public class StageEntity {
    @Id
    @Column(name = "id")
    private UUID id;

    @NotEmpty
    @Column(name = "name", nullable = false)
    private String name;

    @NotEmpty
    @Column(name = "script_type", nullable = false)
    private String scriptType;

    @NotEmpty
    @Column(name = "path", nullable = false)
    private String path;
}
