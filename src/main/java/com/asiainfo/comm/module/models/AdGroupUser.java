package com.asiainfo.comm.module.models;

import com.avaje.ebean.Model;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
@Entity
@Table(name = "AD_GROUP_USER")
public class AdGroupUser extends Model {
    @SequenceGenerator(name = "AD_GROUP_USER", sequenceName = "AD_GROUP_USER$SEQ", allocationSize = 0)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "AD_GROUP_USER")
    @Id
    @Size(max = 12)
    Long groupUserId;
    Long groupId;
    Long userId;
    String userName;
    Long opId;
    Long doneCode;
    Integer state;
    String userType;
    Date createDate;
    Date updateDate;
    String remark;
    Date ext1;
    String ext2;

}
