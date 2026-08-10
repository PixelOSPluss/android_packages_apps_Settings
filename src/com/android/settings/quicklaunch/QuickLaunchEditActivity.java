/*
 * Copyright (C) 2026 Lunaris-AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.quicklaunch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.android.settings.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class QuickLaunchEditActivity extends AppCompatActivity {

    private ImageView mBtnClose;
    private ImageView mBtnSave;
    private TabLayout mTabLayout;
    private ViewPager2 mViewPager;

    private ImageView[] mSlotIcons;
    private ImageView[] mSlotRemoveBtns;

    private final List<String> mSelectedPackages = new ArrayList<>();
    private static final int MAX_SLOTS = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quick_launch_edit_activity);

        mBtnClose = findViewById(R.id.btn_close);
        mBtnSave = findViewById(R.id.btn_save);
        mTabLayout = findViewById(R.id.tab_layout);
        mViewPager = findViewById(R.id.edit_viewpager);

        mSlotIcons = new ImageView[]{
                findViewById(R.id.edit_slot_center_icon),
                findViewById(R.id.edit_slot_top_icon),
                findViewById(R.id.edit_slot_left_icon),
                findViewById(R.id.edit_slot_right_icon)
        };

        mSlotRemoveBtns = new ImageView[]{
                findViewById(R.id.edit_slot_center_remove),
                findViewById(R.id.edit_slot_top_remove),
                findViewById(R.id.edit_slot_left_remove),
                findViewById(R.id.edit_slot_right_remove)
        };

        loadSavedSlots();

        mBtnClose.setOnClickListener(v -> finish());
        mBtnSave.setOnClickListener(v -> saveSlotsAndFinish());

        for (int i = 0; i < mSlotRemoveBtns.length; i++) {
            final int index = i;
            if (mSlotRemoveBtns[i] != null) {
                mSlotRemoveBtns[i].setOnClickListener(v -> removeSlotAt(index));
            }
        }

        mViewPager.setAdapter(new EditPagerAdapter(this));
        new TabLayoutMediator(mTabLayout, mViewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText(R.string.quick_launch_tab_functions);
            } else {
                tab.setText(R.string.quick_launch_tab_apps);
            }
        }).attach();

        updateSlotsUI();
    }

    private void loadSavedSlots() {
        mSelectedPackages.clear();
        String saved = Settings.Secure.getString(getContentResolver(),
                QuickLaunchSettings.QUICK_LAUNCH_SLOTS);
        if (saved != null && !saved.isEmpty()) {
            mSelectedPackages.addAll(Arrays.asList(saved.split(",")));
        }
    }

    private void saveSlotsAndFinish() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mSelectedPackages.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(mSelectedPackages.get(i));
        }
        Settings.Secure.putString(getContentResolver(),
                QuickLaunchSettings.QUICK_LAUNCH_SLOTS, sb.toString());
        finish();
    }

    private void removeSlotAt(int index) {
        if (index >= 0 && index < mSelectedPackages.size()) {
            mSelectedPackages.remove(index);
            updateSlotsUI();
        }
    }

    public void addPackageToSlots(String pkgName) {
        if (mSelectedPackages.contains(pkgName)) {
            Toast.makeText(this, "Already added", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mSelectedPackages.size() >= MAX_SLOTS) {
            Toast.makeText(this, "Maximum 5 items allowed", Toast.LENGTH_SHORT).show();
            return;
        }
        mSelectedPackages.add(pkgName);
        updateSlotsUI();
    }

    private void updateSlotsUI() {
        PackageManager pm = getPackageManager();

        for (int i = 0; i < mSlotIcons.length; i++) {
            if (i < mSelectedPackages.size()) {
                try {
                    Drawable icon = pm.getApplicationIcon(mSelectedPackages.get(i));
                    mSlotIcons[i].setImageDrawable(icon);
                    mSlotRemoveBtns[i].setVisibility(View.VISIBLE);
                } catch (PackageManager.NameNotFoundException e) {
                    mSlotIcons[i].setImageDrawable(null);
                    mSlotRemoveBtns[i].setVisibility(View.GONE);
                }
            } else {
                mSlotIcons[i].setImageDrawable(null);
                mSlotRemoveBtns[i].setVisibility(View.GONE);
            }
        }
    }

    private class EditPagerAdapter extends RecyclerView.Adapter<EditPagerAdapter.ViewHolder> {

        private final Activity mActivity;

        EditPagerAdapter(Activity activity) {
            mActivity = activity;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            RecyclerView rv = new RecyclerView(mActivity);
            rv.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            rv.setLayoutManager(new LinearLayoutManager(mActivity));
            return new ViewHolder(rv);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            RecyclerView rv = (RecyclerView) holder.itemView;
            if (position == 0) {
                rv.setAdapter(new FunctionsAdapter(mActivity));
            } else {
                rv.setAdapter(new AppsAdapter(mActivity));
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ViewHolder(View itemView) {
                super(itemView);
            }
        }
    }

    private class AppsAdapter extends RecyclerView.Adapter<AppsAdapter.ViewHolder> {

        private final Activity mActivity;
        private final List<ApplicationInfo> mAppList;
        private final PackageManager mPm;

        AppsAdapter(Activity activity) {
            mActivity = activity;
            mPm = activity.getPackageManager();
            List<ApplicationInfo> allApps = mPm.getInstalledApplications(PackageManager.GET_META_DATA);
            mAppList = new ArrayList<>();
            for (ApplicationInfo app : allApps) {
                if (mPm.getLaunchIntentForPackage(app.packageName) != null) {
                    mAppList.add(app);
                }
            }
            Collections.sort(mAppList, new ApplicationInfo.DisplayNameComparator(mPm));
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mActivity).inflate(
                    R.layout.quick_launch_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ApplicationInfo app = mAppList.get(position);
            holder.title.setText(app.loadLabel(mPm));
            holder.icon.setImageDrawable(app.loadIcon(mPm));
            holder.itemView.setOnClickListener(v -> addPackageToSlots(app.packageName));
        }

        @Override
        public int getItemCount() {
            return mAppList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView icon;
            TextView title;

            ViewHolder(View itemView) {
                super(itemView);
                icon = itemView.findViewById(R.id.item_icon);
                title = itemView.findViewById(R.id.item_title);
            }
        }
    }

    private class FunctionsAdapter extends RecyclerView.Adapter<FunctionsAdapter.ViewHolder> {

        private final Activity mActivity;
        private final List<ApplicationInfo> mFunctionsList;
        private final PackageManager mPm;

        FunctionsAdapter(Activity activity) {
            mActivity = activity;
            mPm = activity.getPackageManager();
            mFunctionsList = new ArrayList<>();
            List<ApplicationInfo> allApps = mPm.getInstalledApplications(PackageManager.GET_META_DATA);
            for (ApplicationInfo app : allApps) {
                if ((app.flags & ApplicationInfo.FLAG_SYSTEM) != 0 && mPm.getLaunchIntentForPackage(app.packageName) != null) {
                    mFunctionsList.add(app);
                }
            }
            Collections.sort(mFunctionsList, new ApplicationInfo.DisplayNameComparator(mPm));
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mActivity).inflate(
                    R.layout.quick_launch_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ApplicationInfo app = mFunctionsList.get(position);
            holder.title.setText(app.loadLabel(mPm));
            holder.icon.setImageDrawable(app.loadIcon(mPm));
            holder.itemView.setOnClickListener(v -> addPackageToSlots(app.packageName));
        }

        @Override
        public int getItemCount() {
            return mFunctionsList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView icon;
            TextView title;

            ViewHolder(View itemView) {
                super(itemView);
                icon = itemView.findViewById(R.id.item_icon);
                title = itemView.findViewById(R.id.item_title);
            }
        }
    }
}
