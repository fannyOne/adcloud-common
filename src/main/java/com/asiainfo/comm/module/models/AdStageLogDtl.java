package com.asiainfo.comm.module.models;


import com.avaje.ebean.Model;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "AD_STAGE_LOG_DTL")
public class AdStageLogDtl extends Model {
    public static final Finder<Long, AdStageLogDtl> finder = new Finder<Long, AdStageLogDtl>(Long.class, AdStageLogDtl.class);
    @SequenceGenerator(name = "id_seq", sequenceName = "AD_STAGE_LOG_DTL$SEQ", allocationSize = 0)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_seq")
    @Id
    Long logId;

    Long buildSeqId;

    Long step;

    Long totalStep;

    Date beginDate;

    Date finishDate;

    String stageResult;

    String relatUserId;

    @Lob
    String failLog;

    Long state;

    @ManyToOne(optional = false)
    @JoinColumn(name = "BRANCH_ID", nullable = false, updatable = false)
    AdBranch adBranch;
    @ManyToOne(optional = false)
    @JoinColumn(name = "OP_ID", nullable = false, updatable = false, referencedColumnName = "USER_ID")
    AdUser adUser;
    @ManyToOne(optional = false)
    @JoinColumn(name = "STAGE_ID", nullable = false, updatable = false)
    AdStage adStage;

    String commitId;

}
