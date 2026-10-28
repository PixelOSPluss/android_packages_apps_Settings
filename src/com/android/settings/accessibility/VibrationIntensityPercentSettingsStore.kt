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
import android.provider.Settings.System.VIBRATE_ON
import android.provider.Settings.System.VIBRATION_INTENSITY_PERCENT
import com.android.settings.R
import com.android.settingslib.datastore.AbstractKeyedDataObservable
import com.android.settingslib.datastore.HandlerExecutor
import com.android.settingslib.datastore.KeyValueStore
import com.android.settingslib.datastore.KeyedObserver
import com.android.settingslib.datastore.SettingsSystemStore

/** SettingsStore for the global vibration strength percentage. */
class VibrationIntensityPercentSettingsStore(
    private val context: Context,
    private val preferenceKey: String,
    private val keyValueStoreDelegate: KeyValueStore = SettingsSystemStore.get(context),
) : AbstractKeyedDataObservable<String>(), KeyedObserver<String>, KeyValueStore {

    fun isPreferenceEnabled(): Boolean = keyValueStoreDelegate.getBoolean(VIBRATE_ON) != false

    fun getSummary(): CharSequence? {
        if (!isPreferenceEnabled()) {
            return context.getString(R.string.vibration_intensity_percent_off)
        }
        val percent = getPercent()
        return if (percent == MIN_PERCENT) {
            context.getString(R.string.vibration_intensity_percent_off)
        } else {
            context.getString(R.string.vibration_intensity_percent_summary, percent)
        }
    }

    fun getPercent(): Int =
        (keyValueStoreDelegate.getInt(VIBRATION_INTENSITY_PERCENT) ?: DEFAULT_PERCENT)
            .coerceIn(MIN_PERCENT, MAX_PERCENT)

    override fun contains(key: String) = keyValueStoreDelegate.contains(VIBRATION_INTENSITY_PERCENT)

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> getDefaultValue(key: String, valueType: Class<T>): T? =
        when (valueType) {
            Boolean::class.javaObjectType -> true as T
            Int::class.javaObjectType -> DEFAULT_PERCENT as T
            else -> null
        }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> getValue(key: String, valueType: Class<T>): T? =
        when (valueType) {
            Boolean::class.javaObjectType -> isPreferenceEnabled() as T
            Int::class.javaObjectType ->
                (if (isPreferenceEnabled()) getPercent() else MIN_PERCENT) as T
            else -> null
        }

    override fun <T : Any> setValue(key: String, valueType: Class<T>, value: T?) {
        val percent = value as Int?
        keyValueStoreDelegate.setInt(
            VIBRATION_INTENSITY_PERCENT,
            percent?.coerceIn(MIN_PERCENT, MAX_PERCENT),
        )
    }

    override fun onFirstObserverAdded() {
        keyValueStoreDelegate.addObserver(VIBRATION_INTENSITY_PERCENT, this, HandlerExecutor.main)
        keyValueStoreDelegate.addObserver(VIBRATE_ON, this, HandlerExecutor.main)
    }

    override fun onLastObserverRemoved() {
        keyValueStoreDelegate.removeObserver(VIBRATION_INTENSITY_PERCENT, this)
        keyValueStoreDelegate.removeObserver(VIBRATE_ON, this)
    }

    override fun onKeyChanged(key: String, reason: Int) {
        // forward data change to preference hierarchy key
        notifyChange(preferenceKey, reason)
    }

    companion object {
        const val MIN_PERCENT = 0
        const val MAX_PERCENT = 100
        const val DEFAULT_PERCENT = 100
    }
}

/** Returns true if the device uses a global vibration strength percentage. */
fun Context.isVibrationIntensityPercentEnabled(): Boolean =
    resources.getBoolean(
        com.android.internal.R.bool.config_vibrationIntensityPercentEnabled
    )
