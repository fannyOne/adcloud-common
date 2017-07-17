package com.asiainfo.schedule.sync;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by YangRY on 2016/6/30 0030.
 */
@Component
@Profile("loop")
public class SpringBootSchedule {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    //    @Scheduled(fixedDelay = 5000)
    public void joker() {
        System.out.println("Joker:有些事，我都已忘记！" + sdf.format(new Date()));
    }

    //    @Scheduled(fixedRate = 5000)
    public void batman() {
        System.out.println("Batman:I'm Batman!" + sdf.format(new Date()));
    }

    //    @Scheduled(cron = "*/5 * * * * *")
    public void superMan() {
        System.out.println("SuperMan:No fight!" + sdf.format(new Date()));
    }
}
