package com.zynick.commons;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * a very simple logger prefixed with date.
 * @author zynick
 */
public enum Logger {
    INSTANCE;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Date date = new Date();

    public void log(String message) {
        date.setTime(System.currentTimeMillis());
        System.out.println(sdf.format(date) + " " + message);
    }
}
