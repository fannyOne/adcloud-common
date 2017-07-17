package com.asiainfo.util;

import com.asiainfo.comm.common.pojo.Pojo;
import net.sf.json.JSONArray;

/**
 * Created by yangry on 2016/6/15 0015.
 */
public class JsonpUtil {

    public static String addCallBack(String json) {
        return "callback(" + json + ");";
    }

    public static String addJsonMode(String data, String sign) {
        String json = "{\"" + sign + "\":\"" + data.replaceAll("[\n\t\r]", "<br/>") + "\"}";
        return "callback(" + json + ");";
    }

    public static String modelToJsonp(Pojo obj) {
        if (obj.getRetCode() == null || obj.getRetCode().equals("")) {
            obj.setRetCode("200");
        }
        JSONArray json = JSONArray.fromObject(obj);
        return JsonpUtil.addCallBack(json.get(0).toString());
    }

    public static String modelToJson(Pojo obj) {
        if (obj.getRetCode() == null || obj.getRetCode().equals("")) {
            obj.setRetCode("200");
        }
        JSONArray json = JSONArray.fromObject(obj);
        return json.get(0).toString();
    }

    public static String objToJson(Object obj) {
        JSONArray json = JSONArray.fromObject(obj);
        return json.get(0).toString();
    }
}
