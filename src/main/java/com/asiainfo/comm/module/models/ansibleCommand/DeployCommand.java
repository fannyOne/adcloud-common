package com.asiainfo.comm.module.models.ansibleCommand;

import lombok.Data;


@Data
public class DeployCommand extends AnsibleCommand {
    String sPath;
    String dPath;
    String fileName;
}