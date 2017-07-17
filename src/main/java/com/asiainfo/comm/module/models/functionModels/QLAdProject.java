package com.asiainfo.comm.module.models.functionModels;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.Where;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

/**
 * Created by YangRY on 2016/8/30.
 */
@Table(name = "AD_PROJECT")
@Entity
@Getter
@Setter
public class QLAdProject extends Model {
    @Id
    Long projectId;
    String projectName;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "PROJECT_ID", referencedColumnName = "PROJECT_ID", updatable = false)
    @Where(clause = "state=1")
    List<SAdBranch> branchList;
}
