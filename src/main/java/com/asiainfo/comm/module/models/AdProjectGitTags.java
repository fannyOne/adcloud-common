package com.asiainfo.comm.module.models;

import com.avaje.ebean.Model;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "AD_PROJECT_GIT_TAGS")
public class AdProjectGitTags extends Model {
    @Id
    Long proTagId;
    Long projectId;
    Date createDate;
    String tagName;
    String ext1;
    @ManyToOne(optional = false)
    @JoinColumn(name = "BRANCH_ID", updatable = false)
    AdBranch adBranch;
    String version;
    String downPath;
    String commitId;
    Integer branchType;
}
