package com.asiainfo.comm.module.models;

import com.avaje.ebean.Model;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by YangRY on 2016/11/9.
 */
@Entity
@Getter
@Setter
@Table(name = "AD_USER_DATA_RELATE")
public class AdUserTreeRelate extends Model {
    @Id
    @SequenceGenerator(name = "AD_USER_DATA_RELATE$SEQ", sequenceName = "AD_USER_DATA_RELATE$SEQ", allocationSize = 0)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "AD_USER_DATA_RELATE$SEQ")
    Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "USER_ID", nullable = false, updatable = false, referencedColumnName = "USER_ID")
    AdUser adUser;
    @ManyToOne(optional = false)
    @JoinColumn(name = "FOREIGN_ID", referencedColumnName = "ID", nullable = false, updatable = false)
    AdTreeData adTreeData;
    Long foreignId;
    Integer dataType;
    Integer state;
    @Column(columnDefinition = "DATE")
    Date createDate;
    @Column(columnDefinition = "DATE")
    Date doneDate;
    Integer reportType;
}
