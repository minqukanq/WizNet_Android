package com.callor.hello.wiznet_android_views;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.callor.hello.wiznet_android_views.databinding.ActivityMainBinding;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.github.mikephil.charting.utils.ColorTemplate.rgb;

public class MainActivity extends AppCompatActivity implements OnChartValueSelectedListener {
    // URL 설정.

    NotificationManager Notifi_M;
    SharedPreferences setting;
    private ActivityMainBinding binding;
    // chart 글자용 폰트
    private Typeface mTfRegular;
    private Typeface mTfLight;
    private int pos = 0;
    // 화면갱신을 위한 Handler 설정
    // Handler를 이용하지 않고 Thread에서 화면 갱신을 시행하면 오류가 발생한다.
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            refreshDisplay();
        }
    };


    // 수동으로 데이터 업데이트와 화면 리플레시
    public void refreshDisplay() {

//        setData();
//            Toast.makeText(getApplicationContext(), (String) msg.obj, Toast.LENGTH_LONG).show();
            ArrayList<PieEntry> entries = new ArrayList<PieEntry>();

            /****************************************/
            // PieEntry : Pie 그래프를 그리기 위한 기본 데이터
            // new PieEntry(value,label,icon)
            PieEntry entry = new PieEntry(100, "",
                    ContextCompat.getDrawable(getApplicationContext(), R.drawable.star));

            Pm25VO pm25VO = Pm25VO.getInstance();
            int chartValue = 0;
            if (!pm25VO.mDataRead) return;

            String piTitle = "PM 2.5";
            // Pie chart의 색상
            int[] pmColor = {rgb("#FF0000"), rgb("#c5f7e7"), rgb("#e74c3c"), rgb("#3498db")};

            int pm25_value = 0;
            boolean pm25_warning = false;
            switch (pos % 3) {
                case 0:
                    piTitle = "PM 0.1";
                    pm25_value = Integer.parseInt(setting.getString("pm_scope_01", "50"));
                    chartValue = pm25VO.getPm2501();
                    if (chartValue >= pm25_value) pm25_warning = true;

                    pmColor = new int[]{rgb("#FF0000"), rgb("#c5f7e7"), rgb("#e74c3c"), rgb("#3498db")};
                    break;
                case 1:
                    piTitle = "PM 2.5";
                    pm25_value = Integer.parseInt(setting.getString("pm_scope_25", "50"));
                    chartValue = pm25VO.getPm2525();
                    if (chartValue >= pm25_value) pm25_warning = true;

                    pmColor = new int[]{rgb("#00FF00"), rgb("#c5f7e7"), rgb("#e74c3c"), rgb("#3498db")};
                    break;
                case 2:
                    piTitle = "PM 10";
                    pm25_value = Integer.parseInt(setting.getString("pm_scope_10", "80"));
                    chartValue = pm25VO.getPm2510();
                    if (chartValue >= pm25_value) pm25_warning = true;

                    pmColor = new int[]{rgb("#0000FF"), rgb("#c5f7e7"), rgb("#e74c3c"), rgb("#3498db")};
                    break;
            }
            if (pos++ > 10) pos = 0;

            if(setting.getBoolean("notifications_new_message",false) && pm25_warning)
                            pm25Alam(piTitle,Integer.toString(chartValue));

            if(pm25_warning) binding.txtWarning.setText(piTitle + " 미세먼지경고");
            else binding.txtWarning.setText("");
//            pm25Alam(piTitle, Integer.toString(pm25_value));

            entry = new PieEntry(chartValue, "" + chartValue + "㎍", ContextCompat.getDrawable(getApplicationContext(), R.drawable.star));
            entries.add(entry);
            entry = new PieEntry(120 - chartValue, "", ContextCompat.getDrawable(getApplicationContext(), R.drawable.star));
            entries.add(entry);

            PieDataSet dataSet = new PieDataSet(entries, "");

            dataSet.setDrawIcons(false);
            dataSet.setSliceSpace(3f);
            dataSet.setIconsOffset(new MPPointF(0, 40));
            dataSet.setSelectionShift(5f);

            ArrayList<Integer> colors = new ArrayList<Integer>();

            // Pie chart의 색상
//            int[] pmColor = {rgb("#FF0000"), rgb("#c5f7e7"), rgb("#e74c3c"), rgb("#3498db")};

            for (int c : pmColor) colors.add(c);

            colors.add(ColorTemplate.getHoloBlue());
            dataSet.setColors(colors);

            PieData data = new PieData(dataSet);

            // 데이터 값을 표시하지 않게
            // 데이터 값의 표시방법이 궁금하다. 어떤 연산을 하는지
            data.setDrawValues(false);
            binding.txtMessage.setText(pm25VO.getPM25_Date() + "/" + pm25VO.getPM25_Time());

            // 화면갱신을 위해 chart 데이터를
            binding.pm25Chart.setCenterText(generateCenterSpannableText(piTitle));
            binding.pm25Chart.setData(data);

            binding.pm25Chart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
            // undo all highlights
            binding.pm25Chart.highlightValues(null);
            binding.pm25Chart.invalidate();
    };


    private Runnable refresh = new Runnable() {
        @Override
        public void run() {
            while (true) {
                // 저장된 Preference 읽기
                Message msg = new Message();
                String strSleep = setting.getString("sync_refresh", "5");
                long longSleep = Long.parseLong(strSleep) * 1000;
                msg.obj = strSleep;
                handler.sendMessage(msg);
                try {
                    Thread.sleep(longSleep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };
    private boolean startService = true;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    if (!startService) {
                        Toast.makeText(MainActivity.this, "Start DB Get", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(MainActivity.this, ReadLastDB.class);
                        startService(intent);
                    } else {
                        Toast.makeText(MainActivity.this, "Stop DB Get", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(MainActivity.this, ReadLastDB.class);
                        stopService(intent);
                    }
                    startService = !startService;
                    return true;
                case R.id.navigation_dashboard:
//                    Toast.makeText (MainActivity.this,"dashboard",Toast.LENGTH_LONG).show();
                    refreshDisplay();
//                    setData();
                    return true;
                case R.id.navigation_notifications:
                    // 환경설정을 위해 Db 읽기 쓰레드를 잠시 중지한다
                    Intent intent = new Intent(MainActivity.this, ReadLastDB.class);
                    stopService(intent);

                    Intent setIntent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(setIntent);
                    return true;
            }
            return false;
        }

    };

    public void pm25Alam(String contentTitle, String contentText) {

//        Intent intent = new Intent(ReadLastDB.this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
//                 (MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(MainActivity.this);
//        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), android.R.drawable.star_on));
        builder.setSmallIcon(android.R.drawable.star_on);
        builder.setTicker(contentTitle);
        builder.setContentTitle("미세먼지경고! " + contentTitle + " : " +contentText + "㎍");

        Date data = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String curDate = simpleDateFormat.format(data);
//        builder.setContentText(curDate);

        String ct = curDate + " 현재";
        builder.setContentText(ct);
        builder.setWhen(System.currentTimeMillis());

        // 알림 팝업으로 띄우기
        builder.setPriority(Notification.PRIORITY_MAX);

        //소리추가
        builder.setDefaults(Notification.DEFAULT_VIBRATE);

        builder.setSound(Uri.parse(setting.getString("notifications_new_message_ringtone","무음")));

//            Notifi.defaults = Notification.DEFAULT_SOUND;
        //알림 소리를 한번만 내도록
//            Notifi.flags = Notification.FLAG_ONLY_ALERT_ONCE;

        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);
        //확인하면 자동으로 알림이 제거 되도록
//            Notifi.flags = Notification.FLAG_AUTO_CANCEL;

        Notifi_M = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notifi_M.notify(0, builder.build());

//            Notifi_M.notify(777, Notifi);

        //토스트 띄우기
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
//        setSupportActionBar(binding.myToolbar);

        // nav 버튼 활성화
        binding.navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mTfRegular = Typeface.createFromAsset(getAssets(), "12롯데마트행복Bold.ttf");
        mTfLight = Typeface.createFromAsset(getAssets(), "12롯데마트행복Light.ttf");


        Date data = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String curDate = simpleDateFormat.format(data);
        binding.txtMessage.setText("미세먼지 측정데이터 조회중입니다\n" +
                "잠시만 기다려 주세요..");

        // 설정파일 로딩
        setting = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());


        // DB 읽기 서비스 실행
        startService = true;
        Intent intent = new Intent(MainActivity.this, ReadLastDB.class);
        startService(intent);

        chartCreate();
        refreshDisplay();

        // 화면갱신 thread Start
        new Thread(refresh).start();

    }

    // 서버에서 새로운 정보를 받아들여 dashboard를 refresh 한다.


    @Override
    public void onValueSelected(Entry e, Highlight h) {
        if (e == null)
            return;
        Log.i("VAL SELECTED",
                "Value: " + e.getY() + ", index: " + h.getX()
                        + ", DataSet index: " + h.getDataSetIndex());
    }

    public void chartCreate() {

        binding.pm25Chart.setUsePercentValues(false);
        binding.pm25Chart.getDescription().setEnabled(false);
        binding.pm25Chart.setExtraOffsets(5, 10, 5, 5);

        binding.pm25Chart.setDragDecelerationFrictionCoef(0.95f);

        binding.pm25Chart.setCenterTextTypeface(mTfLight);
        binding.pm25Chart.setCenterText(generateCenterSpannableText());

        binding.pm25Chart.setDrawHoleEnabled(true);
        binding.pm25Chart.setHoleColor(Color.WHITE);

        binding.pm25Chart.setTransparentCircleColor(Color.WHITE);
        binding.pm25Chart.setTransparentCircleAlpha(110);

        binding.pm25Chart.setHoleRadius(58f);
        binding.pm25Chart.setTransparentCircleRadius(61f);

        // 파이 중심 글자 표시
        binding.pm25Chart.setDrawCenterText(true);

        binding.pm25Chart.setRotationAngle(180);

        // enable rotation of the chart by touch
        binding.pm25Chart.setRotationEnabled(true);
        binding.pm25Chart.setHighlightPerTapEnabled(true);

        // add a selection listener
        binding.pm25Chart.setOnChartValueSelectedListener(this);
        binding.pm25Chart.animateY(1400, Easing.EasingOption.EaseInOutQuad);


        // 설명문, 적요 표시부분 설정
        Legend l = binding.pm25Chart.getLegend();
        l.setEnabled(false); // 적요 텍스트 안보이게
        /*
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);
        */

        // entry label styling
        binding.pm25Chart.setEntryLabelColor(Color.WHITE);
        binding.pm25Chart.setEntryLabelTypeface(mTfRegular);
        binding.pm25Chart.setEntryLabelTextSize(20f);


    }

    private SpannableString generateCenterSpannableText() {
        return generateCenterSpannableText("PM 2.5㎛");
    }

    private SpannableString generateCenterSpannableText(String title) {

        String mainTitle = "◎ " + title + " ◎";
        int sizeMain = mainTitle.length();

        String subTitle = "by callor@callor.com";
        int sizeSub = subTitle.length();

        SpannableString s = new SpannableString(mainTitle + "\n" + subTitle);
        s.setSpan(new RelativeSizeSpan(2.0f), 0, sizeMain, 0);
        s.setSpan(new StyleSpan(Typeface.NORMAL), sizeMain, s.length() - (sizeMain + 1), 0);
        s.setSpan(new ForegroundColorSpan(Color.GRAY), sizeMain, s.length() - sizeMain, 0);

        // 19: 두번째 줄 글자수
        // 뒤에서부터 글자수를 세어서 모양을 이텔릭으로 바꾼다.
        // 숫자가 부족할경우 일부 글자모양이 이상해 진다.
        s.setSpan(new RelativeSizeSpan(.8f), sizeMain, s.length() - (sizeSub + 1), 0);
        s.setSpan(new StyleSpan(Typeface.ITALIC), s.length() - (sizeSub + 1), s.length(), 0);
        s.setSpan(new ForegroundColorSpan(ColorTemplate.getHoloBlue()), s.length() - sizeSub, s.length(), 0);

        return s;
    }


    @Override
    public void onNothingSelected() {
        Log.i("PieChart", "nothing selected");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        overridePendingTransition(R.anim.move_left_in_activity, R.anim.move_right_out_activity);
    }


}
