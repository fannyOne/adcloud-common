package com.asiainfo.comm.module.models;

import com.avaje.ebean.Model;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Max;
import java.util.Date;

/**
 * Created by YangRY
 * 2016/7/5 0005.
 */
@Entity
@Data
@Table(name = "AD_AUTO_TEST_LOG")
public class AdAutoTestLog extends Model {
    @Id
    @Max(12)
    @SequenceGenerator(name = "ad_auto_test_log_seq", sequenceName = "AD_AUTO_TEST_LOG$SEQ", allocationSize = 0)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ad_auto_test_log_seq")
    Long logId;
    Long seqId;
    Long totalNum;
    Long sucNum;
    Long failNum;
    String sucPre;
    Integer autoType;
    @Temporal(TemporalType.DATE)
    Date createDate;
    Long opId;
    Integer state;
    String testLog;
    Long testId;
    @Temporal(TemporalType.DATE)
    Date beginDate;
    @Temporal(TemporalType.DATE)
    Date endDate;
    @ManyToOne(optional = false)
    @JoinColumn(name = "BRANCH_ID", nullable = false, updatable = false)
    AdBranch adBranch;
    @ManyToOne(optional = false)
    @JoinColumn(name = "STAGE_ID", nullable = false, updatable = false)
    AdStage adStage;
}
