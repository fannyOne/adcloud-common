package com.asiainfo.comm.module.models;

import com.avaje.ebean.Model;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;

@Data
@Entity
@Table(name = "AD_BRANCH_DICOSS_RELAT")
public class AdBranchRelat extends Model {
    public static final Finder<Long, AdBranchRelat>
        finder = new Finder<Long, AdBranchRelat>(Long.class, AdBranchRelat.class);

    @Id
    @Size(max = 12)
    Long branchId;

    String dicossAppid;

    Long state;

    String localPath;

    String remotePath;

    String packageName;

    String dicossFilepath;

}
