package com.asiainfo.comm.externalservice.jenkins.client;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ShellCommand {

    private static final Log LOG = LogFactory.getLog(ShellCommand.class);

    public static void main(String[] args) throws Exception {
        // String host = "20.26.20.106";
        // String username = "aideploy";
        // String password = "aideploy";
        // String cmd;
        // String jobname = "ddzx_acctcenter_dev_step3";
        // String maxnum = "24";
        //
        // cmd = "cd jenkins/jobs/" + jobname + "/builds/" + maxnum
        // + " \n cat log";

        // runSSH(host,username,password,cmd);
    }

    public static Connection getOpenedConnection(String host, String username,
                                                 String password) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("connecting to " + host + " with user " + username
                + " and pwd " + password);
        }

        Connection conn = new Connection(host);
        conn.connect(); // make sure the connection is opened
        boolean isAuthenticated = conn.authenticateWithPassword(username,
            password);
        if (isAuthenticated == false)
            throw new IOException("Authentication failed.");
        return conn;
    }

    public static void scpGet(String host, String username, String password,
                              String remoteFile, String localDir) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("spc [" + remoteFile + "] from " + host + " to "
                + localDir);
        }
        Connection conn = getOpenedConnection(host, username, password);
        SCPClient client = new SCPClient(conn);
        client.get(remoteFile);
        conn.close();
    }

    public static StringBuffer runSSH(String host, String username,
                                      String password, String cmd) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("running SSH cmd [" + cmd + "]");
        }
        StringBuffer bf = new StringBuffer();
        Connection conn = getOpenedConnection(host, username, password);
        Session sess = conn.openSession();
        sess.execCommand(cmd);
        InputStream stdout = new StreamGobbler(sess.getStdout());
        InputStreamReader isr = new InputStreamReader(stdout, "gb2312");
        BufferedReader br = new BufferedReader(isr);
        String name = "";
        String buf = null;
        while ((buf = br.readLine()) != null) {
            bf.append(new String(buf.getBytes()) + "    \r\n");
            // bf.append(new
            // String(buf.getBytes("gbk"),"UTF-8")+"    <br>\r\n");
            bf.append(name);
        }
        sess.close();
        conn.close();
        if (br != null) {
            br.close();
        }
        return bf;
    }

}
