package com.asiainfo.comm.module.models;

import com.avaje.ebean.Model;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * DCOS环境-流水对应表
 * Created by dyn on 2017/3/10.
 */
@Entity
@Data
@Table(name = "AD_DCOS_BRANCH_RELATE")
public class AdDcBranchRelate extends Model {
    @Id
    @Size(max = 11)
    @SequenceGenerator(name = "id_seq", sequenceName = "AD_DCOS_BRANCH_RELATE$SEQ", allocationSize = 0)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_seq")
    Long id;//自然主键

    long dcosId;

    long branchId;

    Long state;//状态；0-失效，1有效

    @Column(columnDefinition = "Date")
    Date createDate;//创建时间
}
