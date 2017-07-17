package com.asiainfo.comm.module.models;

import com.avaje.ebean.Model;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
@Entity
@Table(name = "AD_STAGE")
public class AdStage extends Model {
    public static final Finder<Long, AdStage>
        finder = new Finder<Long, AdStage>(Long.class, AdStage.class);

    @Id
    @Size(max = 12)
    Long stageId;

    Integer stageType;

    String jenkinsJobName;

    Long state;

    Integer stageCode;

    Integer step;

    Integer isSpec;

    @Temporal(TemporalType.DATE)
    Date createDate;

    Integer dealResult;

    Long buildSeq;
    @Lob
    String stageConfig;

    @ManyToOne(optional = false)
    @JoinColumn(name = "PIPELINE_ID", nullable = false, updatable = false)
    AdPipeLineState adPipeLineState;

    @ManyToOne(optional = false)
    @JoinColumn(name = "BRANCH_ID", nullable = false, updatable = false)
    AdBranch adBranch;

    String preCommitId;

    String commitId;

    String commitOperator;

    Integer isUpdate;

    Integer pipelineOperator;

    String jobSchedule;
}
