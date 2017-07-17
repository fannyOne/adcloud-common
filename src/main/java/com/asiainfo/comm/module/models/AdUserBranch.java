package com.asiainfo.comm.module.models;

import com.avaje.ebean.Model;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "AD_USER_BRANCH")
public class AdUserBranch extends Model {
    @Id
    @SequenceGenerator(name = "AD_USER_BRANCH_SEQ", sequenceName = "AD_USER_BRANCH$SEQ", allocationSize = 0)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "AD_USER_BRANCH_SEQ")
    Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "USER_ID", nullable = false, updatable = false)
    AdUser adUser;
    String userName;
    @ManyToOne(optional = false)
    @JoinColumn(name = "BRANCH_ID", nullable = false, updatable = false)
    AdBranch adBranch;
    Integer state;
    @Column(columnDefinition = "DATE")
    Date createDate;
    @Column(columnDefinition = "DATE")
    Date doneDate;
}
