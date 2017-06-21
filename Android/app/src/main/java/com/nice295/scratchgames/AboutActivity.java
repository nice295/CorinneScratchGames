/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nice295.scratchgames;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nice295.scratchgames.fragment.BestFragment;
import com.nice295.scratchgames.fragment.ShowRoomFragment;

import java.util.ArrayList;
import java.util.List;

import de.cketti.mailto.EmailIntentBuilder;
import io.paperdb.Paper;

/**
 * TODO
 */
public class AboutActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";

    private FirebaseAnalytics mFirebaseAnalytics;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private TextView mTvVersion;
    private TextView mTvVersionGuide;
    private Button mBtnUpgrade;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        mTvVersion = (TextView)findViewById(R.id.tvVersion);
        mTvVersionGuide = (TextView)findViewById(R.id.tvVersionGuide);
        mBtnUpgrade = (Button)findViewById(R.id.btnUpgrade);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Paper.init(this);
        String newVersion = Paper.book().read("version");
        String currentVersion = BuildConfig.VERSION_NAME;

        mTvVersion.setText(String.format(getString(R.string.version), currentVersion));
        if (currentVersion.equals(newVersion)) {
            mTvVersionGuide.setText(getString(R.string.current_is_newest));
            mBtnUpgrade.setVisibility(View.GONE);
        }
        else {
            mTvVersionGuide.setText(getString(R.string.Tap_upgrade));
            mBtnUpgrade.setOnClickListener(this);
        }

        Paper.init(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    public void onClick(View view) {
        if (view == mBtnUpgrade) {
            final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }
        }
    }
}
