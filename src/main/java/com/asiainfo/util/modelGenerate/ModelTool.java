package com.asiainfo.util.modelGenerate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yangry on 2016/6/14 0014.
 */
public class ModelTool {
    //数据库连接
    private static final String URL = "jdbc:oracle:thin:@20.26.11.7:1521:CSHP04";
    PropertyEngine pro = new PropertyEngine(new File("").getAbsolutePath() + "/src/main/resources/ebean.properties");
    private PrintWriter pw = null;
    private Connection conn = null;
    private PreparedStatement pst = null;
    private ResultSetMetaData rsm = null;
    private String sql = "SELECT * FROM ";
    private String packageOutPath = "com.asiainfo.comm.module.models";//指定实体生成所在包的路径
    private String authorName = "yangry";//作者名字
    private String tableName = "user";//表名
    private String[] colNames; // 列名数组
    private String[] colTypes; //列名类型数组
    private int[] colSizes; //列名大小数组
    private SimpleDateFormat smt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private boolean f_util = false; // 是否需要导入包java.util.*
    private boolean f_sql = false; // 是否需要导入包java.sql.*
//    private static final String NAME = "devops";
//    private static final String PASS = "devops";
//    private static final String DRIVER = "com.oracle.jdbc.Driver";

    public ModelTool(String authorName, String tableName) throws SQLException, IOException {
        try {
            this.authorName = authorName;
            this.tableName = tableName;
            sql += tableName;
            conn = DriverManager.getConnection(URL, pro.getValue("datasource.ora.username"), pro.getValue("datasource.ora.password"));
            pst = conn.prepareStatement(sql);
            rsm = pst.getMetaData();
            int size = rsm.getColumnCount();   //统计列
            colNames = new String[size];
            colTypes = new String[size];
            colSizes = new int[size];
            //数组填装
            for (int runSize = 0; runSize < size; runSize++) {
                colNames[runSize] = rsm.getColumnName(runSize + 1);
                colTypes[runSize] = rsm.getColumnTypeName(runSize + 1);
                colSizes[runSize] = rsm.getColumnDisplaySize(runSize + 1);
                if (colTypes[runSize].equalsIgnoreCase("datetime")) {
                    f_util = true;
                }
                if (colTypes[runSize].equalsIgnoreCase("image") || colTypes[runSize].equalsIgnoreCase("text")) {
                    f_sql = true;
                }
            }
            //文件内容
            String content = parse();
            File directory = new File("");
            //System.out.println("绝对路径："+directory.getAbsolutePath());
            //System.out.println("相对路径："+directory.getCanonicalPath());
            String path = this.getClass().getResource("").getPath();
            System.out.println(path);
            System.out.println("src/?/" + path.substring(path.lastIndexOf("/com/", path.length())));
//              String outputPath = directory.getAbsolutePath()+ "/src/"+path.substring(path.lastIndexOf("/com/", path.length()), path.length()) + initcap(tablename) + ".java";
            String outputPath = directory.getAbsolutePath() + "/src/main/java/" + this.packageOutPath.replace(".", "/") + "/" + formatTableName(tableName) + ".java";
            FileWriter fw = new FileWriter(outputPath);
            pw = new PrintWriter(fw);
            pw.println(content);
            pw.flush();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (pw != null) {
                pw.close();
            }
            if (conn != null) {
                conn.close();
            }
            if (pst != null) {
                pst.close();
            }
        }

    }

    private String parse() {
        StringBuffer sb = new StringBuffer();
        //判断是否导入工具包
        if (f_util) {
            sb.append("import java.util.Date;\r\n");
        }
        if (f_sql) {
            sb.append("import java.sql.*;\r\n");
        }
        sb.append("package " + this.packageOutPath + ";\r\n");
        sb.append("\r\n");
        //注释部分
        sb.append("   /**\r\n");
        sb.append("    * " + tableName + " Model\r\n");
        sb.append("    * " + smt.format(new Date()) + " " + this.authorName + "\r\n");
        sb.append("    */ \r\n");
        //实体部分
        sb.append("\r\n\r\npublic class " + formatTableName(tableName) + "{\r\n");
        processAllAttrs(sb);//属性
        processAllMethod(sb);//get set方法
        sb.append("}\r\n");

        //System.out.println(sb.toString());
        return sb.toString();
    }

    /**
     * 功能：生成所有属性
     *
     * @param sb
     */
    private void processAllAttrs(StringBuffer sb) {
        for (int i = 0; i < colNames.length; i++) {
            sb.append("\tprivate " + sqlType2JavaType(colTypes[i]) + " " + colNames[i] + ";\r\n");
        }
    }

    /**
     * 功能：生成所有方法
     *
     * @param sb
     */
    private void processAllMethod(StringBuffer sb) {
        for (int i = 0; i < colNames.length; i++) {
            sb.append("\tpublic void set" + formatTableName(colNames[i]) + "(" + sqlType2JavaType(colTypes[i]) + " " +
                colNames[i] + "){\r\n");
            sb.append("\tthis." + colNames[i] + "=" + colNames[i] + ";\r\n");
            sb.append("\t}\r\n");
            sb.append("\tpublic " + sqlType2JavaType(colTypes[i]) + " get" + formatTableName(colNames[i]) + "(){\r\n");
            sb.append("\t\treturn " + colNames[i] + ";\r\n");
            sb.append("\t}\r\n");
        }
    }

    /**
     * 功能：将输入字符串的首字母改成大写
     *
     * @param str
     * @return
     */
    private String initCap(String str) {
        char[] ch = str.toCharArray();
        if (ch[0] >= 'a' && ch[0] <= 'z') {
            ch[0] = (char) (ch[0] - 32);
        }
        return new String(ch);
    }

    /**
     * 功能：将输入字符串的首字母改成大写
     *
     * @param str
     * @return
     */
    private String formatTableName(String str) {
        String returnStr = "";
        str = str.toLowerCase();
        String[] strS = str.split("_");
        for (int i = 0; i < strS.length; i++) {
            char[] ch = strS[i].toCharArray();
            ch[0] = (char) (ch[0] - 32);
            returnStr = new String(ch);
        }
        return returnStr;
    }

    /**
     * 功能：获得列的数据类型
     *
     * @param sqlType
     * @return
     */
    private String sqlType2JavaType(String sqlType) {
        if (sqlType.equalsIgnoreCase("bit")) {
            return "boolean";
        } else if (sqlType.equalsIgnoreCase("tinyint")) {
            return "byte";
        } else if (sqlType.equalsIgnoreCase("smallint")) {
            return "short";
        } else if (sqlType.equalsIgnoreCase("int")) {
            return "int";
        } else if (sqlType.equalsIgnoreCase("bigint")) {
            return "long";
        } else if (sqlType.equalsIgnoreCase("float")) {
            return "float";
        } else if (sqlType.equalsIgnoreCase("decimal") || sqlType.equalsIgnoreCase("numeric")
            || sqlType.equalsIgnoreCase("real") || sqlType.equalsIgnoreCase("money")
            || sqlType.equalsIgnoreCase("smallmoney")) {
            return "double";
        } else if (sqlType.equalsIgnoreCase("varchar") || sqlType.equalsIgnoreCase("char")
            || sqlType.equalsIgnoreCase("nvarchar") || sqlType.equalsIgnoreCase("nchar")
            || sqlType.equalsIgnoreCase("text")) {
            return "String";
        } else if (sqlType.equalsIgnoreCase("datetime")) {
            return "Date";
        } else if (sqlType.equalsIgnoreCase("image")) {
            return "Blob";
        }
        return null;
    }
}
