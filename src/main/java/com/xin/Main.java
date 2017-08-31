package com.xin;

/**
 * @author linxixin@cvte.com
 * @version 1.0
 * @description
 */
public class Main {

    public static void main(String[] args) {
        System.out.println(getTime(30000));

    }

    public static String getTime(long millis) {
        if (millis < 1000) {
            return millis + "ms";
        } else {
            String misecondStr = String.valueOf(millis);
            return misecondStr.substring(0, misecondStr.length() - 3) + "," + misecondStr.substring(misecondStr.length() - 3);
        }
    }
}
