package com.asiainfo.comm.module.models;

import com.avaje.ebean.Model;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * Created by guojian on 7/26/16.
 */
@Table(name = "AD_SYSTEMDEPLOY_LOG")
@Entity
@Data
public class AdSystemDeployLog extends Model {
    @SequenceGenerator(name = "AD_SYSTEMDEPLOY_LOG_SEQ", sequenceName = "AD_SYSTEMDEPLOY_LOG_SEQ", allocationSize = 0)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "AD_SYSTEMDEPLOY_LOG_SEQ")
    @Id
    @Size(max = 12)
    Long logId;
    Long projectId;
    @Column(columnDefinition = "Date")
    Date startTime;
    @Column(columnDefinition = "Date")
    Date endTime;
    String deployType;
    String ip;
    @Column(columnDefinition = "CLOB")
    String deployComment;
    Integer deployResult;
    @ManyToOne(optional = false)
    @JoinColumn(name = "OP_ID", nullable = false, updatable = false, referencedColumnName = "USER_ID")
    AdUser adUser;
    String deployVersion;
    String jobToken;
    @Column(columnDefinition = "Date")
    Date runTime;
    Integer hostType;
    String remark;
    @Column(columnDefinition = "Date")
    Date createDate;
    @Column(columnDefinition = "Date")
    Date modifyDate;
    @JoinColumn(name = "OP_ID", nullable = false, referencedColumnName = "USER_ID")
    AdUser modifyUser;
    Long envId;
    Long proTagId;
    //    @ManyToOne(optional = false)
//    @JoinColumn(name = "PRO_TAG_ID", nullable = false)
//    AdProjectGitTags projectGitTags;
    String appId;
    Integer planState;
    Integer operType;
    @Column(columnDefinition = "Date")
    Date failedTime;
    Long branchId;
}
