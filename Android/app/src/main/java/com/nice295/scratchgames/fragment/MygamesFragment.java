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
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.getbase.floatingactionbutton.AddFloatingActionButton;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nice295.scratchgames.Event.EventMygames;
import com.nice295.scratchgames.Event.EventShowroom;
import com.nice295.scratchgames.R;
import com.nice295.scratchgames.ShowWebView;
import com.nice295.scratchgames.model.ShowRoomExtItem;
import com.nice295.scratchgames.model.ShowRoomItem;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import io.paperdb.Paper;

public class MygamesFragment extends Fragment {
    private static final String TAG = "MygamesFragment";

    private TextView mTvNoGames;
    private ListView mLvMyItems;
    private ListViewAdapter mAdapter = null;
    private ArrayList<ShowRoomItem> mMyItemArray;
    private HashMap<String, ShowRoomExtItem> mMyItemExtHash;

    private FirebaseAnalytics mFirebaseAnalytics;
    private DatabaseReference mDatabase;

    private String mAndroidId = null;

    private LinkedList<String> mListLikes;

    private DataSnapshot mLastdataSnapshot = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FrameLayout ll = (FrameLayout) inflater.inflate(
                R.layout.fragment_mygames, container, false);

        //mMyItemArray = new ArrayList<ShowRoomItem>();
        mMyItemArray = Paper.book().read("_showroom", new ArrayList<ShowRoomItem>());
        mMyItemExtHash = Paper.book().read("_showroom-ext", new HashMap<String, ShowRoomExtItem>());
        mListLikes = Paper.book().read("likes", new LinkedList<String>());

        Log.d(TAG, "mMyItemArray count: " + mMyItemArray.size());

        mTvNoGames = (TextView) ll.findViewById(R.id.tvNoGames);
        mLvMyItems = (ListView) ll.findViewById(R.id.lvShowroom);
        mAdapter = new ListViewAdapter(getActivity(), R.layout.layout_item_list_item, mMyItemArray);
        mLvMyItems.setAdapter(mAdapter);

        EventBus.getDefault().post( new EventMygames(mListLikes.size()));

        /* 170620
        final View header = getActivity().getLayoutInflater().inflate(R.layout.layout_showroom_header, null, false);
        mLvMyItems.addHeaderView(header);
        */

        final View footer = getActivity().getLayoutInflater().inflate(R.layout.layout_item_list_item_ad, null, false);
        mLvMyItems.addFooterView(footer, null, false);
        NativeExpressAdView adView = (NativeExpressAdView) footer.findViewById(R.id.adView);
        AdRequest request = new AdRequest.Builder().build();
        adView.loadAd(request);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        mDatabase = FirebaseDatabase.getInstance().getReference();

        Query showroomExtQuery = mDatabase.child("showroom-ext");
        showroomExtQuery.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mMyItemExtHash.clear();

                        if (dataSnapshot.getChildrenCount() != 0) {
                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                ShowRoomExtItem item = postSnapshot.getValue(ShowRoomExtItem.class);
                                Log.d(TAG, "postSnapshot.getKey(): " + postSnapshot.getKey());
                                Log.d(TAG, "viewCount: " + item.getViewCount());

                                mMyItemExtHash.put(postSnapshot.getKey(), item);
                            }

                            mAdapter.notifyDataSetChanged();
                            EventBus.getDefault().post( new EventMygames(mAdapter.getCount()));

