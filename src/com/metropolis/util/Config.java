package com.metropolis.util;


import org.joda.time.DateTime;

public class Config {

    public static final DateTime CACHE_EPOCH = new DateTime(2007, 5, 1, 9, 45);
    public static final DateTime CACHE_END = new DateTime(2016, 12, 25, 4, 30);

    public static final DateTime SIM_START_DATE = new DateTime(2015, 1, 1, 9, 45);
    public static final DateTime SIM_END_DATE = new DateTime(2015, 12, 29, 4, 30);

    public static boolean isProduction() {
        return false;
    }


}
