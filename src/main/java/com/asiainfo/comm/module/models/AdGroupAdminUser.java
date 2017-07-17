package com.asiainfo.comm.module.models;

import com.avaje.ebean.Model;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "AD_GROUP_ADMIN_USER")
public class AdGroupAdminUser extends Model {
    @Id
    @SequenceGenerator(name = "AD_GROUP_ADMIN_USER", sequenceName = "AD_GROUP_ADMIN_USER$SEQ", allocationSize = 0)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "AD_GROUP_ADMIN_USER")
    Long groupAdminUserId;
    Long userId;
    String userName;
    Long opId;
    Integer state;
    Integer userType;
    Date createDate;
    Date updateDate;
    String remark;
    //    Long groupId;
    @ManyToOne(optional = false)
    @JoinColumn(name = "GROUP_ID", updatable = false)
    AdGroup adGroup;
//    @ManyToOne(optional = false,fetch = FetchType.LAZY)
//    @JoinColumn(name = "GROUP_ID", updatable = false)
//    AdGroup adGroup;
}
