package com.asiainfo.comm.module.models.ansibleCommand;

import lombok.Data;

/**
 * Created by zhenghp on 2017/2/16.
 */
@Data
public class FtpPutCommand extends AnsibleCommand {
    String fileName;
    String ftpService;
    String ftpUser;
    String ftpPassword;
    String ftpPath;
}
