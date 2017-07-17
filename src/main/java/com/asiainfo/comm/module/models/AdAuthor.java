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
@Table(name = "AD_AUTHOR")
@Entity
@Data
public class AdAuthor extends Model {
    @Id
    @Size(max = 12)
    @SequenceGenerator(name = "id_author_seq", sequenceName = "AD_AUTHOR$SEQ", allocationSize = 0)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_author_seq")
    Long authorId;

    @ManyToOne
    @JoinColumn(name = "PROJECT_ID", updatable = false)
    AdProject adProject;

    @ManyToOne
    @JoinColumn(name = "ROLE_ID", updatable = false)
    AdRole adRole;

    @Size(max = 2)
    Integer state;

    @Column(columnDefinition = "Date")
    Date createDate;

    @ManyToOne
    @JoinColumn(name = "OP_ID", updatable = false, referencedColumnName = "USER_ID")
    AdUser adUser;

    @Size(max = 255)
    String remark;
}
