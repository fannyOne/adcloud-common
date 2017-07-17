package com.asiainfo.comm.module.models.ansibleCommand;

import lombok.Data;

/**
 * Created by zhenghp on 2017/2/17.
 */
@Data
public class FileCommand extends AnsibleCommand {
    String dest;
    String state;
}
