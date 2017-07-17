package com.asiainfo.comm.module.models;

import com.avaje.ebean.Model;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@Table(name = "AD_USER")
public class AdUser extends Model {

    @Id
    @SequenceGenerator(name = "AD_USER_SEQ", sequenceName = "AD_USER_SEQ", allocationSize = 0)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "AD_USER_SEQ")

    Long userId;

    String loginName;

    String password;

    String displayName;

    String gender;

    String email;

    String qqNumber;

    String phoneNumber;

    Date createDate;

    Date lastLoginDate;

    Long state;

    Long createUser;

    Long opId;

    Long authorId;

    String sessionId;

    Integer firstLogin;

    Date activeDate;

    String login4aName;

    String notification;

    String verificationCode;

    Date verificationTime;
}
