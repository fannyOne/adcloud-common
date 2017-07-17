package com.asiainfo.comm.module.models;

import com.avaje.ebean.Model;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
@Entity
@Table(name = "AD_BUILD_LOG")
public class AdBuildLog extends Model {
    @SequenceGenerator(name = "id_seq", sequenceName = "AD_BUILD_LOG$SEQ", allocationSize = 0)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_seq")
    @Id
    @Size(max = 12)
    Long buildId;

    Long buildSeq;

    Date createDate;

    Integer state;

    Integer buildResult;

    Integer lastStep;

    Integer totalStep;

    String infoDetail;

    Date buildDate;

    Integer buildType;

    @ManyToOne(optional = false)
    @JoinColumn(name = "OP_ID", unique = true, nullable = false, updatable = false, referencedColumnName = "USER_ID")
    AdUser adUser;

    @ManyToOne(optional = false)
    @JoinColumn(name = "BRANCH_ID", unique = true, nullable = false, updatable = false)
    AdBranch adBranch;

    Long lastStageId;
}
