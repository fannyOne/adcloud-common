package com.asiainfo.comm.module.models.functionModels;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.Where;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

@Entity
@Setter
@Getter
@Table(name = "AD_GROUP")
public class AdGroupAndProject extends Model {

    @SequenceGenerator(name = "id_seq", sequenceName = "AD_GROUP$SEQ", allocationSize = 0)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_seq")
    @Id
    @Size(max = 12)
    Long groupId;

    String groupName;

    Integer groupType;

    String groupDesc;

    @Column(columnDefinition = "Date")
    Date createDate;

    @Column(columnDefinition = "Date")
    Date doneDate;

    @Column(columnDefinition = "Date")
    Date expireDate;

    Integer state;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "GROUP_ID", referencedColumnName = "GROUP_ID", updatable = false)
    @Where(clause = "state=1")
    List<AdProject> adProjects;

    String opUser;
}
