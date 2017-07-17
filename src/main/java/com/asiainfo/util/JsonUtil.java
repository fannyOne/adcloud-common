package com.asiainfo.util;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;


public class JsonUtil {
    protected static final Log log = LogFactory.getLog(JsonUtil.class);


    public static <T> T jsonToBean(String jsonString, Class<T> beanCalss) {
        JSONObject jsonObject = JSONObject.fromObject(jsonString);
        T bean = (T) JSONObject.toBean(jsonObject, beanCalss);
        return bean;
    }

    public static JSON jsonToJSON(String jsonString, boolean isArray, String... keys) {
        JSONObject jsonObject = JSONObject.fromObject(jsonString);
        if (keys == null || keys.length == 0) {
            return jsonObject;
        }
        int index = keys.length;
        for (int i = 0; i < index - 1; i++) {
            if (!jsonObject.isNullObject()) {
                jsonObject = jsonObject.getJSONObject(keys[i]);
            }
        }
        if (isArray) {
            if (!jsonObject.isNullObject()) {
                Object ob = jsonObject.get(keys[index - 1]);
                if (ob instanceof JSONArray) {
                    JSONArray jsonnArray = (JSONArray) ob;
                    return jsonnArray;
                } else {
                    return new JSONArray();
                }
            }
        }
        if (!jsonObject.isNullObject()) {
            jsonObject = jsonObject.getJSONObject(keys[index - 1]);
        }
        return jsonObject.isNullObject() ? null : jsonObject;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> jsonToMap(String json) {
        Map<String, Object> result = JSONObject.fromObject(json);
        return result == null ? new HashMap<String, Object>() : result;
    }

    @SuppressWarnings("unchecked")
    public static <T> Collection<T> jsonToArray(String json, Class<T> cls) {
        Collection<T> list = JSONArray.toCollection(JSONArray.fromObject(json), cls);
        return list;
    }


    public static String mapToJson(Map<String, Object> map) {
        JSONObject json = JSONObject.fromObject(map);
        return json.toString();
    }

    public static boolean isJson(String result) {
        if (StringUtils.isEmpty(result)) {
            return false;
        }
        try {
            JSONObject.fromObject(result);
            return true;
        } catch (Exception e) {
            log.error("result is not a valid JSON value");
            return false;
        }
    }

    public static boolean isJsonArray(String result) {
        if (StringUtils.isEmpty(result)) {
            return false;
        }
        try {
            JSONArray.fromObject(result);
            return true;
        } catch (Exception e) {
            log.error("result is not a valid JSON value");
            return false;
        }
    }

    public static HashMap<String, Object> reflect(JSONObject json) {

        HashMap<String, Object> map = new HashMap<String, Object>();

        Set<?> keys = json.keySet();

        for (Object key : keys) {

            Object o = json.get(key);

            if (o instanceof JSONArray)

                map.put((String) key, reflect((JSONArray) o));

            else if (o instanceof JSONObject)

                map.put((String) key, reflect((JSONObject) o));

            else

                map.put((String) key, o);

        }

        return map;

    }


    public static List reflect(JSONArray json) {

        List<Object> list = new ArrayList<Object>();
        if (json == null || "".equals(json)) {
            return list;
        }
        for (Object o : json) {

            if (o instanceof JSONArray)

                list.add(reflect((JSONArray) o));

            else if (o instanceof JSONObject)

                list.add(reflect((JSONObject) o));

            else

                list.add(o);

        }

        return list;

    }

}
