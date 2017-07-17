package com.asiainfo.comm.module.models;

import lombok.Data;

/**
 * Created by zhenghp on 2017/2/7.
 */
@Data
public class ShellCommand extends AnsibleCommand {
    String path;
    String shell;
    String vars;
}
