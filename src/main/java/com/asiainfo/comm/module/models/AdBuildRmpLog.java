package com.asiainfo.comm.module.models;

import com.avaje.ebean.Model;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "AD_BUILD_RMP_LOG")
public class AdBuildRmpLog extends Model {
    @Id
    @SequenceGenerator(name = "id_seq", sequenceName = "AD_BUILD_RMP_LOG$SEQ", allocationSize = 0)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_seq")
    Long id;
    String tbCode;
    Integer tbType;
    Long buildSeq;
    @Column(columnDefinition = "DATE")
    Date createDate;
    Integer state;
    String commitId;
    @ManyToOne(optional = false)
    @JoinColumn(name = "BRANCH_ID", unique = true, nullable = false, updatable = false)
    AdBranch adBranch;

}
