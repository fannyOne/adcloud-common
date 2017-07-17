package com.asiainfo.comm.module.models.functionModels;

import com.asiainfo.comm.module.models.ansibleCommand.AnsibleCommand;
import lombok.Data;

/**
 * Created by zhenghp on 2017/2/7.
 */
@Data
public class GetUrlCommand extends AnsibleCommand {
    String url;
    String urlUser;
    String urlPassword;
    String dest;
}
