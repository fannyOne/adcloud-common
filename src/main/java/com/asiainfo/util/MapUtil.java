package com.asiainfo.util;

import org.apache.commons.collections.MapUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by weif on 2017/1/10.
 */
public class MapUtil {


    public static Map getValue(Object thisObj) throws IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        Map map = new HashMap();
        Class c;
        try {
            c = Class.forName(thisObj.getClass().getName());
            Method[] m = c.getMethods();
            for (int i = 0; i < m.length; i++) {
                String method = m[i].getName();
                if (method.startsWith("get")) {
                    try {
                        Object value = m[i].invoke(thisObj);
                        if (value != null) {
                            String key = method.substring(3);
                            key = key.substring(0, 1).toUpperCase() + key.substring(1);
                            map.put(key, value);
                        }
                    } catch (Exception e) {
                        // TODO: handle exception
                        System.out.println("error:" + method);
                        throw e;
                    }
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            throw e;
        }
        return map;
    }

    public static String getValue(Map map, String key, String defaultValue) {
        String value = defaultValue;
        if (MapUtils.isNotEmpty(map)) {
            if (map.containsKey(key)) {
                return null == map.get(key) ? defaultValue : (String) map.get(key);
            }
        }
        return value;
    }

    public static long getValue(Map map, String key, long defaultValue) {
        long value = defaultValue;
        if (MapUtils.isNotEmpty(map)) {
            if (map.containsKey(key)) {
                return null == map.get(key) ? defaultValue : Long.parseLong((String) map.get(key));
            }
        }
        return value;
    }

    public static String getValue(Map map, String key) {
        return getValue(map, key, "");
    }

    public static long getLongValue(Map map, String key) {
        return getValue(map, key, 0l);
    }
}
