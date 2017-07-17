package com.asiainfo.comm.module.models;

import com.avaje.ebean.Model;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * Created by YangRY
 * 2016/7/18 0018.
 */
@Data
@Entity
@Table(name = "AD_SEQ_TEST_RELATE")
public class AdSeqTestRelate extends Model {
    @SequenceGenerator(name = "id_seq", sequenceName = "AD_SEQ_TEST_RELATE$SEQ", allocationSize = 0)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_seq")
    @Id
    @Size(max = 12)
    Long relateId;
    Date createDate;
    String opId;
    Integer state;
    String remarks;
    Long seqId;
    Long testId;
    @ManyToOne
    @JoinColumn(name = "STAGE_ID")
    AdStage adStage;
    String testType;
    Long totalNum;
}
