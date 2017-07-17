package com.asiainfo.comm.module.models;

import com.avaje.ebean.Model;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * Created by YangRY
 * 2016/7/12 0012.
 */
@Entity
@Table(name = "AD_USER_ROLE_REL")
@Data
public class AdUserRoleRel extends Model {
    @Id
    @Size(max = 12)
    @SequenceGenerator(name = "id_user_role_seq", sequenceName = "AD_USER_ROLE_REL$SEQ", allocationSize = 0)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_user_role_seq")
    Long relId;

    String userName;

    @ManyToOne
    @JoinColumn(name = "ROLE_ID", updatable = true)
    AdRole adRole;

    Integer state;

    Date createDate;

    String opUser;
}
