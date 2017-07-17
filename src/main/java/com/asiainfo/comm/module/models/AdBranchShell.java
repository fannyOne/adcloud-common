package com.asiainfo.comm.module.models;

import com.avaje.ebean.Model;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Size;

/**
 * Created by weif on 2016/7/13.
 */
@Data
@Entity
@Table(name = "AD_BRANCH_SHELL")
public class AdBranchShell extends Model {

    @SequenceGenerator(name = "id_shell_seq", sequenceName = "AD_BRANCH_SHELL$SEQ", allocationSize = 0)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_shell_seq")
    @Id
    @Size(max = 12)
    Long shellId;

    Long branchId;

    Long pipelineId;

    String shell;


}
