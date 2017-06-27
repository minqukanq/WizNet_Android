package com.callor.hello.wiznet_android_views;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by callor on 2017-06-23.
 */


public class ReadLastDB extends Service {
    boolean mQuit = false;
    SharedPreferences setting;

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
//            if (msg.what == 0) {

//            Toast.makeText(ReadLastDB.this, Pm25VO.DB_SERVER_URL, Toast.LENGTH_SHORT).show();
//            Toast.makeText(ReadLastDB.this, ss, Toast.LENGTH_SHORT).show();
//            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        setting = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mQuit = false;
        NewsThread newsThread = new NewsThread(this, handler);
        newsThread.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mQuit = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class NewsThread extends Thread {
        ReadLastDB parent;
        Handler handler;

        public NewsThread(ReadLastDB parent, Handler handler) {
            this.parent = parent;
            this.handler = handler;
        }


        public void run() {
            // android.os
            while (!mQuit) {
                Pm25VO pm25VO = Pm25VO.getInstance();

                RequestHttpURLConnection requestHttpURLConnection = new RequestHttpURLConnection();
                String response = new String(requestHttpURLConnection.request(Pm25VO.DB_SERVER_URL, null)); // 해당 URL로 부터 결과물을 얻어온다.

                try {
                    JSONObject responseJSON = new JSONObject(response);
                    pm25VO.setPM25_Date((String) (responseJSON.get("PM25_DATE")));
                    pm25VO.setPM25_Time((String) (responseJSON.get("PM25_TIME")));

                    pm25VO.setPm2501((int) (responseJSON.get("PM25_01")));
                    pm25VO.setPm2525((int) (responseJSON.get("PM25_25")));
                    pm25VO.setPm2510((int) (responseJSON.get("PM25_10")));

                    pm25VO.mDataRead = true;
                    handler.sendEmptyMessage(0);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // 저장된 Preference 읽기
                String strSleep = setting.getString("sync_frequency", "10");
                long longSleep = Long.parseLong(strSleep)*1000;

                try {
                    Thread.sleep(longSleep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