                            Paper.book().write("_showroom-ext", mMyItemExtHash);
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "showroom-ext:onCancelled", databaseError.toException());
                    }
                }
        );

        Query myTopPostsQuery = mDatabase.child("showroom").orderByChild("name");
        myTopPostsQuery.addValueEventListener(
                //mDatabase.child("my-items").addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mMyItemArray.clear();

                        if (dataSnapshot.getChildrenCount() != 0) {
                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                ShowRoomItem item = postSnapshot.getValue(ShowRoomItem.class);
                                if (mListLikes.contains(item.getId())) {
                                    mMyItemArray.add(item);
                                }
                            }

                            mAdapter.notifyDataSetChanged();
                            EventBus.getDefault().post( new EventMygames(mAdapter.getCount()));

                            Paper.book().write("_showroom", mMyItemArray);
                        }
                        mLastdataSnapshot = dataSnapshot;
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

                ShowRoomItem item = mAdapter.getItem().get(i);

                intent.putExtra("id", item.getId());
                intent.putExtra("user", item.getUser());
                intent.putExtra("name", item.getName());

                // Add view count
                ShowRoomExtItem showRoomExtItem = mMyItemExtHash.get(item.getId());
                if (showRoomExtItem != null) {
                    increaseViewCount(item.getId(), showRoomExtItem.getViewCount());
                }

                startActivity(intent);
            }
        });

        mAndroidId = Settings.Secure.getString(getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
        Log.d(TAG, "ANDROID_ID: " + mAndroidId);

        /* Later
        mLvMyItems.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long id) {

                ShowRoomItem item = mAdapter.getItem().get(position);
                ShowRoomExtItem showRoomExtItem = mMyItemExtHash.get(item.getId());

                if (mAndroidId != null &&
                        ( mAndroidId.equals("3aebba2c123546da") || mAndroidId.equals(showRoomExtItem.getReserved1()) )) {

                    showRemoveDialog(item.getId());

                    return true;
                }

                return false;
            }
        });
        */

        return ll;
    }


    private class ListViewAdapter extends ArrayAdapter<ShowRoomItem> {
        private ArrayList<ShowRoomItem> items;

        public ListViewAdapter(Context context, int textViewResourceId, ArrayList<ShowRoomItem> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }

        public class ViewHolder {
            public ImageView ivPic;
            public ImageView ivView;
            public TextView tvName;
            public TextView tvUser;
            public TextView tvViewCount;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ListViewAdapter.ViewHolder viewHolder;

            Log.d(TAG, "position: " + position);

            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.layout_item_list_item, parent, false);

                viewHolder = new ListViewAdapter.ViewHolder();
                viewHolder.ivPic = (ImageView) convertView.findViewById(R.id.ivPic);
                viewHolder.tvName = (TextView) convertView.findViewById(R.id.tvName);
                viewHolder.tvUser = (TextView) convertView.findViewById(R.id.tvUser);
                viewHolder.tvViewCount = (TextView) convertView.findViewById(R.id.tvViewCount);
                viewHolder.ivView = (ImageView) convertView.findViewById(R.id.ivView);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ListViewAdapter.ViewHolder) convertView.getTag();
            }

            ShowRoomItem item = items.get(position);
            Log.d(TAG, "Name: " + item.getName());
            Log.d(TAG, "URL: " + item.getImageUrl());
            if (mListLikes.contains(item.getId()) && item != null) {
                Glide.with(getActivity())
                        .load(item.getImageUrl())
                        .into(viewHolder.ivPic);

                viewHolder.tvName.setText(item.getName());
                viewHolder.tvUser.setText(item.getUser());

                ShowRoomExtItem showRoomExtItem = mMyItemExtHash.get(item.getId());
                if (showRoomExtItem != null) {
                    viewHolder.tvViewCount.setText(String.valueOf(showRoomExtItem.getViewCount()));

                    viewHolder.ivView.setVisibility(View.VISIBLE);
                    viewHolder.tvViewCount.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.ivView.setVisibility(View.GONE);
                    viewHolder.tvViewCount.setVisibility(View.GONE);
                }

                return convertView;
            }
            else {
                return null;
            }
        }

        public ArrayList<ShowRoomItem> getItem() {
            return this.items;
        }
    }

    @Override
    public void onStart() {
        setUserVisibleHint(true);
        super.onStart();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        mListLikes = Paper.book().read("likes", new LinkedList<String>());

        if (mAdapter != null && isVisibleToUser) {
            Log.d(TAG, "onResume: Like count: " + mListLikes.size());

            mAdapter.clear();

            if (mLastdataSnapshot != null && mLastdataSnapshot.getChildrenCount() != 0) {
                for (DataSnapshot postSnapshot : mLastdataSnapshot.getChildren()) {
                    ShowRoomItem item = postSnapshot.getValue(ShowRoomItem.class);
                    if (mListLikes.contains(item.getId())) {
                        mMyItemArray.add(item);
                    }
                }
            }

            mAdapter.notifyDataSetChanged();
            EventBus.getDefault().post( new EventMygames(mAdapter.getCount()));
        }
        else {
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.add) {
            showAddDialog();
        }
        return true;
    }

    private void showRemoveDialog(final String removeId) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.DialogTheme);
        dialogBuilder.setMessage(getString(R.string.removefrom_mygame));
        dialogBuilder.setPositiveButton(getString(R.string.remove), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mListLikes.remove(removeId);
                Paper.book().write("likes", mListLikes);
            }
        });

        dialogBuilder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int whichButton) {
                dialogInterface.cancel();
            }
        });

        final AlertDialog b = dialogBuilder.create();
        b.show();
    }

    private void showAddDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.DialogTheme);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add, null);
        dialogBuilder.setView(dialogView);

        final EditText editId = (EditText) dialogView.findViewById(R.id.editId);
        final EditText editName = (EditText) dialogView.findViewById(R.id.editName);
        final EditText editUser = (EditText) dialogView.findViewById(R.id.editUser);

        dialogBuilder.setTitle(getString(R.string.add_you_game_title));
        dialogBuilder.setMessage(getString(R.string.add_you_game_message));
        dialogBuilder.setPositiveButton(getString(R.string.add), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (editId.getText() != null && editName.getText() != null && editUser.getText() != null) {


                    addNewMyItem(editId.getText().toString(),
                            editName.getText().toString(),
                            editUser.getText().toString(),
                            "https://cdn2.scratch.mit.edu/get_image/project/" + editId.getText() + "_282x210.png");

                    addNewMyItemExt(editId.getText().toString());

                    editId.clearFocus();
                    editId.setText("");
                    editName.clearFocus();
                    editName.setText("");
                    editUser.clearFocus();
                    editUser.setText("");

                    getActivity().getWindow().setSoftInputMode(
                            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

                    Toast.makeText(getActivity(), getString(R.string.added_your_game), Toast.LENGTH_SHORT).show();

                } else {
                    Log.e(TAG, "Input Text!");
                }
            }
        });

        dialogBuilder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });


        final AlertDialog b = dialogBuilder.create();
        b.show();

        // Initially disable the button
        ((AlertDialog) b).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

        // Now set the textchange listener for edittext
        editName.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Check if edittext is empty
                if (editId.getText().length() != 9 || TextUtils.getTrimmedLength(s) < 3) {
                    // Disable ok button
                    ((AlertDialog) b).getButton(
                            AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    // Something into edit text. Enable the button.
                    ((AlertDialog) b).getButton(
                            AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }

            }
        });

        // Now set the textchange listener for edittext
        editId.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Check if edittext is empty
                if (editName.getText().length() < 3 || TextUtils.getTrimmedLength(s) != 9) {
                    // Disable ok button
                    ((AlertDialog) b).getButton(
                            AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    // Something into edit text. Enable the button.
                    ((AlertDialog) b).getButton(
                            AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }

            }
        });
    }

    private void addNewMyItem(String id, String name, String user, String imageUrl) {
        ShowRoomItem item = new ShowRoomItem(id, name, user, imageUrl);
        mDatabase.child("showroom").child(id).setValue(item);
    }

    private void addNewMyItemExt(String id) {
        ShowRoomExtItem item = new ShowRoomExtItem(id, 0, "deviceId", mAndroidId, "reserved2");
        mDatabase.child("showroom-ext").child(id).setValue(item);
    }

    private void increaseViewCount(String id, int viewCount) {
        mDatabase.child("showroom-ext").child(id).child("viewCount").setValue(viewCount + 1);
    }
}
