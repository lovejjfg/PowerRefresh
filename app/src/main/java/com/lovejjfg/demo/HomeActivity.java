/*
 * Copyright (c) 2017.  Joe
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lovejjfg.demo;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.lovejjfg.demo.fragment.LoadMoreFragment;
import com.lovejjfg.demo.fragment.NestedScrollFragment;
import com.lovejjfg.demo.fragment.RecycleFragment;
import com.lovejjfg.demo.fragment.ScrollFragment;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = HomeActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ViewPager mPager = (ViewPager) findViewById(R.id.vp);
        TabLayout mTab = (TabLayout) findViewById(R.id.tab);
        final ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(NestedScrollFragment.newInstance(null, "pos" + 1));
        fragments.add(ScrollFragment.newInstance("xx"));
        fragments.add(new LoadMoreFragment());
        fragments.add(RecycleFragment.newInstance(2));
        mPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return fragments.get(position);
            }

            @Override
            public int getCount() {
                return fragments.size();
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        return "NestedScroll";
                    case 1:
                        return "ScrollView";
                    case 2:
                        return "LoadMore";
                    case 3:
                        return "RecycleView";
                }
                return null;
            }
        });
        mTab.setupWithViewPager(mPager, true);
        setSupportActionBar(toolbar);

    }
}
