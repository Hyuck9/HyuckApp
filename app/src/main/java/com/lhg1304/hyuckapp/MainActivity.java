package com.lhg1304.hyuckapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.lhg1304.hyuckapp.common.util.BackPressCloseUtil;
import com.lhg1304.hyuckapp.page01.baas_test.BaasTestActivity;
import com.lhg1304.hyuckapp.page01.google_map.GoogleMapActivity;
import com.lhg1304.hyuckapp.page01.my_location.MyLocationActivity;
import com.lhg1304.hyuckapp.page01.tmap.TMapActivity;
import com.lhg1304.hyuckapp.page02.firebase.FirebaseAuthActivity;
import com.lhg1304.hyuckapp.page02.firemessenger.views.MessengerLoginActivity;

public class MainActivity extends AppCompatActivity {

    private ViewPager mPager;
    private BackPressCloseUtil mBackPressUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();
    }

    /**
     * 초기화
     * */
    private void initialize() {
        mBackPressUtil = new BackPressCloseUtil(this);
        mPager = (ViewPager) findViewById(R.id.main_view_pager);
        mPager.setAdapter(new ViewPagerAdapter(this));

        // 해시키 확인 코드
        /*try {
            PackageInfo info = getPackageManager().getPackageInfo("com.lhg1304.hyuckapp", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }*/
    }

    /**
     * 첫번째 페이지 버튼
     * */
    public void mOnClick01(View v) {
        switch (v.getId()) {
            case R.id.btn_main_01:
                openActivity(MyLocationActivity.class);
                break;
            case R.id.btn_main_02:
                openActivity(GoogleMapActivity.class);
                break;
            case R.id.btn_main_03:
                openActivity(TMapActivity.class);
                break;
            case R.id.btn_main_04:
                openActivity(BaasTestActivity.class);
                break;
        }
    }

    /**
     * 두번째 페이지 버튼
     * */
    public void mOnClick02(View v) {
        switch (v.getId()) {
            case R.id.btn_main_16:
                openActivity(FirebaseAuthActivity.class);
                break;
            case R.id.btn_main_17:
                openActivity(MessengerLoginActivity.class);
                break;
        }
    }

    /**
     * Activity 열기
     * */
    private void openActivity(Class<?> activity) {
        startActivity(new Intent(this, activity));
    }

    @Override
    public void onBackPressed() {
        mBackPressUtil.onBackPressed();
    }

    private class ViewPagerAdapter extends PagerAdapter {

        private LayoutInflater mLayoutInflater;

        public ViewPagerAdapter(Context context) {
            super();
            mLayoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = null;

            if (position == 0) {
                view = mLayoutInflater.inflate(R.layout.page_01, null);
            } else {
                view = mLayoutInflater.inflate(R.layout.page_02, null);
            }
            container.addView(view, 0);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }
}
