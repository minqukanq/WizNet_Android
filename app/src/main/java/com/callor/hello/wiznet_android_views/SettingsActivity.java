package com.callor.hello.wiznet_android_views;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.view.MenuItem;

import java.util.List;

public class SettingsActivity extends android.preference.PreferenceActivity {
    /**
     * 환경 설정의 요약을 갱신하는 환경 설정 값 변경 리스너 새로운 값을 반영합니다.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener
            = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // 목록 환경 설정의 경우 올바른 표시 값을
                // 환경 설정의 「엔트리」리스트로 보여줍니다.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // 새 값을 반영하도록 요약을 설정하십시오.
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);

            } else if (preference instanceof RingtonePreference) {
                // 벨소리 환경 설정의 경우 RingtoneManager를 사용하여.
                // 올바른 표시 값을 찾으십시오
                if (TextUtils.isEmpty(stringValue)) {
                    // 빈 값은 '무음'(벨소리 없음)에 해당합니다.
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // 조회 오류가있는 경우 요약을 지우십시오.
                        preference.setSummary(null);
                    } else {
                        // 새 벨소리 표시 이름을 반영하도록 요약을 설정하십시오.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else {
                // 다른 모든 환경 설정의 경우 요약을 값의 간단한 문자열 표현으로 설정하십시오.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * 도우미 메서드를 사용하여 장치에 초대형 화면이 있는지 확인합니다. 예를 들어, 10 인치 태블릿은 초대형입니다.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * preference의 summary에 값을 바인딩한다.
     * 보다 구체적으로, 기호의 값이 변경되면 기호 (기호 제목 아래의 텍스트 행)가 값을 반영하도록 갱신됩니다.
     * 이 메소드를 호출하면 요약도 즉시 업데이트됩니다. 정확한 표시 형식은 선호 유형에 따라 다릅니다.
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {

        // 값 변경을 감시하는 Listener를 설정합니다.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // preference의 현재 값을 사용하여 리스너를 즉시 트리거합니다.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
//        addPreferencesFromResource(R.xml.pref_headers);
        // setting Activity Action bar의 위(뒤)로 가기 버튼 활성화
//        ActionBar ab = getSupportActionBar();
//        ab.setDisplayHomeAsUpEnabled(true);
//    }
    }


    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * res/xml/pref_headres.xml 파일로 부터 페이지 첫 메뉴를 생성한다.
     */
    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * Code Injection 공격대비 함수
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || SettingsFragment.class.getName().equals(fragmentName)
                || DataSyncPreferenceFragment.class.getName().equals(fragmentName)
                || NotificationPreferenceFragment.class.getName().equals(fragmentName)
                || RefreshSyncPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * PreferenceFragment를 상속받아 각각의 항목 구성을 실행
     */
    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // pref_general.xml 파일로 부터 서브 메뉴 설정
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            // 업데이트 이벤트 설정
            // EditText / List / Dialog / Ringtone 환경 설정의 summary를 해당 값에 바인딩합니다.
            // 값이 변경되면 Android 디자인 가이드 라인에 따라 새로운 값을 반영하도록 summary가 표시됩니다.
            bindPreferenceSummaryToValue(findPreference("general_list"));
        }

        //  General Sub Menu에서 뒤(위)로 가기 버튼 이벤트 설정
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
//            Toast.makeText(getActivity().getApplicationContext(),"메롱",Toast.LENGTH_SHORT).show();
            int id = item.getItemId();
            if (id == android.R.id.home) {
                // 현재 액티비티 종료하고 이전 메뉴로 돌아가기
                // 하드웨어 뒤로 가기 버튼과 호환성을 위해 설정
                getActivity().finish();


//                Intent homeIntent = new Intent(getActivity().getParent(), SettingsActivity.class);
//                homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                startActivity(homeIntent);
//                startActivity(new Intent(getActivity(), SettingsActivity.class));
//                moveTaskToBack(true);
//                return true;

            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * 이 부분은 알림 환경 설정 만 보여줍니다.
     * 이 값은 액티비티가 두 개의 창 설정 UI를 보여줍니다.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    public static class NotificationPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notification);
            setHasOptionsMenu(true);

            // EditText / List / Dialog / Ringtone 환경 설정의 요약을 해당 값에 바인딩하십시오.
            // 값이 변경되면 Android 디자인 가이드 라인에 따라 새로운 값을 반영하도록 요약이 업데이트됩니다.
            bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
            bindPreferenceSummaryToValue(findPreference("pm_scope_10"));
            bindPreferenceSummaryToValue(findPreference("pm_scope_25"));
            bindPreferenceSummaryToValue(findPreference("pm_scope_01"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                getActivity().finish();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * 이 부분은 데이터 및 동기화 환경 설정 만 보여줍니다. 액티비티가 두 개의 창 설정 UI를 표시 할 때 사용됩니다.
     */

    public static class DataSyncPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_data_sync);
            setHasOptionsMenu(true);

            // EditText / List / Dialog / Ringtone 환경 설정의 요약을 해당 값에 바인딩하십시오.
            // 값이 변경되면 Android 디자인 가이드 라인에 따라 새로운 값을 반영하도록 요약이 업데이트됩니다.
            bindPreferenceSummaryToValue(findPreference("sync_frequency"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                getActivity().finish();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    public static class RefreshSyncPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_refresh_sync);
            setHasOptionsMenu(true);

            // EditText / List / Dialog / Ringtone 환경 설정의 요약을 해당 값에 바인딩하십시오.
            // 값이 변경되면 Android 디자인 가이드 라인에 따라 새로운 값을 반영하도록 요약이 업데이트됩니다.
            bindPreferenceSummaryToValue(findPreference("sync_refresh"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                getActivity().finish();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

}
