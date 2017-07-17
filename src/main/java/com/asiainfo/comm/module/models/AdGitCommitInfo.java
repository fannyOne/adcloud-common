package com.asiainfo.comm.module.models;

import com.avaje.ebean.Model;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "AD_GIT_COMMIT_INFO")
public class AdGitCommitInfo extends Model {
    @Id
    String beforecode;
    String userName;
    String userEmail;
    Date commitDate;
    @ManyToOne(optional = false)
    @JoinColumn(name = "BRANCH_ID", unique = true, nullable = false, updatable = false)
    AdBranch adBranch;
    String branchType;

}
