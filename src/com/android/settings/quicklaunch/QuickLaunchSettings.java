/*
 * Copyright (C) 2026 Lunaris-AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.quicklaunch;

import android.os.Bundle;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class QuickLaunchSettings extends SettingsPreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.quick_launch_settings_preference);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.VIEW_UNKNOWN;
    }
}
