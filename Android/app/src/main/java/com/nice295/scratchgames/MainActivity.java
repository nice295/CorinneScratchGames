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

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nice295.scratchgames.Event.EventMygames;
import com.nice295.scratchgames.Event.EventShowroom;
import com.nice295.scratchgames.fragment.BestFragment;
import com.nice295.scratchgames.fragment.MygamesFragment;
import com.nice295.scratchgames.fragment.ShowRoomFragment;
import com.nice295.scratchgames.model.ShowRoomExtItem;
import com.nice295.scratchgames.model.ShowRoomItem;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import de.cketti.mailto.EmailIntentBuilder;
import io.paperdb.Paper;

/**
 * TODO
 */
public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";
    private TabLayout mTabLayout;
    private FloatingActionsMenu mFloatingActionsMenu;

    private FirebaseAnalytics mFirebaseAnalytics;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private static final int RC_OCR_CAPTURE = 9003;

    private ArrayList<ShowRoomItem> mMyItemArray;
    private LinkedList<String> mListLikes;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventShowroom(EventShowroom event) {
        Log.d(TAG, "onShowroomCountEvent: "  + event.count);
        updateTabTitle(1, getString(R.string.showroom) + "(" + event.count + ")");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMygames(EventMygames event) {
        Log.d(TAG, "onShowroomCountEvent: "  + event.count);
        updateTabTitle(2, getString(R.string.mygames) + "(" + event.count + ")");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, "ca-app-pub-9553464645775660~6069496836");

        mMyItemArray = Paper.book().read("showroom", new ArrayList<ShowRoomItem>());
        mListLikes = Paper.book().read("likes", new LinkedList<String>());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        if (viewPager != null) {
            setupViewPager(viewPager);
        }

        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(viewPager);

        /*
        // Change font for tab titles
        Typeface mTypeface = Typeface.createFromAsset(getAssets(), "scr.ttf");
        ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
        int tabsCount = vg.getChildCount();
        for (int j = 0; j < tabsCount; j++) {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
            int tabChildsCount = vgTab.getChildCount();
            for (int i = 0; i < tabChildsCount; i++) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    ((TextView) tabViewChild).setTypeface(mTypeface, Typeface.NORMAL);
                }
            }
        }
        */

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        /* v1.5: Do not use anymore
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInAnonymously:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInAnonymously:failure", task.getException());
                            //updateUI(null);
                        }
                    }
                });
        */

        Paper.init(this);

        Query showroomExtQuery = mDatabase.child("version");
        showroomExtQuery.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String version = dataSnapshot.getValue(String.class);
                        Paper.book().write("version", version);
                        Log.w(TAG, "version: " + version);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "version:onCancelled", databaseError.toException());
                    }
                }
        );


    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_contact) {
            sendEmail();
            return true;
        }
        else if (id == R.id.action_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);

            return true;
        }
        /*
        else if (id == R.id.action_community) {
            Intent facebookIntent = new Intent(Intent.ACTION_VIEW);
            String facebookUrl = getFacebookPageURL(this);
            facebookIntent.setData(Uri.parse(facebookUrl));
            startActivity(facebookIntent);
        }
        */
        return super.onOptionsItemSelected(item);
    }

    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getSupportFragmentManager());
        adapter.addFragment(new BestFragment(), getString(R.string.best));
        adapter.addFragment(new ShowRoomFragment(), getString(R.string.showroom)+ "(" + mMyItemArray.size() + ")");
        adapter.addFragment(new MygamesFragment(), getString(R.string.mygames) + "(" + mListLikes.size() + ")");
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(0);
    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }

    private void sendEmail() {
        EmailIntentBuilder.from(this)
                .to("nice295@gmail.com")
                .subject(getString(R.string.emailTitle))
                .body(getString(R.string.emailDesc))
                .start();
    }

    private void updateTabTitle(int position, String title) {
        mTabLayout.getTabAt(position).setText(title);
    }

    public String getFacebookPageURL(Context context) {
        String FACEBOOK_URL = "https://www.facebook.com/groups/1348800195234630/";
        String FACEBOOK_PAGE_ID = "1348800195234630";

        PackageManager packageManager = context.getPackageManager();
        try {
            int versionCode = packageManager.getPackageInfo("com.facebook.katana", 0).versionCode;
            if (versionCode >= 3002850) { //newer versions of fb app
                return "fb://facewebmodal/f?href=" + FACEBOOK_URL;
            } else { //older versions of fb app
                return "fb://group/" + FACEBOOK_PAGE_ID;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return FACEBOOK_URL; //normal web url
        }
    }
}
