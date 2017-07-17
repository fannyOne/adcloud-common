package com.asiainfo.util;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import javax.net.ssl.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import java.io.File;
import java.security.SecureRandom;
import java.security.cert.CertificateException;

/**
 * Created by guojian on 08/11/2016.
 */
public class JerseyClient {
    public static String SERVER_BASE_URL = "";

    public static void setServerBaseURL(String url) {
        SERVER_BASE_URL = url;
    }

    public static String getSessionID() {
        JerseyClient.setServerBaseURL("https://10.78.146.199/rest");
        String sessionId = JerseyClient.getSessionId("luoqiong", "Dev_lq@1019");
        return sessionId;
    }

    public static String getState(String taskId) {
        String sessionId = getSessionID();
        String str = JerseyClient.testTask(sessionId, taskId);
       /* if (str.contains("Fail") && str.contains("not found")) {
            return "wait";
        }*/
        try {
            SAXReader reader = new SAXReader();
            Document doc = reader.read(IOUtils.toInputStream(str));
            Element ele = doc.getRootElement();
            Element s = ele.element("State");
            if (s.getText().equals("finish")) {
                return "success";
            } else if (s.getText().equals("running")) {
                return "run";
            } else {
                return "fail";//fail
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "fail";
    }

    public static void buildConfig(String url, ClientConfig config) {
        if (url.startsWith("https")) {
            SSLContext ctx = getSSLContext();
            config.getProperties().put(
                HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
                new HTTPSProperties(new HostnameVerifier() {
                    @Override
                    public boolean verify(String s, SSLSession sslSession) {
                        return true;
                    }
                }, ctx));
        }
    }

    public static String loginCsoc(String url, String xmlContent) {
        ClientConfig config = new DefaultClientConfig();
        buildConfig(url, config);
        Client client = Client.create(config);
        WebResource service = client.resource(url);
        String response = service.type(MediaType.APPLICATION_XML).post(
            String.class, xmlContent);
        return response;
    }

    public static String GetInfos(String url, String sessionId) {
        ClientConfig config = new DefaultClientConfig();
        buildConfig(url, config);
        Client client = Client.create(config);
        WebResource service = client.resource(url);
        String response = service.cookie(new NewCookie("sessionid", sessionId))
            .type(MediaType.APPLICATION_XML).accept(MediaType.TEXT_XML)
            .get(String.class);
        return response;
    }

    public static String warningToCSoc(String url, String xmlContent) {
        ClientConfig config = new DefaultClientConfig();
        Client client = Client.create(config);
        WebResource service = client.resource(url);
        String sessionid = "xxxx";
        String response = service.cookie(new NewCookie("sessionid", sessionid))
            .type(MediaType.APPLICATION_XML).accept(MediaType.TEXT_XML)
            .post(String.class, xmlContent);
        return response;
    }

    public static String notifySecEvent(String url, String xmlContent) {
        ClientConfig config = new DefaultClientConfig();
        Client client = Client.create(config);
        WebResource service = client.resource(url);
        String response = service.type(MediaType.APPLICATION_XML)
            .accept(MediaType.TEXT_XML).post(String.class, xmlContent);
        return response;
    }

    public static String notifyTaskFinish(String url, String xmlContent) {
        ClientConfig config = new DefaultClientConfig();
        Client client = Client.create(config);
        WebResource service = client.resource(url);
        String response = service.type(MediaType.APPLICATION_XML)
            .accept(MediaType.TEXT_XML).post(String.class, xmlContent);
        return response;
    }

    public static SSLContext getSSLContext() {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(
                java.security.cert.X509Certificate[] arg0, String arg1)
                throws CertificateException {
            }

            @Override
            public void checkServerTrusted(
                java.security.cert.X509Certificate[] arg0, String arg1)
                throws CertificateException {
            }
        }};

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection
                .setDefaultSSLSocketFactory(sc.getSocketFactory());
            return sc;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    public static String PostInfos(String url, String xmlContent,
                                   String sessionid) {
        ClientConfig config = new DefaultClientConfig();
        buildConfig(url, config);
        Client client = Client.create(config);
        WebResource service = client.resource(url);
        String response = service.cookie(new NewCookie("sessionid", sessionid))
            .type(MediaType.APPLICATION_XML).post(String.class, xmlContent);
        return response;
    }

    /*
     * public static String PostInfos(String url,String xmlContent) {
     * ClientConfig config = new DefaultClientConfig(); buildConfig(url,config);
     * Client client = Client.create(config); WebResource service =
     * client.resource(url); String response =
     * service.type(MediaType.APPLICATION_XML).post(String.class, xmlContent);
     * return response; }
     */
    public static String PutInfos(String url, String sessionid) {
        ClientConfig config = new DefaultClientConfig();
        buildConfig(url, config);
        Client client = Client.create(config);
        WebResource service = client.resource(url);
        String response = service.cookie(new NewCookie("sessionid", sessionid))
            .type(MediaType.APPLICATION_XML).accept(MediaType.TEXT_XML)
            .put(String.class);
        return response;
    }

    public static String DeleteInfos(String url, String sessionid) {
        ClientConfig config = new DefaultClientConfig();
        buildConfig(url, config);
        Client client = Client.create(config);
        WebResource service = client.resource(url);
        String response = service.cookie(new NewCookie("sessionid", sessionid))
            .type(MediaType.APPLICATION_XML).accept(MediaType.TEXT_XML)
            .delete(String.class);
        return response;
    }

    public static String getSessionId(String userName, String pwd) {
        try {
            String xmlContent = "<Login><Name>" + userName
                + "</Name><Password>" + pwd + "</Password></Login>";
            String url = SERVER_BASE_URL + "/login";
            String str = JerseyClient.loginCsoc(url, xmlContent);
            SAXReader reader = new SAXReader();
            Document doc = reader.read(IOUtils.toInputStream(str));
            Element ele = doc.getRootElement();
            Element s = ele.element("SessionId");
            return s.getText();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String nullFilter(String str) {
        return str == null ? "" : str;
    }

    /**
     * @param sessionId
     * @return
     */
    public static String getSystemStatus(String sessionId) {
        String url = SERVER_BASE_URL + "/control/GetState";
        return GetInfos(url, sessionId);
    }

    // 启动引擎
    public static String startEngine(String sessionId) {
        String url = SERVER_BASE_URL + "/control/Start";
        return PostInfos(url, null, sessionId);
    }

    // 停止引擎
    public static String stopEngine(String sessionId) {
        String url = SERVER_BASE_URL + "/control/Stop";
        return PostInfos(url, null, sessionId);
    }

    // 获取任务模板
    public static String getSLA(String sessionId) {
        String url = SERVER_BASE_URL + "/control/SLA";
        return GetInfos(url, sessionId);
    }

    // 下发任务
    public static String dispatchTask(String sessionId, String taskId,
                                      String destURL, String destIP, String destPort, String taskSLA) {
        String xml = "<Task><TaskID>" + taskId
            + "</TaskID><CustomID>123123</CustomID><TaskInfo><DestURL>"
            + destURL + "</DestURL><DestIP>" + nullFilter(destIP)
            + "</DestIP><DestPort>" + nullFilter(destPort)
            + "</DestPort></TaskInfo><TaskSLA>" + taskSLA
            + "</TaskSLA></Task>";
        String url = SERVER_BASE_URL + "/task";
        return PostInfos(url, xml, sessionId);
    }

    // 取消任务
    public static String deleteTask(String sessionId, String taskId) {
        String url = SERVER_BASE_URL + "/task/Remove/" + taskId;
        return PostInfos(url, null, sessionId);
    }

    // 开始停止的任务
    public static String startTask(String sessionId, String taskId) {
        String url = SERVER_BASE_URL + "/task/Start/" + taskId;
        return PostInfos(url, null, sessionId);
    }

    // 停止任务
    public static String stopTask(String sessionId, String taskId) {
        String url = SERVER_BASE_URL + "/task/Stop/" + taskId;
        return PostInfos(url, null, sessionId);
    }

    // 任务立即测试
    public static String testTask(String sessionId, String taskId) {
        String url = SERVER_BASE_URL + "/task/Test/" + taskId;
        return PostInfos(url, null, sessionId);
    }

    // 根据任务ID获取该任务的扫描结果记录总数
    public static String getResultCountByTaskID(String sessionId, String taskId) {
        String url = SERVER_BASE_URL + "/report/ResultCount/TaskID/" + taskId;
        return GetInfos(url, sessionId);
    }

    // 根据任务ID分页获取扫描结果
    public static String getReportByTaskID(String sessionId, String taskId,
                                           String productId, int startNum, int size) {
        String url = SERVER_BASE_URL + "/report/TaskID";
        String xml = "<ResultParam><TaskID>" + taskId + "</TaskID>"
            + "<ProductID>" + productId + "</ProductID><StartNum>"
            + startNum + "</StartNum><Size>" + size
            + "</Size></ResultParam>";
        return PostInfos(url, xml, sessionId);
    }

    // 根据ReportID获取记录总数
    public static String getResultCountByReportID(String sessionId, int reportId) {
        String url = SERVER_BASE_URL + "/report/ResultCount/ReportID/"
            + reportId;
        return GetInfos(url, sessionId);
    }

    // 根据ReportID分页获取扫描结果
    public static String getReportByReportID(String sessionId, int reportId,
                                             int startNum, int size) {
        String url = SERVER_BASE_URL + "/report/ReportID";
        String xml = "<ReportParam><ReportID>" + reportId
            + "</ReportID><StartNum>" + startNum + "</StartNum><Size>"
            + size + "</Size></ReportParam>";
        return PostInfos(url, xml, sessionId);
    }

    // 获取Report数量
    public static String getReportCount(String sessionId) {
        String url = SERVER_BASE_URL + "/report/ReportCount";
        return GetInfos(url, sessionId);
    }

    // 分页获取Report列表
    public static String getReportIDList(String sessionId, int startNum,
                                         int size) {
        String url = SERVER_BASE_URL + "/report/ReportIDList";
        String xml = "<PageParam><StartNum>" + startNum + "</StartNum><Size>" + size
            + "</Size></PageParam>";
        return PostInfos(url, xml, sessionId);
    }

    public static String getTaskProgress(String sessionId, String taskId,
                                         String productId) {
        String url = SERVER_BASE_URL + "/task/getTaskProgress";
        String xml = "<Task><TaskID>" + taskId + "</TaskID><ProductID>"
            + productId + "</ProductID></Task>";
        return PostInfos(url, xml, sessionId);
    }

    public static String getTaskLoadInfo(String sessionId) {
        String url = SERVER_BASE_URL + "/task/GetTaskLoadInfo";
        return GetInfos(url, sessionId);
    }

    public static String distortChangeActive(String sessionId, String taskId) {
        String url = SERVER_BASE_URL + "/task/distortChangeActive/" + taskId;
        return PostInfos(url, null, sessionId);
    }

    public static String getWebsiteCount(String sessionId) {
        String url = SERVER_BASE_URL + "/report/GetWebsiteCount";
        return GetInfos(url, sessionId);
    }

    public static String getWebsiteList(String sessionId, int startNum, int size) {
        String url = SERVER_BASE_URL + "/report/GetWebsiteList";
        String xml = "<PageParam><StartNum>" + startNum + "</StartNum><Size>" + size + "</Size></PageParam>";
        return PostInfos(url, xml, sessionId);
    }

    public static String getReportCountByWebID(String sessionId, String websiteId) {
        String url = SERVER_BASE_URL + "/report/GetReportCountByWebID/" + websiteId;
        return GetInfos(url, sessionId);
    }

    public static String getReportIDListByWebId(String sessionId) {
        String url = SERVER_BASE_URL + "/report/GetReportIDListByWebId";
        String xml = "<Param><WebId>2</WebId><ProductId>1</ProductId><StartNum>0</StartNum><Size>100</Size></Param>";
        return PostInfos(url, xml, sessionId);
    }

    public static String getEngineState(String sessionId) {
        String url = SERVER_BASE_URL + "/control/GetEngineState";
        return GetInfos(url, sessionId);
    }

    public static String getIssueRepositoryList(String sessionId) {
        String url = SERVER_BASE_URL + "/control/GetIssueRepositoryList";
        return GetInfos(url, sessionId);
    }

    public static String updateEngine(String sessionId) {
        String url = SERVER_BASE_URL + "/control/UpdateEngine";
        String xml = "<RequestCommand><command>update_version</command></RequestCommand>";
        return PostInfos(url, xml, sessionId);
    }

    public static String createReport(String sessionId, String reportId, String reportType) {
        String url = SERVER_BASE_URL + "/report/CreateReport/" + reportId;
        String xml = "<ReportParam><ReportType>" + reportType + "</ReportType></ReportParam>";
        return PostInfos(url, xml, sessionId);
    }

    public static String getReportFileStatus(String sessionId, String reportZipFileName) {
        String url = SERVER_BASE_URL + "/report/GetReportFileStatus/" + reportZipFileName;
        return PostInfos(url, null, sessionId);
    }

    public static File downloadReportFile(String sessionId, String reportFileName) {
        String url = SERVER_BASE_URL + "/report/DownloadReportFile/" + reportFileName;
        ClientConfig config = new DefaultClientConfig();
        buildConfig(url, config);
        Client client = Client.create(config);
        WebResource service = client.resource(url);
        File response = service.cookie(new NewCookie("sessionid", sessionId)).type(
            MediaType.APPLICATION_XML).post(File.class, null);
        return response;
    }

    public static void main(String args[]) throws Exception {
        JerseyClient.getState("abc");
        //setServerBaseURL("https://10.78.146.199/rest");
//https://10.78.146.199/rest/login
        /*String sessionId = getSessionId("luoqiong", "Dev_lq@1019");
        System.out.println(sessionId);

        System.out.println(testTask(sessionId, "abc123"));
        String str = getResultCountByTaskID(sessionId, "abc123");

        SAXReader reader = new SAXReader();
        Document doc = reader.read(IOUtils.toInputStream(str));
        Element ele = doc.getRootElement().element("Funcs");
        java.util.List<Element> t = doc.getRootElement().element("Funcs").elements("Func");
        StringBuffer bs = new StringBuffer();
        for (int i = 0; i < t.size(); i++) {
            switch (Integer.parseInt(t.get(i).element("ProductId").getText())) {
                case 1:
                    bs.append("漏洞扫描:").append(t.get(i).element("Total").getText()).append("\n");
                    break;
                case 2:
                    bs.append("木马检测:").append(t.get(i).element("Total").getText()).append("\n");
                    break;
                case 3:
                    bs.append("篡改检测:").append(t.get(i).element("Total").getText()).append("\n");
                    break;
                case 4:
                    bs.append("敏感关键字:").append(t.get(i).element("Total").getText()).append("\n");
                    break;
            }

        }

        System.out.print(bs.toString());*/
    }
}