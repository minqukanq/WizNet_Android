package com.callor.hello.wiznet_android_views;

/**
 * Created by callor on 2017-06-25.
 * @author callor
 *
 * Db로부터 데이터를 가져오는 Thread와
 * 화면 갱신용 Thread에서 사용할 공용 VO 객체를 싱글톤으로 설정해 둔다
 *
 */

public class Pm25VO {

    public static final String DB_SERVER_URL = "http://callor.com:9999/pm25/getlast";
    public static boolean mDataRead = false;

    private static volatile Pm25VO singletonInstance = null;

    private String PM25_Date;
    private String PM25_Time;

    private int PM25_01;
    private int PM25_25;
    private int PM25_10;

    public static Pm25VO getInstance() {
        if (singletonInstance == null) {
            synchronized (Pm25VO.class) {
                if (singletonInstance == null) {
                    singletonInstance = new Pm25VO();
                }
            }
        }
        return singletonInstance;
    }

    public String getPM25_Date() {
        return PM25_Date;
    }

    public Pm25VO setPM25_Date(String PM25_Date) {
        this.PM25_Date = PM25_Date;
        return this;
    }

    public String getPM25_Time() {
        return PM25_Time;
    }

    public Pm25VO setPM25_Time(String PM25_Time) {
        this.PM25_Time = PM25_Time;
        return this;
    }

    public int getPm2501() {
        return PM25_01;
    }

    public void setPm2501(int pm2501) {
        PM25_01 = pm2501;
    }

    public int getPm2525() {
        return PM25_25;
    }

    public void setPm2525(int pm2525) {
        PM25_25 = pm2525;
    }

    public int getPm2510() {
        return PM25_10;
    }

    public void setPm2510(int pm2510) {
        PM25_10 = pm2510;
    }


}
