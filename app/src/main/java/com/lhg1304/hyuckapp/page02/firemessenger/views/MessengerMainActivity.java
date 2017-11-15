package com.lhg1304.hyuckapp.page02.firemessenger.views;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

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
