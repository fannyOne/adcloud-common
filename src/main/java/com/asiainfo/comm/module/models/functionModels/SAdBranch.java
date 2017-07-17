package com.asiainfo.comm.module.models.functionModels;

import com.avaje.ebean.Model;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "AD_BRANCH")
public class SAdBranch extends Model {
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

    Long jenkinsId;

}
