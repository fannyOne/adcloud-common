package com.asiainfo.comm.module.models;

import com.avaje.ebean.Model;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * Created by YangRY
 * 2016/7/11 0011.
 */
@Table(name = "AD_ROLE")
@Entity
@Data
public class AdRole extends Model {
    @Id
    @Size(max = 12)
    @SequenceGenerator(name = "id_role_seq", sequenceName = "AD_ROLE$SEQ", allocationSize = 0)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_role_seq")
    Long roleId;

    @Size(max = 255)
    String roleName;

    @Size(max = 2)
    Integer state;

    @Column(columnDefinition = "Date")
    Date createDate;

    @ManyToOne
    @JoinColumn(name = "OP_ID", nullable = false, updatable = false, referencedColumnName = "USER_ID")
    AdUser adUser;

    @Size(max = 255)
    String remark;

    @Size(max = 5)
    Integer roleLevel;
}
