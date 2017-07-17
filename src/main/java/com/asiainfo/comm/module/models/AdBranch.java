package com.asiainfo.comm.module.models;

import com.asiainfo.util.DateConvertUtils;
import com.avaje.ebean.Model;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
@Getter
@Setter
@ToString
@Entity
@Table(name = "AD_BRANCH")
@EqualsAndHashCode(exclude = "adPipeLineStates", callSuper = false)
public class AdBranch extends Model {
    public static final Finder<Long, AdBranch> finder = new Finder<Long, AdBranch>(Long.class, AdBranch.class);
    @Id
    @Size(max = 12)
    Long branchId;

    String branchName;

    String branchDesc;

    String jenkinsToken;

    Long codeStoreId;

    String appMachineInfo;

    String appUrl;

    Date doneDate;

    Date expireDate;

    Long state;

    Long resourceState;

    Date lastDate;

    Integer branchType;

    String dockerCommand;

    String branchPath;

    Long version;

    String originPath;

    String buildFileType;

    Long triggerBranch;

    String buildCron; //定时构建的Cron表达式

    @ManyToOne(optional = false)
    @JoinColumn(name = "JENKINS_ID", nullable = false, updatable = false)
    AdJenkinsInfo adJenkinsInfo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "PROJECT_ID", nullable = false, updatable = false)
    AdProject adProject;

    String envType; //环境类型

    Long envId; //环境ID


    public String getDoneDateString() {
        return DateConvertUtils.format();
    }

}
