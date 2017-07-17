package com.asiainfo.util.modelGenerate;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by YangRY
 * on 2016/6/28 0028.
 */

public class PropertyEngine {
    private Properties properties;

    public PropertyEngine(String fileNamePath) throws IOException {
        properties = new Properties();
        InputStream in = null;
        try {
            in = new FileInputStream(fileNamePath);
            properties.load(in);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != in) {
                in.close();
            }
        }
    }

    public String getValue(String name) {
        return properties.getProperty(name);
    }
}
