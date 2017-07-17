package com;

import com.asiainfo.comm.module.models.AdStaticData;
import com.asiainfo.util.CommConstants;
import com.asiainfo.comm.module.common.AdStaticDataDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.List;

import static com.asiainfo.util.CommConstants.BuildConstants.AD_CLOUD_IP;

/**
 * Created by YangRY
 * 2016/7/4 0004.
 */
@lombok.extern.slf4j.Slf4j
@Component
@Profile({"printIp", "prod"})
public class PrintIp implements CommandLineRunner {
    @Autowired
    AdStaticDataDAO staticDataDAO;

    @Override
    public void run(String... args) throws Exception {
        Enumeration allNetInterfaces = NetworkInterface.getNetworkInterfaces();
        InetAddress ip;
        List<AdStaticData> staticDatas = staticDataDAO.qryByCodeType("SYNC_RMP");
        while (allNetInterfaces.hasMoreElements()) {
            NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
//            System.out.println(netInterface.getName());
            Enumeration addresses = netInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                ip = (InetAddress) addresses.nextElement();
                if (ip != null && ip instanceof Inet4Address && !ip.getHostAddress().equals("127.0.0.1")) {
                    log.error("本机的IP = " + ip.getHostAddress());
                    CommConstants.BuildConstants.UPDATE_IP(ip.getHostAddress());
                }
            }
        }
        /*if (AD_CLOUD_IP.matches("[10]+.[0-9]*.[0-9]*.[0-9]*")) {
            log.error("本地Ip不计入数据库");
            return;
        }*/
        if (staticDatas != null && staticDatas.size() > 0) {
            log.error("=====================================It is in command class!");
            log.error("=====================================Update ip start.");
            AdStaticData data = staticDatas.get(0);
            data.setCodeValue(AD_CLOUD_IP);
            staticDataDAO.update(data);
            log.error("=====================================Update ip end.");
        } else {
            System.out.println("=====================================Save ip start.");
            AdStaticData data = new AdStaticData();
            data.setCodeType("SYNC_RMP");
            data.setCodeValue(AD_CLOUD_IP);
            data.setCodeName("同步代码的ip地址");
            data.setSortId(1);
            data.setState("U");
            staticDataDAO.save(data);
            log.error("=====================================Save ip end.");
        }
    }
}
