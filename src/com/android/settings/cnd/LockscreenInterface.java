/*
 * Copyright (C) 2012 CyanogenMod
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

package com.android.settings.cnd;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.Window;
import android.widget.Toast;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.notificationlight.ColorPickerView;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class LockscreenInterface extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "LockscreenInterface";
    private static final boolean DEBUG = true;
    private static final int LOCKSCREEN_BACKGROUND = 1024;
    public static final String KEY_WEATHER_PREF = "lockscreen_weather";
    public static final String KEY_CALENDAR_PREF = "lockscreen_calendar";
    public static final String KEY_BACKGROUND_PREF = "lockscreen_background";
    public static final String KEY_WIDGETS_PREF = "lockscreen_widgets";
    private static final String PREF_LOCKSCREEN_TEXT_COLOR = "lockscreen_text_color";
    private static final String KEY_ALWAYS_BATTERY_PREF = "lockscreen_battery_status";
	private static final String KEY_CLOCK_ALIGN = "lockscreen_clock_align";
    public static final String KEY_VIBRATE_PREF = "lockscreen_vibrate";
    public static final String KEY_TRANSPARENT_PREF = "lockscreen_transparent";
    private static final String KEY_LOCKSCREEN_BUTTONS = "lockscreen_buttons";
    private static final String PREF_CIRCLES_LOCK_BG_COLOR = "circles_lock_bg_color";
    private static final String PREF_CIRCLES_LOCK_RING_COLOR = "circles_lock_ring_color";
    private static final String PREF_CIRCLES_LOCK_HALO_COLOR = "circles_lock_halo_color";
    private static final String PREF_CIRCLES_LOCK_WAVE_COLOR = "circles_lock_wave_color";

    private CheckBoxPreference mVibratePref;
    private CheckBoxPreference mTransparentPref;
    private ListPreference mCustomBackground;
    private ListPreference mWidgetsAlignment;
    private Preference mWeatherPref;
    private Preference mCalendarPref;
    private ColorPickerPreference mLockscreenTextColor;
	private ListPreference mClockAlign;
    private ListPreference mBatteryStatus;
    private PreferenceScreen mLockscreenButtons;
    private Activity mActivity;
    ContentResolver mResolver;
    ColorPickerPreference mCirclesLockBgColor;
    ColorPickerPreference mCirclesLockRingColor;
    ColorPickerPreference mCirclesLockHaloColor;
    ColorPickerPreference mCirclesLockWaveColor;

    private File wallpaperImage;
    private File wallpaperTemporary;
    private boolean mIsScreenLarge;
    private boolean mCirclesLock;
    private boolean mBlackBerryLock;

    public boolean hasButtons() {
        return !getResources().getBoolean(com.android.internal.R.bool.config_showNavigationBar);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        mResolver = mActivity.getContentResolver();

        addPreferencesFromResource(R.xml.lockscreen_interface_settings);
        mWeatherPref = (Preference) findPreference(KEY_WEATHER_PREF);
        mCalendarPref = (Preference) findPreference(KEY_CALENDAR_PREF);

        mCustomBackground = (ListPreference) findPreference(KEY_BACKGROUND_PREF);
        mCustomBackground.setOnPreferenceChangeListener(this);
        wallpaperImage = new File(mActivity.getFilesDir()+"/lockwallpaper");
        wallpaperTemporary = new File(mActivity.getCacheDir()+"/lockwallpaper.tmp");

        mWidgetsAlignment = (ListPreference) findPreference(KEY_WIDGETS_PREF);
        mWidgetsAlignment.setOnPreferenceChangeListener(this);
        mWidgetsAlignment.setValue(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.LOCKSCREEN_LAYOUT,
                0) + "");

        mBatteryStatus = (ListPreference) findPreference(KEY_ALWAYS_BATTERY_PREF);
        mBatteryStatus.setOnPreferenceChangeListener(this);
        
        mLockscreenTextColor = (ColorPickerPreference) findPreference(PREF_LOCKSCREEN_TEXT_COLOR);
        mLockscreenTextColor.setOnPreferenceChangeListener(this);

		mClockAlign = (ListPreference) findPreference(KEY_CLOCK_ALIGN);
        mClockAlign.setOnPreferenceChangeListener(this);

        mCirclesLockBgColor = (ColorPickerPreference) findPreference(PREF_CIRCLES_LOCK_BG_COLOR);
        mCirclesLockBgColor.setOnPreferenceChangeListener(this);

        mCirclesLockRingColor = (ColorPickerPreference) findPreference(PREF_CIRCLES_LOCK_RING_COLOR);
        mCirclesLockRingColor.setOnPreferenceChangeListener(this);

        mCirclesLockHaloColor = (ColorPickerPreference) findPreference(PREF_CIRCLES_LOCK_HALO_COLOR);
        mCirclesLockHaloColor.setOnPreferenceChangeListener(this);

        mCirclesLockWaveColor = (ColorPickerPreference) findPreference(PREF_CIRCLES_LOCK_WAVE_COLOR);
        mCirclesLockWaveColor.setOnPreferenceChangeListener(this);

        mVibratePref = (CheckBoxPreference) findPreference(KEY_VIBRATE_PREF);
        boolean bVibrate = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.LOCKSCREEN_VIBRATE_ENABLED, 1) == 1 ? true : false;
        mVibratePref.setChecked(bVibrate);
        mVibratePref.setOnPreferenceChangeListener(this);

        mTransparentPref = (CheckBoxPreference) findPreference(KEY_TRANSPARENT_PREF);
        boolean bTransparent = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.LOCKSCREEN_TRANSPARENT_ENABLED, 0) == 1 ? true : false;
        mTransparentPref.setChecked(bTransparent);
        mTransparentPref.setOnPreferenceChangeListener(this);

        mLockscreenButtons = (PreferenceScreen) findPreference(KEY_LOCKSCREEN_BUTTONS);
        if (!hasButtons()) {
            getPreferenceScreen().removePreference(mLockscreenButtons);
        }

        mIsScreenLarge = Utils.isTablet(getActivity());

        createCustomLockscreenView();
        updateCustomBackgroundSummary();

    }

    public void createCustomLockscreenView() {
        mCirclesLock = Settings.System.getBoolean(getActivity().getContentResolver(),
                Settings.System.USE_CIRCLES_LOCKSCREEN, false);
    
        mBlackBerryLock = Settings.System.getBoolean(getActivity().getContentResolver(),
                Settings.System.USE_BLACKBERRY_LOCKSCREEN, false);

        if (mCirclesLock) {
            PreferenceCategory stockCategory = (PreferenceCategory) findPreference("lockscreen_style_options");
            getPreferenceScreen().removePreference(stockCategory);
            PreferenceCategory blackberryCategory = (PreferenceCategory) findPreference("lockscreen_style_options_blackberry");
            getPreferenceScreen().removePreference(blackberryCategory);
            PreferenceCategory interfaceCategory = (PreferenceCategory) findPreference("lockscreen_interface_options");
            getPreferenceScreen().removePreference(interfaceCategory);
        }else if (mBlackBerryLock) {
            PreferenceCategory stockCategory = (PreferenceCategory) findPreference("lockscreen_style_options");
            getPreferenceScreen().removePreference(stockCategory);
            PreferenceCategory circlesCategory = (PreferenceCategory) findPreference("lockscreen_style_options_circles");
            getPreferenceScreen().removePreference(circlesCategory);
            PreferenceCategory circlesColorCategory = (PreferenceCategory) findPreference("circles_lockscreen");
            getPreferenceScreen().removePreference(circlesColorCategory);
            PreferenceCategory interfaceCategory = (PreferenceCategory) findPreference("lockscreen_interface_options");
            getPreferenceScreen().removePreference(interfaceCategory);
        }else {
            PreferenceCategory circleCategory = (PreferenceCategory) findPreference("lockscreen_style_options_circles");
            getPreferenceScreen().removePreference(circleCategory);
            PreferenceCategory blackberryCategory = (PreferenceCategory) findPreference("lockscreen_style_options_blackberry");
            getPreferenceScreen().removePreference(blackberryCategory);
            PreferenceCategory circlesColorCategory = (PreferenceCategory) findPreference("circles_lockscreen");
            getPreferenceScreen().removePreference(circlesColorCategory);
            PreferenceCategory interfaceAltCategory = (PreferenceCategory) findPreference("lockscreen_interface_options_alt");
            getPreferenceScreen().removePreference(interfaceAltCategory);
        }

    }

    private void updateCustomBackgroundSummary() {
        int resId;
        String value = Settings.System.getString(getContentResolver(),
                Settings.System.LOCKSCREEN_BACKGROUND);
        if (value == null) {
            resId = R.string.lockscreen_background_default_wallpaper;
            mCustomBackground.setValueIndex(2);
        } else if (value.isEmpty()) {
            resId = R.string.lockscreen_background_custom_image;
            mCustomBackground.setValueIndex(1);
        } else {
            resId = R.string.lockscreen_background_color_fill;
            mCustomBackground.setValueIndex(0);
        }
        mCustomBackground.setSummary(getResources().getString(resId));
    }

    @Override
    public void onResume() {
        super.onResume();
        updateState();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void updateState() {
        int resId;

        // Set the weather description text
        if (mWeatherPref != null) {
            boolean weatherEnabled = Settings.System.getInt(mResolver,
                    Settings.System.LOCKSCREEN_WEATHER, 0) == 1;
            if (weatherEnabled) {
                mWeatherPref.setSummary(R.string.lockscreen_weather_enabled);
            } else {
                mWeatherPref.setSummary(R.string.lockscreen_weather_summary);
            }
        }

        // Set the calendar description text
        if (mCalendarPref != null) {
            boolean weatherEnabled = Settings.System.getInt(mResolver,
                    Settings.System.LOCKSCREEN_CALENDAR, 0) == 1;
            if (weatherEnabled) {
                mCalendarPref.setSummary(R.string.lockscreen_calendar_enabled);
            } else {
                mCalendarPref.setSummary(R.string.lockscreen_calendar_summary);
            }
        }

        // Set the calendar description text
        if (mBatteryStatus != null) {
            boolean batteryStatusAlwaysOn = Settings.System.getInt(mResolver,
                    Settings.System.LOCKSCREEN_ALWAYS_SHOW_BATTERY, 0) == 1;
            if (batteryStatusAlwaysOn) {
                mBatteryStatus.setValueIndex(1);
            } else {
                mBatteryStatus.setValueIndex(0);
            }
            mBatteryStatus.setSummary(mBatteryStatus.getEntry());
            //mCustomBackground.setSummary(getResources().getString(resId));
        }
        // Set the clock align value
        if (mClockAlign != null) {
            int clockAlign = Settings.System.getInt(mResolver,
                    Settings.System.LOCKSCREEN_CLOCK_ALIGN, 2);
            mClockAlign.setValue(String.valueOf(clockAlign));
            mClockAlign.setSummary(mClockAlign.getEntries()[clockAlign]);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOCKSCREEN_BACKGROUND) {
            if (resultCode == Activity.RESULT_OK) {
                if (wallpaperTemporary.exists()) {
                    wallpaperTemporary.renameTo(wallpaperImage);
                }
                wallpaperImage.setReadOnly();
                Toast.makeText(mActivity, getResources().getString(R.string.
                        lockscreen_background_result_successful), Toast.LENGTH_LONG).show();
                Settings.System.putString(getContentResolver(),
                        Settings.System.LOCKSCREEN_BACKGROUND,"");
                updateCustomBackgroundSummary();
            } else {
                if (wallpaperTemporary.exists()) {
                    wallpaperTemporary.delete();
                }
                Toast.makeText(mActivity, getResources().getString(R.string.
                        lockscreen_background_result_not_successful), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        boolean handled = false;
        if (preference == mCustomBackground) {
            int indexOf = mCustomBackground.findIndexOfValue(objValue.toString());
            switch (indexOf) {
            //Displays color dialog when user has chosen color fill
            case 0:
                final ColorPickerView colorView = new ColorPickerView(mActivity);
                int currentColor = Settings.System.getInt(getContentResolver(),
                        Settings.System.LOCKSCREEN_BACKGROUND, -1);
                if (currentColor != -1) {
                    colorView.setColor(currentColor);
                }
                colorView.setAlphaSliderVisible(true);
                new AlertDialog.Builder(mActivity)
                .setTitle(R.string.lockscreen_custom_background_dialog_title)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_BACKGROUND, colorView.getColor());
                        updateCustomBackgroundSummary();
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setView(colorView).show();
                return false;
            //Launches intent for user to select an image/crop it to set as background
            case 1:
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                intent.putExtra("crop", "true");
                intent.putExtra("scale", true);
                intent.putExtra("scaleUpIfNeeded", false);
                intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
                Display display = mActivity.getWindowManager().getDefaultDisplay();
                int width = display.getWidth();
                int height = display.getHeight();
                Rect rect = new Rect();
                Window window = mActivity.getWindow();
                window.getDecorView().getWindowVisibleDisplayFrame(rect);
                int statusBarHeight = rect.top;
                int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
                int titleBarHeight = contentViewTop - statusBarHeight;
                // Lock screen for tablets visible section are different in landscape/portrait,
                // image need to be cropped correctly, like wallpaper setup for scrolling in background in home screen
                // other wise it does not scale correctly
                if (mIsScreenLarge) {
                    width = mActivity.getWallpaperDesiredMinimumWidth();
                    height = mActivity.getWallpaperDesiredMinimumHeight();
                    float spotlightX = (float) display.getWidth() / width;
                    float spotlightY = (float) display.getHeight() / height;
                    intent.putExtra("aspectX", width);
                    intent.putExtra("aspectY", height);
                    intent.putExtra("outputX", width);
                    intent.putExtra("outputY", height);
                    intent.putExtra("spotlightX", spotlightX);
                    intent.putExtra("spotlightY", spotlightY);

                } else {
                    boolean isPortrait = getResources().getConfiguration().orientation ==
                        Configuration.ORIENTATION_PORTRAIT;
                    intent.putExtra("aspectX", isPortrait ? width : height - titleBarHeight);
                    intent.putExtra("aspectY", isPortrait ? height - titleBarHeight : width);
                }
                try {
                    wallpaperTemporary.createNewFile();
                    wallpaperTemporary.setWritable(true, false);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(wallpaperTemporary));
                    intent.putExtra("return-data", false);
                    mActivity.startActivityFromFragment(this, intent, LOCKSCREEN_BACKGROUND);
                } catch (IOException e) {
                } catch (ActivityNotFoundException e) {
                }
                return false;
            //Sets background color to default
            case 2:
                Settings.System.putString(getContentResolver(),
                        Settings.System.LOCKSCREEN_BACKGROUND, null);
                updateCustomBackgroundSummary();
                break;
            }
            return true;
        } else if (preference == mBatteryStatus) {
            int value = Integer.valueOf((String) objValue);
            int index = mBatteryStatus.findIndexOfValue((String) objValue);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.LOCKSCREEN_ALWAYS_SHOW_BATTERY, value);
            mBatteryStatus.setSummary(mBatteryStatus.getEntries()[index]);
            return true;
        } else if (preference == mVibratePref) {
            boolean bValue = Boolean.valueOf((Boolean) objValue);
            int value = 0;
            if (bValue) {
                value = 1;
            }
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.LOCKSCREEN_VIBRATE_ENABLED, value);
            return true;
         } else if (preference == mTransparentPref) {
            boolean bValue = Boolean.valueOf((Boolean) objValue);
            int value = 0;
            if (bValue) {
                value = 1;
            }
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.LOCKSCREEN_TRANSPARENT_ENABLED, value);
            return true;
        } else if (preference == mLockscreenTextColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String.valueOf(objValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                                    Settings.System.LOCKSCREEN_CUSTOM_TEXT_COLOR, intHex);
            if (DEBUG) Log.d(TAG, String.format("new color hex value: %d", intHex));
            return true;
        } else if (preference == mWidgetsAlignment) {
            int value = Integer.valueOf((String) objValue);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                                    Settings.System.LOCKSCREEN_LAYOUT, value);
            return true;
         } else if (preference == mClockAlign) {
            int value = Integer.valueOf((String) objValue);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.LOCKSCREEN_CLOCK_ALIGN, value);
            mClockAlign.setSummary(mClockAlign.getEntries()[value]);
            return true;
         } else if (preference == mCirclesLockBgColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String.valueOf(objValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.CIRCLES_LOCK_BG_COLOR, intHex);
            return true;
        } else if (preference == mCirclesLockRingColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String.valueOf(objValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.CIRCLES_LOCK_RING_COLOR, intHex);
            return true;
        } else if (preference == mCirclesLockHaloColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String.valueOf(objValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.CIRCLES_LOCK_HALO_COLOR, intHex);
            return true;
        } else if (preference == mCirclesLockWaveColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String.valueOf(objValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.CIRCLES_LOCK_WAVE_COLOR, intHex);
            return true;
        }
        return false;
    }

}