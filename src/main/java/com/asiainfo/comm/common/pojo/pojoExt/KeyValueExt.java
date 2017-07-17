package com.asiainfo.comm.common.pojo.pojoExt;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by yangry on 2016/12/28.
 */
@Setter
@Getter
public class KeyValueExt {
    String key;
    Object value;

    public KeyValueExt(String key, Object value) {
        setKey(key);
        setValue(value);
    }
}
