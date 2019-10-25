package com.lhg1304.hyuckapp.page02.firemessenger.views;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.lhg1304.hyuckapp.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MessengerMainActivity extends AppCompatActivity {

    @BindView(R.id.messenger_tabs)
    TabLayout mTabLayout;

    @BindView(R.id.messenger_fab)
    FloatingActionButton mFab;

    @BindView(R.id.messenger_viewpager)
    ViewPager mViewPager;

    ViewPagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_02_messenger_main);
        ButterKnife.bind(this);
        mTabLayout.setupWithViewPager(mViewPager);
        setUpViewPager();
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment currentFragment = mPagerAdapter.getItem(mViewPager.getCurrentItem());
                if (currentFragment instanceof MessengerFriendFragment) {
                    ((MessengerFriendFragment) currentFragment).toggleSearchBar();
                } else {
                    // 친구 탭으로 이동
                    mViewPager.setCurrentItem(2, true);
                    // 체크박스가 보일 수 있도록 처리
                    MessengerFriendFragment friendFragment = (MessengerFriendFragment) mPagerAdapter.getItem(1);
                    friendFragment.toggleSelectionMode();
                }
            }
        });
    }

    private void setUpViewPager() {
        mPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mPagerAdapter.addFragment(new MessengerChatFragment(), "채팅");
        mPagerAdapter.addFragment(new MessengerFriendFragment(), "친구");
        mViewPager.setAdapter(mPagerAdapter);
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {

        private List<Fragment> fragmentList = new ArrayList<>();
        private List<String> fragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }
    }
}
