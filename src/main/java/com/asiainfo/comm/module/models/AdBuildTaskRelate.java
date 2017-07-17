package com.asiainfo.comm.module.models;

import com.avaje.ebean.Model;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Max;
import java.util.Date;

/**
 * Created by YangRY
 * 2016/7/5 0005.
 */
@Entity
@Data
@Table(name = "AD_BUILD_TASK_RELATE")
public class AdBuildTaskRelate extends Model {
    @Id
    @Max(12)
    @SequenceGenerator(name = "AD_BUILD_TASK_RELATE_SEQ", sequenceName = "AD_BUILD_TASK_RELATE$SEQ", allocationSize = 0)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "AD_BUILD_TASK_RELATE_SEQ")
    Long id;
    String tbCode;
    Integer tbType;
    Long buildSeq;
    Date createDate;
    Integer state;
    @Column(columnDefinition = "CLOB")
    String codeList;

}
