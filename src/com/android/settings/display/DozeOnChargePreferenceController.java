/*
 * Copyright (C) 2023-2026 Lunaris-AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.display;

import android.content.Context;
import android.hardware.display.AmbientDisplayConfiguration;
import android.os.UserHandle;
import android.provider.Settings;

import com.android.settings.core.TogglePreferenceController;

public class DozeOnChargePreferenceController extends TogglePreferenceController {

    private static final int MY_USER = UserHandle.myUserId();
    private AmbientDisplayConfiguration mConfig;

    public DozeOnChargePreferenceController(Context context, String key) {
        super(context, key);
    }

    private AmbientDisplayConfiguration getConfig() {
        if (mConfig == null) {
            mConfig = new AmbientDisplayConfiguration(mContext);
        }
        return mConfig;
    }

    @Override
    public int getAvailabilityStatus() {
        return getConfig().alwaysOnAvailableForUser(MY_USER) ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public boolean isChecked() {
        return Settings.Secure.getIntForUser(
                mContext.getContentResolver(),
                Settings.Secure.DOZE_ON_CHARGE,
                0,
                MY_USER) != 0;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        return Settings.Secure.putIntForUser(
                mContext.getContentResolver(),
                Settings.Secure.DOZE_ON_CHARGE,
                isChecked ? 1 : 0,
                MY_USER);
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return com.android.settings.R.string.menu_key_display;
    }
}
