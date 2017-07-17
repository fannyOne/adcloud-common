package com.asiainfo.util;

import com.jcraft.jsch.*;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Vector;

/**
 * SFTP帮助类
 *
 * @author wangbailin
 */
public class SFtpUtil {

    private static Log log = LogFactory.getLog(SFtpUtil.class);

    /**
     * 连接sftp服务器
     *
     * @param host     远程主机ip地址
     * @param port     sftp连接端口，null 时为默认端口
     * @param user     用户名
     * @param password 密码
     * @return
     * @throws JSchException
     */
    public static Session connect(String host, Integer port, String user,
                                  String password) throws JSchException {
        Session session = null;
        try {
            JSch jsch = new JSch();
            if (port != null) {
                session = jsch.getSession(user, host, port.intValue());
            } else {
                session = jsch.getSession(user, host);
            }
            session.setPassword(password);
            // 设置第一次登陆的时候提示，可选值:(ask | yes | no)
            session.setConfig("StrictHostKeyChecking", "no");
            // 30秒连接超时
            session.connect(30000);
        } catch (JSchException e) {
            e.printStackTrace();
            System.out.println("SFTPUitl 获取连接发生错误");
            throw e;
        }
        return session;
    }

    /**
     * sftp上传文件(夹)
     *
     * @param directory
     * @param uploadFile
     * @param sftp
     * @throws Exception
     */
    public static void upload(String directory, String uploadFile,
                              ChannelSftp sftp) throws Exception {
        /*
         * System.out.println("sftp upload file [directory] : "+directory);
		 * System.out.println("sftp upload file [uploadFile] : "+ uploadFile);
		 */
        File file = new File(uploadFile);
        if (file.exists()) {
            // 这里有点投机取巧，因为ChannelSftp无法去判读远程linux主机的文件路径,无奈之举
            try {
                @SuppressWarnings("rawtypes")
                Vector content = sftp.ls(directory);
                if (content == null) {
                    sftp.mkdir(directory);
                }
            } catch (SftpException e) {
                sftp.mkdir(directory);
            }
            // 进入目标路径
            sftp.cd(directory);
            if (file.isFile()) {
                InputStream ins = null;
                try {
                    ins = new FileInputStream(file);
                    // 中文名称的
                    sftp.put(ins, new String(file.getName().getBytes(), "UTF-8"));
                } catch (Exception e) {
                    throw e;
                } finally {
                    if (ins != null) {
                        ins.close();
                    }
                }
                // sftp.setFilenameEncoding("UTF-8");
            } else {
                File[] files = file.listFiles();
                if (files != null) {
                    StringBuilder sb = new StringBuilder(directory);
                    for (File file2 : files) {
                        String dir = file2.getAbsolutePath();
                        if (file2.isDirectory()) {
                            String str = dir.substring(dir
                                .lastIndexOf(File.separator));
                            // directory = FileUtil.normalize(directory + str);
                            sb.append(str);
                        }
                        upload(sb.toString(), dir, sftp);
                    }
                }
            }
        }
    }

