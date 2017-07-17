package com.asiainfo.util;

import com.asiainfo.comm.module.models.ansibleCommand.*;
import com.asiainfo.comm.module.models.functionModels.GetUrlCommand;
import org.apache.commons.lang3.StringUtils;

@lombok.extern.slf4j.Slf4j
public class AnsibleCommandUtils {
    private final static String separator = "/";
    private final static String comma = "\"";

    private final static String invertedComma = "\'";

    public static String pingCommand(String hostIp) {
        StringBuffer commandShell = new StringBuffer();
        commandShell.append("ansible ");
        commandShell.append(hostIp);
        commandShell.append(" -m ping");
        return commandShell.toString();
    }

    public static String deployCommand(DeployCommand command) {
        StringBuffer commandShell = new StringBuffer();
        commandShell.append("ansible ");
        commandShell.append(command.getHostIp());
        commandShell.append(" -m ").append("copy");
        commandShell.append(" -a ").append(comma + "src=").append(command.getSPath());
        if (!StringUtils.endsWith(commandShell.toString(), separator)) {
            commandShell.append(separator);
        }
        commandShell.append(command.getFileName());
        commandShell.append(" dest=").append(command.getDPath());
        commandShell.append(comma);
        return commandShell.toString();
    }

    public static String restartCommand(RestartCommand command) {
        StringBuffer commandShell = new StringBuffer();
        commandShell.append("ansible ");
        commandShell.append(command.getHostIp());
        commandShell.append(" -m ").append("shell");
        commandShell.append(" -a ");
        commandShell.append(comma);
        commandShell.append(" sh ");
        commandShell.append(command.getShell());
        if (StringUtils.isNotEmpty(command.getPath())) {
            commandShell.append(" chdir=");
            commandShell.append(command.getPath());
        }
        commandShell.append(comma);
        return commandShell.toString();
    }

    public static String shellCommand(ShellCommand command) {
        StringBuffer commandShell = new StringBuffer();
        commandShell.append("ansible ");
        commandShell.append(command.getHostIp());
        commandShell.append(" -m ").append("shell");
        commandShell.append(" -a ");
        commandShell.append(comma);
        commandShell.append(" sh ");
        commandShell.append(command.getShell());
        if (StringUtils.isNotEmpty(command.getVars())) {
            commandShell.append(" ").append(command.getVars());
        }
        if (StringUtils.isNotEmpty(command.getPath())) {
            commandShell.append(" chdir=");
            commandShell.append(command.getPath());
        }
        commandShell.append(comma);
        return commandShell.toString();
    }

    public static String getUrlCommand(GetUrlCommand command) {
        StringBuffer commandShell = new StringBuffer();
        commandShell.append("ansible ");
        commandShell.append(command.getHostIp());
        commandShell.append(" -m ").append("get_url");
        commandShell.append(" -a ");
        commandShell.append(comma);
        commandShell.append("url=");
        commandShell.append(command.getUrl());
        commandShell.append(" dest=");
        commandShell.append(command.getDest());
        if (StringUtils.isNotEmpty(command.getUrlUser())) {
            commandShell.append(" url_username=").append(command.getUrlUser());
        }
        if (StringUtils.isNotEmpty(command.getUrlPassword())) {
            commandShell.append(" url_password=").append(command.getUrlPassword());
        }
        commandShell.append(comma);
        return commandShell.toString();
    }

    public static String ftpPutCommand(FtpPutCommand command) {
        StringBuffer commandShell = new StringBuffer();
        commandShell.append("ansible ");
        commandShell.append(command.getHostIp());
        commandShell.append(" -m shell -a ");
        commandShell.append(comma);
        commandShell.append("lftp -e ");
        commandShell.append(invertedComma);
        if (StringUtils.isNotEmpty(command.getFtpPath())) {
            commandShell.append("cd " + command.getFtpPath() + ";");
        }
        commandShell.append("mput " + command.getFileName() + ";exit");
        commandShell.append(invertedComma);
        commandShell.append(" -u ");
        commandShell.append(command.getFtpUser() + "," + command.getFtpPassword());
        commandShell.append(" " + command.getFtpService());
        commandShell.append(comma);
        return commandShell.toString();
    }

    public static String fileCommand(FileCommand command) {
        StringBuffer commandShell = new StringBuffer();
        commandShell.append("ansible ");
        commandShell.append(command.getHostIp());
        commandShell.append(" -m file -a");
        commandShell.append(comma);
        commandShell.append("dest=").append(command.getDest());
        commandShell.append(" state=").append(command.getState());
        commandShell.append(comma);
        return commandShell.toString();
    }

    public static String exec(SshUtil sshUtil, String cmd) {
        String st = "";
        try {
            log.error(cmd);
            st = sshUtil.exec_noResult(cmd);
            log.error(st);
        } catch (Exception e1) {
            st = e1.getMessage();
            log.error(e1.getMessage(), e1);
        }
        return st;
    }

}
