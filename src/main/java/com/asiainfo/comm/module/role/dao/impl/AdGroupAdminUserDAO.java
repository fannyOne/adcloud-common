package com.asiainfo.comm.module.role.dao.impl;

import com.asiainfo.comm.module.models.AdGroupAdminUser;
import com.asiainfo.comm.module.models.query.QAdGroupAdminUser;
import com.avaje.ebean.Ebean;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * Created by zhenghp on 2016/12/20.
 */
@Component
public class AdGroupAdminUserDAO {

    public List<AdGroupAdminUser> qryByGroupId(long groupId) {
        return new QAdGroupAdminUser().state.eq(1).adGroup.groupId.eq(groupId).findList();
    }

    public List<AdGroupAdminUser> qryByGroupIdAndUsername(long groupId, String userName) {
        return new QAdGroupAdminUser().state.eq(1).userName.eq(userName).adGroup.groupId.eq(groupId).findList();
    }

    public List<AdGroupAdminUser> qryByUsername(String userName) {
        return new QAdGroupAdminUser().state.eq(1).adGroup.state.eq(1).userName.eq(userName).findList();
    }

    public void save(AdGroupAdminUser adGroupAdminUser) {
        adGroupAdminUser.setState(1);
        adGroupAdminUser.setCreateDate(new Date());
        adGroupAdminUser.setUpdateDate(new Date());
        Ebean.save(adGroupAdminUser);
    }

    public void del(AdGroupAdminUser adGroupAdminUser) {
        adGroupAdminUser.setState(0);
        adGroupAdminUser.setUpdateDate(new Date());
        Ebean.save(adGroupAdminUser);
    }

    public List<AdGroupAdminUser> qryByUserName(String userName) {
        return new QAdGroupAdminUser().state.eq(1).userName.eq(userName).findList();
    }

    public void del(String userName) {
        List<AdGroupAdminUser> infos = qryByUserName(userName);
        if (CollectionUtils.isNotEmpty(infos)) {
            for (AdGroupAdminUser info : infos) {
                del(info);
            }
        }
    }
}