    /**
     * sftp上传文件(夹)
     *
     * @param directory
     * @param sftp
     * @throws Exception
     */
    public static void upload(String directory, File file, ChannelSftp sftp)
        throws Exception {
        if (file.exists()) {
            // 这里有点投机取巧，因为ChannelSftp无法去判读远程linux主机的文件路径,无奈之举
            try {
                @SuppressWarnings("rawtypes")
                Vector content = sftp.ls(directory);
                if (content == null) {
                    sftp.mkdir(directory);
                }
            } catch (SftpException e) {
                sftp.mkdir(directory);
            }
            // 进入目标路径
            sftp.cd(directory);
            if (file.isFile()) {
                try {
                    sftp.rm(file.getName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                InputStream ins = null;
                try {
                    ins = new FileInputStream(file);
                    // 中文名称的
                    sftp.put(ins, new String(file.getName().getBytes(), "UTF-8"));
                } catch (Exception e) {
                    throw e;
                } finally {
                    if (ins != null) {
                        ins.close();
                    }
                }
                // sftp.setFilenameEncoding("UTF-8");
            } else {
                File[] files = file.listFiles();
                if (files != null) {
                    StringBuilder sb = new StringBuilder(directory);
                    for (File file2 : files) {
                        String dir = file2.getAbsolutePath();
                        if (file2.isDirectory()) {
                            String str = dir.substring(dir
                                .lastIndexOf(File.separator));
                            // directory = FileUtil.normalize(directory + str);
                            sb.append(str);
                        }
                        upload(sb.toString(), file2, sftp);
                    }
                }
            }
        }
    }

    /**
     * sftp下载文件（夹）
     *
     * @param directory 下载文件上级目录
     * @param srcFile   下载文件完全路径
     * @param saveFile  保存文件路径
     * @param sftp      ChannelSftp
     * @throws UnsupportedEncodingException
     */
    @SuppressWarnings("rawtypes")
    public static void download(String directory, String srcFile,
                                String saveFile, ChannelSftp sftp)
        throws UnsupportedEncodingException {
        Vector conts = null;
        try {
            conts = sftp.ls(srcFile);
        } catch (SftpException e) {
            e.printStackTrace();
            log.debug("ChannelSftp sftp罗列文件发生错误", e);
        }
        File file = new File(saveFile);
        if (!file.exists()) {
            if (!file.mkdir()) {
                log.error("Make dir failed");
            }
        }
        // 文件
        if (srcFile.indexOf(".") > -1) {
            try {
                sftp.get(srcFile, saveFile);
            } catch (SftpException e) {
                e.printStackTrace();
                log.debug("ChannelSftp sftp下载文件发生错误", e);
            }
        } else {
            // 文件夹(路径)
            StringBuilder sb = new StringBuilder(saveFile);
            for (Iterator iterator = conts.iterator(); iterator.hasNext(); ) {
                LsEntry obj = (LsEntry) iterator.next();
                String filename = new String(obj.getFilename().getBytes(),
                    "UTF-8");
                if (!(filename.indexOf(".") > -1)) {
                    // directory = FileUtil.normalize(directory
                    // + System.getProperty("file.separator") + filename);
                    directory = directory
                        + System.getProperty("file.separator") + filename;
                    // srcFile = directory;
                    // saveFile = FileUtil.normalize(saveFile
                    // + System.getProperty("file.separator") + filename);
                    sb.append(System.getProperty("file.separator")
                        + filename);
//                    saveFile = saveFile + System.getProperty("file.separator")
//                        + filename;
                } else {
                    // 扫描到文件名为".."这样的直接跳过
                    String[] arrs = filename.split("\\.");
                    if ((arrs.length > 0) && (arrs[0].length() > 0)) {
                        // srcFile = FileUtil.normalize(directory
                        // + System.getProperty("file.separator")
                        // + filename);
                        srcFile = directory
                            + System.getProperty("file.separator")
                            + filename;
                    } else {
                        continue;
                    }
                }
                saveFile = sb.toString();
                download(directory, srcFile, saveFile, sftp);
            }
        }
    }

    public static void uploadIns(String directory, InputStream file, ChannelSftp sftp, String name)
        throws Exception {
        // 进入目标路径
        boolean falg = false;
        Vector v = sftp.ls(directory);
        System.out.println("/app/==" + v.size());
        //遍历Vector中的元素
        for (int i = 0; i < v.size(); i++) {
            String[] a = v.get(i).toString().split(" ");
            if (a[a.length - 1].equals("pipConfig")) {
                System.out.println("exit:" + a[a.length - 1]);
                falg = true;
                break;
            }
        }

        InputStream ins = null;
        if (!falg) {
            sftp.mkdir("pipConfig");
        }
        try {
            sftp.cd("/app/aideploy/pipConfig/");
            sftp.rm(name);
        } catch (Exception e) {

        }

        try {
//                ins = new FileInputStream(file);
            sftp.put(file, name);

        } catch (Exception e) {

        } finally {
            if (ins != null) {
                ins.close();
            }
        }

    }
}
