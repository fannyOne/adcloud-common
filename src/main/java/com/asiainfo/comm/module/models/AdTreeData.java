package com.asiainfo.comm.module.models;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.Where;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by YangRY on 2016/11/9.
 */
@Entity
@Getter
@Setter
@Table(name = "AD_TREE_DATA")
public class AdTreeData extends Model {
    @Id
    @SequenceGenerator(name = "AD_TREE_DATA$SEQ", sequenceName = "AD_TREE_DATA$SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "AD_TREE_DATA$SEQ")
    Long id;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "PARENT_ID", referencedColumnName = "ID", updatable = false)
    @Where(clause = "state=1")
    List<AdTreeData> adTreeDataList;
    Long parentId;
    Integer treeType;
    String treeCode;
    String treePara;
    String treeName;
    String treeDesc;
    Integer state;
    @Column(columnDefinition = "DATE")
    Date createDate;
    @Column(columnDefinition = "DATE")
    Date doneDate;
    @ManyToOne
    @JoinColumn(name = "OP_ID", referencedColumnName = "USER_ID", nullable = false, updatable = false)
    AdUser adUser;
    Long numberPara;
    String stringPara;

    public AdTreeData() {
        Date date = new Date();
        createDate = date;
        doneDate = date;
        state = 1;
    }
}
