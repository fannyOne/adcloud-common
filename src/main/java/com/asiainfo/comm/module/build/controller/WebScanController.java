package com.asiainfo.comm.module.build.controller;

/**
 * Created by guojian on 07/11/2016.
 */

import com.asiainfo.util.JerseyClient;
import com.asiainfo.util.StringUtil;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
@RequestMapping("/webscan")
public class WebScanController {
    
    /**
     * 啟動安全掃描
     * TaskId,模板名,网站url
     * localhost:8080/webscan/doScan/abc123;http://www.baidu.com;G20扫描模板
     * abc123;http://www.xx.com:8080/abc;templatename
     * 返回sessionid
     *
     * @return
     */
    @RequestMapping(value = "/doScan")
    public
    @ResponseBody
    String doScan(@RequestParam(value = "scanInfo") String scanInfo) {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String dateStr = sdf.format(date);
        String scanCode = dateStr + new StringUtil().getRandomStr(5, false, true, false);
        String[] infos = scanInfo.split("@");
        if (infos.length != 2) {
            return "error:scan info error !";
        }
        //String taskId = infos[0]+"_"+new Random().nextInt(200);
        //taskid是否存在,不存在就下发;TaskSLA是否存在,不存在就返回
        String sessionId = JerseyClient.getSessionID();
        JerseyClient.deleteTask(sessionId, scanCode);
        String str = JerseyClient.dispatchTask(sessionId, scanCode, infos[0], "", "", getScanTemplate(infos[1]));
        try {
            SAXReader reader = new SAXReader();
            Document doc = reader.read(IOUtils.toInputStream(str));
            Element ele = doc.getRootElement();
            Element s = ele.element("ErrorMsg");
            if (s != null && s.getText().contains("not exists")) {
                return "error:" + s.getText();
            }
            Element s2 = ele.element("BaseTaskId");
            if (s2 != null) {
                return "the scan details : <a href=\"https://10.78.146.199/viewDetectDetail?id=" + s2.getText() + "\" target=\"_blank\" >details</a>\nscanCode:" + scanCode + "\n-------------------\n";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //String st1 = JerseyClient.startTask(sessionId,infos[0]);
        return this.getResult(sessionId, scanCode);
    }

    /**
     * 當前掃描狀態
     * @param taskId
     * @return
     */


    /**
     * 獲取掃描結果
     *
     * @param taskId
     * @return
     */
    //@RequestMapping(value = "/getResult")
    // public @ResponseBody  String getResult(@RequestParam(value="taskId") String taskId) {
    public String getResult(String sessionId, String taskId) {
        String str = JerseyClient.getResultCountByTaskID(sessionId, taskId);
        String bs = "漏洞扫描:S1\n" +
            "木马检测:S2\n" +
            "篡改检测:S3\n" +
            "敏感关键字:S4\n";
        String S1 = "0";
        String S2 = "0";
        String S3 = "0";
        String S4 = "0";
        try {
            SAXReader reader = new SAXReader();
            Document doc = reader.read(IOUtils.toInputStream(str));
//            Element ele = doc.getRootElement().element("Funcs");
            java.util.List<Element> t = doc.getRootElement().element("Funcs").elements("Func");

            for (int i = 0; i < t.size(); i++) {
                switch (Integer.parseInt(t.get(i).element("ProductId").getText())) {
                    case 1:
                        S1 = t.get(i).element("Total").getText();
                        break;
                    case 2:
                        S2 = t.get(i).element("Total").getText();
                        break;
                    case 3:
                        S3 = t.get(i).element("Total").getText();
                        break;
                    case 4:
                        S4 = t.get(i).element("Total").getText();
                        break;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bs.replace("S1", S1).replace("S2", S2).replace("S3", S3).replace("S4", S4);
        // return "";
    }

    private String getScanTemplate(String temp) {
        String result = "漏洞扫描";
        switch (Integer.parseInt(temp)) {
            case 1:
                result = "月例行_0918";
                break;
            case 2:
                result = "G20扫描模板";
                break;
            default:
                break;
        }
        return result;
    }
}

