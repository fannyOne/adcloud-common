package com.asiainfo.comm.module.models;

import com.avaje.ebean.Model;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by YangRY
 * 2016/7/3 0003.
 */
@Data
@Entity
@Table(name = "AD_RMP_BRANCH_RELATE")
public class AdRmpBranchRelate extends Model {
    String systemClassMa;
    String systemClassBr;
    @ManyToOne(optional = false)
    @JoinColumn(name = "BRANCH_ID", nullable = false, updatable = false)
    AdBranch branch;
    Date createDate;
    //备注
    String remark;
    //状态（0-失效，1-正常）
    Integer state;
    //测试平台系统名称
    String systemNameTest;
}
