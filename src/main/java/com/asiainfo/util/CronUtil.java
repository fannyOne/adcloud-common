package com.asiainfo.util;

import org.quartz.CronExpression;
import org.springframework.scheduling.support.CronSequenceGenerator;

import java.text.ParseException;
import java.util.Date;

/**
 * Created by liusteven on 17/5/26.
 */
public class CronUtil {

    public static boolean checkCron(String cron) throws Exception{
        try{
            if (getNextDate(cron,new Date(0)).getTime() > 0) {
                return true;
            }
        }catch (ParseException e){
            return false;
        }
        return false;
    }

    public static Date getNextDate(String cron, Date date) throws ParseException{
        CronExpression cronExpression = new CronExpression(cron);
        return cronExpression.getNextValidTimeAfter(date);
    }
}
