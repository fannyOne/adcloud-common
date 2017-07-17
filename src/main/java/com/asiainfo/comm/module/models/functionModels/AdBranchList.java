package com.asiainfo.comm.module.models.functionModels;

import com.avaje.ebean.Model;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Size;

@Entity
@Setter
@Getter
@Table(name = "AD_BRANCH")
public class AdBranchList extends Model {
    @Id
    @Size(max = 12)
    Long branchId;

    Integer branchType;

    Integer state;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "TRIGGER_BRANCH", referencedColumnName = "BRANCH_ID", updatable = false)
    AdBranchList triggerBranch;
}
