package com.asiainfo.comm.module.models;

import com.avaje.ebean.Model;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "AD_PROJECT_DEPLOY_PACKAGE")
public class AdProjectDeployPackage extends Model {
    @Id
    Long packageId;
    @ManyToOne
    @JoinColumn(name = "BRANCH_ID", updatable = false)
    AdBranch adBranch;
    String commitId;
    String packagePath;
    String ext1;
    @Column(columnDefinition = "Date")
    Date createDate;

    String repository;

    Long buildSeqId;
}
