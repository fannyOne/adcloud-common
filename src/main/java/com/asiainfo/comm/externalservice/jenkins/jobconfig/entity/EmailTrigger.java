package com.asiainfo.comm.externalservice.jenkins.jobconfig.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * @version v 1.0 on 2016/7/18 16:02
 * @auther william.xu
 */
public abstract class EmailTrigger {

    private EmailType email;

    protected EmailTrigger() {
        List<RecipientProvider> providers = new ArrayList<>();
        providers.add(new ListRecipientProvider());
        providers.add(new DevelopersRecipientProvider());
        providers.add(new UpstreamComitterRecipientProvider());

        email = new EmailType();
        email.setRecipientProviders(providers);
    }

}
