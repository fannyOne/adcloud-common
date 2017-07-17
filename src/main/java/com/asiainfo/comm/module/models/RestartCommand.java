package com.asiainfo.comm.module.models;

import lombok.Data;

/**
 * Created by zhenghp on 2017/1/24.
 */
@Data
public class RestartCommand extends AnsibleCommand {
    String path;
    String shell;
}
