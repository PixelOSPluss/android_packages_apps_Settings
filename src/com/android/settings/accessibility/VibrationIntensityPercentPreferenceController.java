/*
 * Copyright (C) 2026 The Android Open Source Project
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

package com.android.settings.accessibility;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.os.VibrationAttributes;
import android.os.Vibrator;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.SliderPreferenceController;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;
import com.android.settingslib.widget.SliderPreference;

/**
 * Preference controller for the global vibration strength percentage, used when the device
 * configures vibration with a single percentage instead of discrete per-usage intensity levels.
 */
// LINT.IfChange
public class VibrationIntensityPercentPreferenceController extends SliderPreferenceController
        implements LifecycleObserver, OnStart, OnStop {

    static final int MIN_PERCENT = 0;
    static final int MAX_PERCENT = 100;
    static final int DEFAULT_PERCENT = 100;

    private static final Uri PERCENT_URI =
            Settings.System.getUriFor(Settings.System.VIBRATION_INTENSITY_PERCENT);
    private static final Uri VIBRATE_ON_URI = Settings.System.getUriFor(Settings.System.VIBRATE_ON);

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final ContentObserver mSettingsObserver = new ContentObserver(mHandler) {
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            refreshPreference();
        }
    };

    private SliderPreference mPreference;
    private boolean mObserverRegistered;

    public VibrationIntensityPercentPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        final boolean hasVibrator = mContext.getSystemService(Vibrator.class).hasVibrator();
        return hasVibrator && isIntensityPercentEnabled()
                ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreference = screen.findPreference(getPreferenceKey());
        if (mPreference == null) {
            return;
        }
        mPreference.setMin(getMin());
        mPreference.setMax(getMax());
        mPreference.setSliderIncrement(1);
        mPreference.setTickVisible(false);
        // Haptics previews are played continuously while dragging, so each preview reflects the
        // strength currently selected.
        mPreference.setUpdatesContinuously(true);
        refreshPreference();
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        refreshPreference();
    }

    @Override
    public void onStart() {
        if (mObserverRegistered) {
            return;
        }
        final ContentResolver resolver = mContext.getContentResolver();
        resolver.registerContentObserver(PERCENT_URI, /* notifyForDescendants= */ false,
                mSettingsObserver, UserHandle.USER_CURRENT);
        resolver.registerContentObserver(VIBRATE_ON_URI, /* notifyForDescendants= */ false,
                mSettingsObserver, UserHandle.USER_CURRENT);
        mObserverRegistered = true;
    }

    @Override
    public void onStop() {
        if (!mObserverRegistered) {
            return;
        }
        mContext.getContentResolver().unregisterContentObserver(mSettingsObserver);
        mObserverRegistered = false;
    }

    @Override
    public int getMin() {
        return MIN_PERCENT;
    }

    @Override
    public int getMax() {
        return MAX_PERCENT;
    }

    @Override
    public int getSliderPosition() {
        if (!isPreferenceEnabled()) {
            return getMin();
        }
        return readPercent();
    }

    @Override
    public boolean setSliderPosition(int position) {
        if (!isPreferenceEnabled()) {
            // Ignore slider updates when the main vibration switch is off.
            return false;
        }
        final int percent = Math.max(MIN_PERCENT, Math.min(MAX_PERCENT, position));
        final boolean success = Settings.System.putIntForUser(mContext.getContentResolver(),
                Settings.System.VIBRATION_INTENSITY_PERCENT, percent, UserHandle.USER_CURRENT);
        if (success && percent > MIN_PERCENT) {
            VibrationPreferenceConfig.playVibrationPreview(
                    mContext.getSystemService(Vibrator.class),
                    VibrationAttributes.USAGE_TOUCH);
        }
        return success;
    }

    private void refreshPreference() {
        final SliderPreference preference = mPreference;
        if (preference == null) {
            return;
        }
        final boolean enabled = isPreferenceEnabled();
        preference.setValue(enabled ? readPercent() : MIN_PERCENT);
        preference.setEnabled(enabled);
        if (!enabled || preference.getValue() == MIN_PERCENT) {
            preference.setSummary(R.string.vibration_intensity_percent_off);
        } else {
            preference.setSummary(mContext.getString(
                    R.string.vibration_intensity_percent_summary, preference.getValue()));
        }
    }

    private boolean isPreferenceEnabled() {
        return Settings.System.getIntForUser(mContext.getContentResolver(),
                Settings.System.VIBRATE_ON, 1, UserHandle.USER_CURRENT) != 0;
    }

    private int readPercent() {
        final int percent = Settings.System.getIntForUser(mContext.getContentResolver(),
                Settings.System.VIBRATION_INTENSITY_PERCENT, DEFAULT_PERCENT,
                UserHandle.USER_CURRENT);
        return Math.max(MIN_PERCENT, Math.min(MAX_PERCENT, percent));
    }

    private boolean isIntensityPercentEnabled() {
        return mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_vibrationIntensityPercentEnabled);
    }
}
// LINT.ThenChange(VibrationIntensityPercentSliderPreference.kt)
