/*
 * Copyright (C) 2026 Lunaris-AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.quicklaunch;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.ArrayList;
import java.util.List;

public class QuickLaunchSettings extends SettingsPreferenceFragment {

    public static final String QUICK_LAUNCH_ENABLED = "quick_launch_enabled";
    public static final String QUICK_LAUNCH_SLOTS = "quick_launch_slots";

    private ViewPager2 mTutorialViewPager;
    private LinearLayout mIndicatorContainer;
    private MaterialSwitch mMainSwitch;
    private TextView mBtnEditMenu;

    private ImageView mSlotTopIcon;
    private ImageView mSlotLeftIcon;
    private ImageView mSlotCenterIcon;
    private ImageView mSlotRightIcon;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.quick_launch_settings_preference);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Preference pref = findPreference("quick_launch_layout");
        if (pref != null) {
            pref.setOnPreferenceClickListener(p -> true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateSlotsPreview();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.VIEW_UNKNOWN;
    }

    private void updateSlotsPreview() {
        QuickLaunchLayoutPreference pref = findPreference("quick_launch_layout");
        if (pref != null) {
            pref.notifyChanged();
        }
    }

    static class TutorialAdapter extends RecyclerView.Adapter<TutorialAdapter.ViewHolder> {

        private final Context mContext;
        private final String[] mTutorialTexts;

        public TutorialAdapter(Context context) {
            mContext = context;
            mTutorialTexts = new String[]{
                    context.getString(R.string.quick_launch_tutorial_1),
                    context.getString(R.string.quick_launch_tutorial_2)
            };
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(
                    R.layout.quick_launch_tutorial_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.text.setText(mTutorialTexts[position]);
            holder.image.setImageResource(R.drawable.ic_settings_accent);
        }

        @Override
        public int getItemCount() {
            return 2;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView image;
            TextView text;

            ViewHolder(View itemView) {
                super(itemView);
                image = itemView.findViewById(R.id.tutorial_image);
                text = itemView.findViewById(R.id.tutorial_text);
            }
        }
    }
}
