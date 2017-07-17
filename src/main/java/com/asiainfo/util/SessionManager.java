package com.asiainfo.util;

import com.asiainfo.comm.module.common.AdParaDetailDAO;
import com.asiainfo.comm.module.models.AdParaDetail;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by YangRY
 * 2016/6/30 0030.
 */
public class SessionManager {
    @Autowired
    AdParaDetailDAO paraDetailDAO;

    public SessionManager() throws IOException {
    }

    public Connection getConnection(String databaseName) throws SQLException {
        AdParaDetail paraDetail = paraDetailDAO.qryByDetails("X", databaseName.toUpperCase() + "_DB_INFO", databaseName.toUpperCase() + "_DB_INFO").get(0);
        return DriverManager.getConnection(paraDetail.getPara1(), paraDetail.getPara2(), paraDetail.getPara3());
    }
}
