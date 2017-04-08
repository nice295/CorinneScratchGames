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

package com.nice295.scratchgames.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nice295.scratchgames.R;
import com.nice295.scratchgames.ShowWebView;
import com.nice295.scratchgames.model.BestItem;

import java.util.ArrayList;
import java.util.HashMap;

import io.paperdb.Paper;

public class BestFragment extends Fragment {
    private static final String TAG = "BestFragment";

    private ListView mLvMyItems;
    private ListViewAdapter mAdapter = null;
    private ArrayList<BestItem> mBestItemArray;

    private HashMap<String, String> mItems;

    private FirebaseAnalytics mFirebaseAnalytics;
    private DatabaseReference mDatabase;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout ll = (RelativeLayout) inflater.inflate(
                R.layout.fragment_best, container, false);



        //mBestItemArray = new ArrayList<BestItem>();
        mBestItemArray = Paper.book().read("best", new ArrayList<BestItem>());

        mLvMyItems = (ListView) ll.findViewById(R.id.lvBest);
        mAdapter = new ListViewAdapter(getActivity(), R.layout.layout_item_best, mBestItemArray);
        mLvMyItems.setAdapter(mAdapter);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
        mDatabase = FirebaseDatabase.getInstance().getReference();

        Query myTopPostsQuery = mDatabase.child("best").orderByChild("star");
        myTopPostsQuery.addValueEventListener(
                //mDatabase.child("my-items").addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mBestItemArray.clear();

                        if (dataSnapshot.getChildrenCount() != 0) {
                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                BestItem item = postSnapshot.getValue(BestItem.class);
                                mBestItemArray.add(item);
                            }
                            mAdapter.notifyDataSetChanged();

                            Paper.book().write("best", mBestItemArray);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "my-items:onCancelled", databaseError.toException());
                    }
                }
        );

        mLvMyItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), ShowWebView.class);
                BestItem item = mAdapter.getItem().get(i);

                intent.putExtra("id", item.getId());
                intent.putExtra("user", item.getUser());
                intent.putExtra("name", item.getName());

                startActivity(intent);
            }
        });

        return ll;
    }

    private class ListViewAdapter extends ArrayAdapter<BestItem> {
        private ArrayList<BestItem> items;

        public ListViewAdapter(Context context, int textViewResourceId, ArrayList<BestItem> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }

        public class ViewHolder {
            public ImageView ivPic;
            public ImageView ivProfile;
            public TextView tvName;
            public TextView tvDesc;
            public TextView tvUser;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ListViewAdapter.ViewHolder viewHolder;

            Log.d(TAG, "position: " + position);

            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.layout_item_best, parent, false);

                viewHolder = new ListViewAdapter.ViewHolder();
                viewHolder.ivPic = (ImageView) convertView.findViewById(R.id.ivPic);
                viewHolder.ivProfile = (ImageView) convertView.findViewById(R.id.ivProfile);
                viewHolder.tvName = (TextView) convertView.findViewById(R.id.tvName);
                viewHolder.tvDesc = (TextView) convertView.findViewById(R.id.tvDesc);
                viewHolder.tvUser = (TextView) convertView.findViewById(R.id.tvUser);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ListViewAdapter.ViewHolder) convertView.getTag();
            }

            BestItem item = items.get(position);
            Log.d(TAG, "Name: " + item.getName());
            Log.d(TAG, "URL: " + item.getImageUrl());
            if (item != null) {
                Glide.with(getActivity())
                        .load(item.getImageUrl())
                        .into(viewHolder.ivPic);

                Glide.with(getActivity())
                        .load(item.getImageProfile())
                        .into(viewHolder.ivProfile);

                viewHolder.tvName.setText(item.getName());
                viewHolder.tvDesc.setText(item.getDesc());
                viewHolder.tvUser.setText(item.getUser());
            }
            return convertView;
        }

        public ArrayList<BestItem> getItem() {
            return this.items;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
