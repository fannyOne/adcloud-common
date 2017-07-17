package com.asiainfo.comm.module.models;

import com.avaje.ebean.Model;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
@Entity
@Table(name = "AD_FASTEN_SIGN")
public class AdFastenSign extends Model {
    @ManyToOne(optional = false)
    @JoinColumn(name = "BRANCH_ID", nullable = false, updatable = false)
    AdBranch adBranch;
    @ManyToOne(optional = false)
    @JoinColumn(name = "PROJECT_ID", nullable = false, updatable = false)
    AdProject adProject;
    @ManyToOne(optional = false)
    @JoinColumn(name = "GROUP_ID", updatable = false)
    AdGroup adGroup;
    @SequenceGenerator(name = "id_seq", sequenceName = "AD_FASTEN_SIGN$SEQ", allocationSize = 0)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_seq")
    @Id
    @Size(max = 12)
    private Long signId;
    @ManyToOne(optional = false)
    @JoinColumn(name = "USER_ID", nullable = false, updatable = false)
    private AdUser adUser;
    private String signName;
    private String signParam;
    private Integer signType;
    private Integer state;
    @Column(columnDefinition = "Date")
    private Date createDate;
    @Column(columnDefinition = "Date")
    private Date modifyDate;

}
