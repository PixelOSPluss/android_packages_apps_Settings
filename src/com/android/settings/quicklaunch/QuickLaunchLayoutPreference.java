/*
 * Copyright (C) 2026 Lunaris-AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.quicklaunch;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.android.settings.R;
import com.google.android.material.materialswitch.MaterialSwitch;

public class QuickLaunchLayoutPreference extends Preference {

    public QuickLaunchLayoutPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void notifyChanged() {
        super.notifyChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        View view = holder.itemView;
        Context context = getContext();

        ViewPager2 viewPager = view.findViewById(R.id.tutorial_viewpager);
        LinearLayout indicatorContainer = view.findViewById(R.id.indicator_container);
        MaterialSwitch mainSwitch = view.findViewById(R.id.quick_launch_switch);
        TextView btnEdit = view.findViewById(R.id.btn_edit_menu);

        ImageView slotTop = view.findViewById(R.id.slot_top_icon);
        ImageView slotLeft = view.findViewById(R.id.slot_left_icon);
        ImageView slotCenter = view.findViewById(R.id.slot_center_icon);
        ImageView slotRight = view.findViewById(R.id.slot_right_icon);

        if (viewPager != null && viewPager.getAdapter() == null) {
            viewPager.setAdapter(new QuickLaunchSettings.TutorialAdapter(context));
            setupIndicators(context, indicatorContainer, viewPager);
        }

        if (mainSwitch != null) {
            boolean enabled = Settings.Secure.getInt(context.getContentResolver(),
                    QuickLaunchSettings.QUICK_LAUNCH_ENABLED, 0) == 1;
            mainSwitch.setChecked(enabled);
            mainSwitch.setOnCheckedChangeListener((btn, isChecked) -> {
                Settings.Secure.putInt(context.getContentResolver(),
                        QuickLaunchSettings.QUICK_LAUNCH_ENABLED, isChecked ? 1 : 0);
            });
        }

        if (btnEdit != null) {
            btnEdit.setOnClickListener(v -> {
                Intent intent = new Intent(context, QuickLaunchEditActivity.class);
                context.startActivity(intent);
            });
        }

        loadSlotIcons(context, slotTop, slotLeft, slotCenter, slotRight);
    }

    private void setupIndicators(Context context, LinearLayout container, ViewPager2 viewPager) {
        if (container == null) return;
        container.removeAllViews();
        int count = 2;
        ImageView[] dots = new ImageView[count];

        for (int i = 0; i < count; i++) {
            dots[i] = new ImageView(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(16, 16);
            params.setMargins(8, 0, 8, 0);
            dots[i].setLayoutParams(params);
            dots[i].setImageResource(R.drawable.ic_settings_accent);
            dots[i].setAlpha(i == 0 ? 1.0f : 0.3f);
            container.addView(dots[i]);
        }

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < count; i++) {
                    dots[i].setAlpha(i == position ? 1.0f : 0.3f);
                }
            }
        });
    }

    private void loadSlotIcons(Context context, ImageView top, ImageView left,
                                ImageView center, ImageView right) {
        String slotsData = Settings.Secure.getString(context.getContentResolver(),
                QuickLaunchSettings.QUICK_LAUNCH_SLOTS);
        if (slotsData == null || slotsData.isEmpty()) {
            return;
        }
        String[] packages = slotsData.split(",");
        PackageManager pm = context.getPackageManager();

        ImageView[] views = new ImageView[]{center, top, left, right};
        for (int i = 0; i < packages.length && i < views.length; i++) {
            if (views[i] != null && !packages[i].isEmpty()) {
                try {
                    Drawable icon = pm.getApplicationIcon(packages[i]);
                    views[i].setImageDrawable(icon);
                } catch (PackageManager.NameNotFoundException ignored) {
                }
            }
        }
    }
}
