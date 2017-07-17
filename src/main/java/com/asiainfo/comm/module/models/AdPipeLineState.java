package com.asiainfo.comm.module.models;

import com.avaje.ebean.Model;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
@Entity
@Table(name = "AD_PIPELINE_STATE")
public class AdPipeLineState extends Model {
    @Id
    @Size(max = 12)
    Long pipelineId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "PROJECT_ID", nullable = false, updatable = false)
    AdProject adProject;

    Integer branchState;

    Long currentStageId;

//    @ManyToOne(optional = false)
//    @JoinColumn(name = "CURRENT_OP_ID",nullable = false, updatable = false,referencedColumnName = "USER_ID")
//    AdUser adUser;

    Long buildSeqId;

    Date lastBuildDate;

    Integer lastBuildResult;

    Integer buildType;

    Integer state;

    @ManyToOne(optional = false)
    @JoinColumn(name = "BRANCH_ID", nullable = false, updatable = false)
    AdBranch adBranch;
    Long stopState;
}
