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
package com.android.settings.accessibility

import android.content.Context
import android.os.VibrationAttributes.USAGE_TOUCH
import androidx.annotation.CallSuper
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceChangeListener
import com.android.settings.R
import com.android.settingslib.datastore.KeyValueStore
import com.android.settingslib.metadata.IntRangeValuePreference
import com.android.settingslib.metadata.PreferenceMetadata
import com.android.settingslib.metadata.PreferenceSummaryProvider
import com.android.settingslib.metadata.ReadWritePermit
import com.android.settingslib.metadata.preferencesapi.preconditions.PreconditionStability
import com.android.settingslib.widget.SliderPreference
import com.android.settingslib.widget.SliderPreferenceBinding

/**
 * SliderPreference for the global vibration strength percentage.
 *
 * This preference observes the state of the VibrationMainSwitchPreference in this fragment,
 * disabling and displaying 0 when the main switch is unchecked. This "off" state is not persisted,
 * so the original percentage is restored once the main switch is turned back on.
 */
// LINT.IfChange
class VibrationIntensityPercentSliderPreference(
    context: Context,
    override val key: String,
    private val mainSwitchPreferenceKey: String,
) :
    IntRangeValuePreference,
    PreferenceSummaryProvider,
    SliderPreferenceBinding,
    OnPreferenceChangeListener {

    private val storage by lazy {
        VibrationIntensityPercentSettingsStore(context, preferenceKey = key)
    }

    override val title
        get() = R.string.vibration_intensity_percent_title

    override val purpose
        get() = R.string.vibration_intensity_percent_purpose

    override val keywords
        get() = R.string.keywords_vibration_intensity_percent

    override fun getMinValue(context: Context) =
        VibrationIntensityPercentSettingsStore.MIN_PERCENT

    override fun getMaxValue(context: Context) =
        VibrationIntensityPercentSettingsStore.MAX_PERCENT

    override fun getIncrementStep(context: Context) = 1

    override fun storage(context: Context): KeyValueStore = storage

    override fun dependencies(context: Context) = arrayOf(mainSwitchPreferenceKey)

    override fun getSummary(context: Context) = storage.getSummary()

    override fun createWidget(context: Context) = SliderPreference(context)

    override fun getWritePermit(context: Context, callingPid: Int, callingUid: Int) =
        ReadWritePermit.ALLOW

    override val supportsWrite = true

    @CallSuper
    override fun bind(preference: Preference, metadata: PreferenceMetadata) {
        super.bind(preference, metadata)
        preference.onPreferenceChangeListener = this
        (preference as SliderPreference).apply {
            setTickVisible(false)
            updatesContinuously = true
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        val percent = newValue as Int
        // must make new value effective before preview
        (preference as SliderPreference).value = percent
        if (percent > VibrationIntensityPercentSettingsStore.MIN_PERCENT) {
            preference.context.playVibrationSettingsPreview(USAGE_TOUCH)
        }
        return false // value has been updated
    }

    override fun getEnabledDescription() = "The vibration setting (vibrate_on) must be enabled."

    override fun getEnabledStability() = PreconditionStability.UNSTABLE

    @CallSuper override fun isEnabled(context: Context) = storage.isPreferenceEnabled()
}
// LINT.ThenChange(VibrationIntensityPercentPreferenceController.java)
