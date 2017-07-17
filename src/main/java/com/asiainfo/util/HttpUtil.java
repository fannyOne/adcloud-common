package com.asiainfo.util;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.json.JSONObject;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

/**
 * Created by weif on 2016/6/30.
 */
public class HttpUtil {

    public static String[] sendPost(String url, String param) {
        PrintWriter out = null;
        BufferedReader in = null;
        StringBuffer buf = new StringBuffer();
        String contentLength = "0";
        String[] ret = new String[2];
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("Content-Type", "application/json");

            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
            String line;
            contentLength = conn.getHeaderField("X-Text-Size");
            while ((line = in.readLine()) != null) {
                buf.append(line);
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！" + e);
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        ret[0] = buf.toString();
        ret[1] = "" + contentLength;
        return ret;
    }

    public static JSONObject httpGet(String url, Map<String, String> params) {
        //get请求返回结果
        JSONObject jsonResult = new JSONObject();
        StringBuffer sb = new StringBuffer(url);
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!sb.toString().contains("?")) {
                sb.append("?");
            } else {
                sb.append("&");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        url = sb.toString();

        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod(url);
        getMethod.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, 30000);
        getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
        String response = "";
        try {
            int statusCode = httpClient.executeMethod(getMethod);
            if (statusCode != HttpStatus.SC_OK) {
                System.out.println("请求出错: " + getMethod.getStatusLine());
            }

            byte[] responseBody = getMethod.getResponseBody();// 读取为字节数组
            response = new String(responseBody, "UTF-8");
        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            getMethod.releaseConnection();
        }
        if (response != null && !"".equals(response)) {
            jsonResult = new JSONObject(response);
        }
        return jsonResult;
    }

    public static String[] sendPostGetStyleAndReplace(String url, String param) {
        PrintWriter out = null;
        BufferedReader in = null;
        StringBuffer buf = new StringBuffer();
        String contentLength = "0";
        String[] ret = new String[2];
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            contentLength = conn.getHeaderField("X-Text-Size");
            while ((line = in.readLine()) != null) {
                if (line.contains("Started by ") || line.contains("originally caused by:")
                    || line.contains("Notifying endpoint")) {
                    continue;
                }
                line = "<p>" + line;
                if (line.startsWith("<p>+")) {
                    line = line.replaceFirst("<p>\\+", "<p class=\"command-color\">");
                }
                if (line.contains("sshpass") || line.contains("ADCloud_vm_public_deploy.sh") || line.contains("ADCloud_vm_public_restart.sh") || line.contains("ADCloud_dcoss_public_restart.sh") || line.contains("ADCloud_dcoss_public_deploy.sh")) {
                    line = line.replaceAll("sshpass\\s[^\\s]*\\s", "sshpass ****** ").replaceAll("sh[\\s&^\n]*((ADCloud_vm_public_deploy)|(ADCloud_dcoss_public_deploy))\\.sh[^\\n]*", "部署工程包\n").replaceAll("sh[\\s&^\n]*((ADCloud_vm_public_restart)|(ADCloud_dcoss_public_restart))\\.sh[^\\n]*", "重启应用\n");//隐藏密码信息
                }
                buf.append(line).append("</p>");
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！" + e);
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        ret[0] = buf.toString();
        ret[1] = "" + contentLength;
        return ret;
    }

    public String httppost(String json, String url, String urlPath, String userName, String passWord) {

        String ret = "";
        try {
            URL endpoint = new URL(url);
            HttpClient httpClient = new HttpClient();
            Credentials defaultcreds = new UsernamePasswordCredentials(userName, passWord);
            httpClient.getState().setCredentials(new AuthScope(endpoint.getHost(), endpoint.getPort(), AuthScope.ANY_REALM), defaultcreds);
            PostMethod postMethod = new PostMethod(url + urlPath);//"/simcard.php"
            StringRequestEntity requestEntity = new StringRequestEntity(
                json,
                "application/json",
                "UTF-8");
            postMethod.setRequestEntity(requestEntity);
            int statusCode = httpClient.executeMethod(postMethod);
            if (statusCode != HttpStatus.SC_OK) {
                System.out.println("Method failed code=" + statusCode + ": " + postMethod.getStatusLine());
            } else {
                System.out.println(new String(postMethod.getResponseBody(), "gb2312"));
                ret = new String(postMethod.getResponseBody(), "gb2312");
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

}

