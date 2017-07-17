package com.asiainfo.util;

import com.asiainfo.comm.module.common.AdParaDetailImpl;
import com.asiainfo.comm.module.models.AdParaDetail;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Contract;

import javax.net.ssl.*;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * Created by YangRY
 * 2016/10/10 0010.
 */
public class SpiderUtil {
    //获取Https链接
    public static HttpsURLConnection getHttpsConnect(String url) throws Exception {
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, new TrustManager[]{new SpiderUtil.TrustAnyTrustManager()},
            new java.security.
                SecureRandom());
        URL console = new URL(
            url);
        HttpsURLConnection conn = (HttpsURLConnection) console
            .openConnection();
        conn.setSSLSocketFactory(sc.getSocketFactory());
        conn.setHostnameVerifier(new SpiderUtil.TrustAnyHostnameVerifier());
        return conn;
    }

    //获取登录后的cookie
    @Contract("null -> fail")
    public static HeaderModel loginIn(HeaderModel model) throws Exception {
        String cookieStr;
        StringBuilder sb = new StringBuilder();
        if (model == null) {
            throw new Exception("The model can not be null");
        } else {
            if (StringUtils.isNotEmpty(model.getLoginUrl())) {
                List<String> cookie;
                HttpsURLConnection conn = getHttpsConnect(model.getLoginUrl());
                conn.setDoOutput(model.getIsOutput());
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(conn
                    .getOutputStream(), model.getEncoding());
                outputStreamWriter.write(model.getParams());
                outputStreamWriter.flush();
                outputStreamWriter.close();
                //获取cookie
                cookie = conn.getHeaderFields().get("Set-Cookie");
                for (String cooStr : cookie) {
                    sb.append(cooStr.split(";")[0] + ";");
                }
                cookieStr = sb.toString();
                if (cookieStr.length() > 0) {
                    cookieStr = cookieStr.substring(0, cookieStr.length() - 1);
                }
            } else {
                throw new Exception("The url can not be null");
            }
        }
        model.setCookie(cookieStr);
        return model;
    }

    @Contract("null -> fail")
    public static String getWebInfo(HeaderModel model) throws Exception {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotEmpty(model.getUrl())) {
            HttpsURLConnection conn = getHttpsConnect(model.getUrl());
            conn.setRequestProperty("Cookie", model.getCookie());
            conn.connect();
            InputStream is = conn.getInputStream();
            DataInputStream indata = null;
            try {
                indata = new DataInputStream(is);
                String ret = "";
                while (ret != null) {
                    ret = indata.readLine();
                    if (ret != null && !ret.trim().equals("")) {
                        sb.append(new String(ret.getBytes("ISO-8859-1"), model.getEncoding()));
                    }
                }
            } catch (Exception e) {
                throw e;
            } finally {
                //关闭链接
                conn.disconnect();
                if (indata != null) {
                    try {
                        indata.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            throw new Exception("The url can not be null");
        }
        return sb.toString();
    }

    public static void loginLeangoo(AdParaDetailImpl paraDetailImpl) throws Exception {
        if (CommConstants.SpiderConstant.HEADER_MODEL == null) {
            //模拟登陆获取cookie
            AdParaDetail detail = paraDetailImpl.qryByDetails("X", "LEANGOO_ACCOUNT", "LEANGOO_ACCOUNT");
            SpiderUtil.HeaderModel model = new SpiderUtil.HeaderModel("https://www.leangoo.com/kanban/login/go"
                , "email=" + detail.getPara1() + "&pwd=" + detail.getPara2() + "&loginRemPwdVal=true&from_page=", "UTF-8", true);
            model = SpiderUtil.loginIn(model);
            CommConstants.SpiderConstant.SET_HEADER_MODEL(model);
        }
    }

    private static class TrustAnyTrustManager implements X509TrustManager {

        public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[]{};
        }
    }

    private static class TrustAnyHostnameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    //获取登录cookie需要的Bean
    @Data
    public static class HeaderModel {
        private String url;
        private String params;
        private String encoding;
        private Boolean isOutput;
        private String cookie;
        private String loginUrl;

        public HeaderModel() {
            url = null;
            params = "";
            encoding = "GBK";
            isOutput = true;
        }

        public HeaderModel(String loginUrl, String params, String encoding, boolean isOutput) {
            this.loginUrl = loginUrl;
            this.params = params;
            this.encoding = encoding;
            this.isOutput = isOutput;
        }
    }
}
