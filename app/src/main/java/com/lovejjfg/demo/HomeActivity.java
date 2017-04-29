package com.lovejjfg.demo;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.lovejjfg.demo.fragment.ItemFragment;
import com.lovejjfg.demo.fragment.MyFragment;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = HomeActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ViewPager mPager = (ViewPager) findViewById(R.id.vp);
        TabLayout mTab = (TabLayout) findViewById(R.id.tab);
        mPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                if (position == 0) {
                    return MyFragment.newInstance(null, "pos" + position);
                } else {
                    return ItemFragment.newInstance(1);
                }
            }

            @Override
            public int getCount() {
                return 3;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return "CircleBall";
            }
        });
        mTab.setupWithViewPager(mPager, true);
        setSupportActionBar(toolbar);

    }
}
