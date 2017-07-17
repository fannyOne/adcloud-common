package com.asiainfo.comm.module.models.functionModels;

import com.avaje.ebean.Model;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by YangRY on 2016/11/9.
 */
@Entity
@Data
@Table(name = "AD_USER_DATA_RELATE")
public class AdUserDataRelateSimple extends Model {
    @Id
    @SequenceGenerator(name = "AD_USER_DATA_RELATE$SEQ", sequenceName = "AD_USER_DATA_RELATE$SEQ", allocationSize = 0)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "AD_USER_DATA_RELATE$SEQ")
    Long id;
    Long userId;
    Long foreignId;
    Integer dataType;
    Integer state;
    @Column(columnDefinition = "DATE")
    Date createDate;
    @Column(columnDefinition = "DATE")
    Date doneDate;
    Integer reportType;

    public AdUserDataRelateSimple() {
        Date date = new Date();
        createDate = date;
        doneDate = date;
        state = 1;
    }
}
