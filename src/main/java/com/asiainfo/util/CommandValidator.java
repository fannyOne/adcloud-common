package com.asiainfo.util;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Created by zhenghp on 2016/8/28.
 */
public class CommandValidator {

    /**
     * 断言对象不能为null。
     *
     * @param fieldName  要验证的字段名称
     * @param fieldValue 要验证的字段值
     */
    public static void assertObjectNotNull(String fieldName, Object fieldValue) {
        if (fieldValue == null) {
            throw new IllegalArgumentException(String.format("[%s]不能为空。", fieldName));
        }
    }

    public static void assertStringNotNull(String fieldName, String fieldValue) {
        if (StringUtils.isEmpty(fieldValue)) {
            throw new IllegalArgumentException(String.format("[%s]不能为空。", fieldName));
        }
    }

    public static <E> void assertListNotNull(String fieldName, List<E> list) {
        if (CollectionUtils.isEmpty(list)) {
            throw new IllegalArgumentException(String.format("[%s]不能为空。", fieldName));
        }
    }

}
