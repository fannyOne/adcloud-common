package com.asiainfo.comm.module.models;

import com.avaje.ebean.Model;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Size;

@Data
@Entity
@Table(name = "AD_DCOS_DEPLOY_INFO")
public class AdDcosDeployInfo extends Model {
    @SequenceGenerator(name = "id_seq", sequenceName = "AD_DCOS_DEPLOY_INFO$SEQ", allocationSize = 0)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_seq")
    @Id
    @Size(max = 12)
    Long deployInfoId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "PROJECT_ID", nullable = false, updatable = false)
    AdProject adProject;

    String packageName;
    String appid;
    String dcossPath;
    @ManyToOne(optional = false)
    @JoinColumn(name = "BRANCH_ID", nullable = false, updatable = false)
    AdBranch adBranch;
    String branchDesc;
    Long state;

    String docsUserName = "devops";
    String docsUserPassword = "Devops_123456";
    String docsServerUrl = "http://20.26.17.137:6062";

    String dcosFtpPath = "Temp/yls/adcloud";
    String dcosFtpUrl = "10.70.41.126";
    String dcosFtpUsername = "joy";
    String dcosFtpPassword = "go2hell";
    String dcosFtpPort = "21";

    Integer region;     //所属域，1代表生产域，2代表测试域
    String envName;     //环境名称
}
