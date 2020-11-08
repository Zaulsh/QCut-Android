package com.qcut.customer.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {

    public static String getDisplayWaitingTime(long min) {
        long hours = min / 60; //since both are ints, you get an int
        long minutes = min % 60;
        if(hours > 0){
            return hours+"hr "+minutes+" min";
        } else {
            return minutes+" min";
        }
    }

    public static String getTodayDDMMYYYY() {
        return new SimpleDateFormat("ddMMyyyy").format(new Date());
    }
}