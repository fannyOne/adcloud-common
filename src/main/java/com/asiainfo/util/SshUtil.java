package com.asiainfo.util;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by weif on 2016/7/6.
 */
@lombok.extern.slf4j.Slf4j
public class SshUtil {

    private Connection conn;
    private String ipAddr;
    private String charset;
    private String userName;
    private String password;

    public SshUtil(String ipAddr, String userName, String password,
                   String charset) {
        this.ipAddr = ipAddr;
        this.userName = userName;
        this.password = password;
        if (charset != null) {
            this.charset = charset;
        }
    }

    public boolean login() throws IOException {
        conn = new Connection(ipAddr);
        conn.connect(); // 连接
        return conn.authenticateWithPassword(userName, password); // 认证
    }

    public String exec(String cmds) {
        InputStream in;
        String result = "";
        Session session = null;
        try {
            if (login()) {
                session = conn.openSession(); // 打开一个会话
                session.execCommand(cmds);
                in = session.getStdout();
                result = processStdoutReplace(in, this.charset);
            } else {
                return "login failed";
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return result;
    }


    public String processStdoutReplace(InputStream in, String charset) {
        byte[] buf = new byte[1];
        StringBuffer sb = new StringBuffer();
        try {
            while (in.read(buf) != -1) {
                sb.append(new String(buf, charset));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public String processStdoutReplaceNoResult(InputStream in) {
        StringBuffer sb = new StringBuffer();
        try {
            InputStreamReader isr = new InputStreamReader(in, "gb2312");
            BufferedReader br = new BufferedReader(isr);
            String name = "";
            String buf = null;
            long beinTime = System.currentTimeMillis();
            long endTime;
            while ((buf = br.readLine()) != null) {
                sb.append(new String(buf.getBytes()) + "    \r\n");
                sb.append(name);
                endTime = System.currentTimeMillis();
                if ((endTime - beinTime) > 40000) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            log.error("ssh 执行+(endTime-beinTime)>40000");
        }
        return sb.toString();
    }

    public String exec_noResult(String cmds) throws Exception {
        InputStream in;
        String result = "";
        Session session = null;
        try {
            if (login()) {
                session = conn.openSession(); // 打开一个会话
                session.execCommand(cmds);
                log.error("state:" + session.getState());
                in = session.getStdout();
                result = processStdoutReplaceNoResult(in);
            } else {
                return "login failed";
            }
        } catch (IOException e1) {
            throw e1;
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return result;
    }
}
