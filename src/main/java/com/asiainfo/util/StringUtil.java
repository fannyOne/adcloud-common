package com.asiainfo.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import static com.asiainfo.util.CommConstants.WebStatusCode.SECRET_CODE;
import static com.asiainfo.util.CommConstants.WebStatusCode.SECRET_CODE_LENGTH;

/**
 * Created by YangRY on 2016/6/30 0030.
 */
public class StringUtil {
    /**
     * 将驼峰式命名的字符串转换为下划线大写方式。如果转换前的驼峰式命名的字符串为空，则返回空字符串。</br>
     * 例如：HelloWorld->HELLO_WORLD
     *
     * @param name 转换前的驼峰式命名的字符串
     * @return 转换后下划线大写方式命名的字符串
     */
    public static String underscoreName(String name) {
        StringBuilder result = new StringBuilder();
        if (name != null && name.length() > 0) {
            // 将第一个字符处理成大写
            result.append(name.substring(0, 1).toUpperCase());
            // 循环处理其余字符
            for (int i = 1; i < name.length(); i++) {
                String s = name.substring(i, i + 1);
                // 在大写字母前添加下划线
                if (s.equals(s.toUpperCase()) && !Character.isDigit(s.charAt(0))) {
                    result.append("_");
                }
                // 其他字符直接转成大写
                result.append(s.toUpperCase());
            }
        }
        return result.toString();
    }

    /**
     * 将下划线大写方式命名的字符串转换为驼峰式。如果转换前的下划线大写方式命名的字符串为空，则返回空字符串。</br>
     * 例如：HELLO_WORLD->HelloWorld
     *
     * @param name 转换前的下划线大写方式命名的字符串
     * @return 转换后的驼峰式命名的字符串
     */
    public static String camelName(String name) {
        StringBuilder result = new StringBuilder();
        // 快速检查
        if (name == null || name.isEmpty()) {
            // 没必要转换
            return "";
        } else if (!name.contains("_")) {
            // 不含下划线，仅将首字母小写
            return name.substring(0, 1).toLowerCase() + name.substring(1);
        }
        // 用下划线将原始字符串分割
        String camels[] = name.split("_");
        for (String camel : camels) {
            // 跳过原始字符串中开头、结尾的下换线或双重下划线
            if (camel.isEmpty()) {
                continue;
            }
            // 处理真正的驼峰片段
            if (result.length() == 0) {
                // 第一个驼峰片段，全部字母都小写
                result.append(camel.toLowerCase());
            } else {
                // 其他的驼峰片段，首字母大写
                result.append(camel.substring(0, 1).toUpperCase());
                result.append(camel.substring(1).toLowerCase());
            }
        }
        return result.toString();
    }

    public static String ADEncrypt(String expressly) {
        return expressly.replaceAll("(.){1}", "*");
    }

    @NotNull
    public static String ADEncrypt(int length) {
        return SECRET_CODE.substring(0, length);
    }

    @NotNull
    public static String ADEncrypt() {
        return ADEncrypt(SECRET_CODE_LENGTH);
    }

    public static String ADEncryptMix(String expressly, int expressLength, int secretLength) {
        if (expressly.length() <= expressLength) {
            return ADEncrypt(SECRET_CODE_LENGTH + expressLength);
        } else {
            return (expressly.substring(0, expressLength) + ADEncrypt(secretLength));
        }
    }

    public static String ADEncryptMix(String expressly, int expressLength) {
        if (expressly.length() <= expressLength) {
            return ADEncrypt(SECRET_CODE_LENGTH + expressLength);
        } else {
            return (expressly.substring(0, expressLength) + ADEncrypt(SECRET_CODE_LENGTH));
        }
    }

    public String getRandomStr(int length, boolean number, boolean upCase, boolean lowCase) {
        if (!number && !upCase && !lowCase) {
            return null;
        }
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        int ranNum;
        for (int i = 0; i < length; i++) {
            ranNum = random.nextInt(3);
            switch (ranNum) {
                case 0:
                    if (!upCase) {
                        i--;
                        break;
                    }
                    sb.append((char) ('A' + random.nextInt(26)));
                    break;
                case 1:
                    if (!lowCase) {
                        i--;
                        break;
                    }
                    sb.append((char) ('a' + random.nextInt(26)));
                    break;
                case 2:
                    if (!number) {
                        i--;
                        break;
                    }
                    sb.append((char) ('0' + random.nextInt(10)));
                    break;
            }
        }
        String returnStr = sb.toString();
        return returnStr;
    }

    public String getRandomStr(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        int ranNum;
        for (int i = 0; i < length; i++) {
            ranNum = random.nextInt(3);
            switch (ranNum) {
                case 0:
                    sb.append((char) ('A' + random.nextInt(26)));
                    break;
                case 1:
                    sb.append((char) ('a' + random.nextInt(26)));
                    break;
                case 2:
                    sb.append((char) ('0' + random.nextInt(10)));
                    break;
            }
        }
        String returnStr = sb.toString();
        return returnStr;
    }

    public String ChineseToPinyin(String name) {
        StringBuilder pinyinName = new StringBuilder("");
        char[] nameChar = name.toCharArray();
        HanyuPinyinOutputFormat defaultFormat =
            new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        changePinYin(defaultFormat, pinyinName, nameChar);
        return pinyinName.toString();
    }

    public String ChineseToPinyin(String name, HanyuPinyinOutputFormat defaultFormat) {
        StringBuilder pinyinName = new StringBuilder("");
        char[] nameChar = name.toCharArray();
        defaultFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        changePinYin(defaultFormat, pinyinName, nameChar);
        return pinyinName.toString();
    }

    private void changePinYin(HanyuPinyinOutputFormat defaultFormat, StringBuilder pinyinName, char[] nameChar) {
        for (int i = 0; i < nameChar.length; i++) {
            if (nameChar[i] > 128) {
                try {
                    pinyinName.append(PinyinHelper.toHanyuPinyinStringArray
                        (nameChar[i], defaultFormat)[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                pinyinName.append(nameChar[i]);
            }
        }
    }

    public byte[] readBytes(InputStream is, int contentLen) throws IOException {
        if (contentLen > 0) {
            int readLen = 0;
            int readLengthThisTime = 0;
            byte[] message = new byte[contentLen];
            try {
                while (readLen != contentLen) {
                    readLengthThisTime = is.read(message, readLen, contentLen
                        - readLen);
                    if (readLengthThisTime == -1) {// Should not happen.
                        break;
                    }
                    readLen += readLengthThisTime;
                }
                return message;
            } catch (IOException e) {
                throw e;
            }
        }
        return new byte[]{};
    }
}
