package com.asiainfo.comm.module.models.functionModels;

import com.avaje.ebean.Model;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * Created by YangRY
 * 2016/7/11 0011.
 */
@Table(name = "AD_GROUP_USER")
@Entity
@Getter
@Setter
public class QLAdGroupUser extends Model {
    @SequenceGenerator(name = "AD_GROUP_USER", sequenceName = "AD_GROUP_USER$SEQ", allocationSize = 0)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "AD_GROUP_USER")
    @Id
    @Size(max = 12)
    Long groupUserId;
    @ManyToOne(optional = false)
    @JoinColumn(name = "GROUP_ID", unique = true, nullable = false, updatable = false)
    QLAdGroup qlAdGroup;
    Long userId;
    String userName;
    Long opId;
    Long doneCode;
    Integer state;
    Integer userType;
    Date createDate;
    Date updateDate;
    String remark;
    Date ext1;
    String ext2;
}
