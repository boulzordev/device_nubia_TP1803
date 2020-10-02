/*
 * Copyright (C) 2016 The CyanogenMod Project
 * Copyright (C) 2017 The LineageOS Project
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

package org.omnirom.device;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class Constants {

    public static final boolean DEBUG = false;

    private static final String TAG = "DeviceParts";

    // FP gestures
    public static final int FP_TAP_SCANCODE = 87;
    public static final int FP_HOLD_SCANCODE = 28;
    public static final int FP_RIGHT_SCANCODE = 106;
    public static final int FP_LEFT_SCANCODE = 105;
    public static final int[] sSupportedFPGestures = new int[]{
            FP_TAP_SCANCODE,
            FP_HOLD_SCANCODE,
            FP_RIGHT_SCANCODE,
            FP_LEFT_SCANCODE
    };

    // FP actions
    public static final int ACTION_HOME = 100;
    public static final int ACTION_POWER = 101;
    public static final int ACTION_BACK = 102;
    public static final int ACTION_RECENTS = 103;
    public static final int ACTION_VOLUME_UP = 104;
    public static final int ACTION_VOLUME_DOWN = 105;
    public static final int ACTION_VOICE_ASSISTANT = 106;
    public static final int ACTION_PLAY_PAUSE = 107;
    public static final int ACTION_PREVIOUS_TRACK = 108;
    public static final int ACTION_NEXT_TRACK = 109;
    public static final int ACTION_FLASHLIGHT = 110;
    public static final int ACTION_CAMERA = 111;
    public static final int ACTION_SCREENSHOT = 112;
    public static final int ACTION_BROWSER = 116;
    public static final int ACTION_DIALER = 117;
    public static final int ACTION_EMAIL = 118;
    public static final int ACTION_MESSAGES = 119;
    public static final int ACTION_LAST_APP = 121;
    public static final int[] sFPSupportedActions = new int[]{
            ACTION_HOME,
            ACTION_POWER,
            ACTION_BACK,
            ACTION_RECENTS,
            ACTION_VOLUME_UP,
            ACTION_VOLUME_DOWN,
            ACTION_VOICE_ASSISTANT,
            ACTION_PLAY_PAUSE,
            ACTION_PREVIOUS_TRACK,
            ACTION_NEXT_TRACK,
            ACTION_FLASHLIGHT,
            ACTION_CAMERA,
            ACTION_SCREENSHOT,
            ACTION_LAST_APP
    };
    public static final int[] sFPSupportedActionsScreenOff = new int[]{
            ACTION_POWER,
            ACTION_VOLUME_UP,
            ACTION_VOLUME_DOWN,
            ACTION_PLAY_PAUSE,
            ACTION_PREVIOUS_TRACK,
            ACTION_NEXT_TRACK,
            ACTION_FLASHLIGHT,
            ACTION_CAMERA
    };

    // List of keys
    public static final String FP_KEYS = "fp_keys";
    public static final String FP_KEY_HOLD = "fp_key_hold";
    public static final String FP_KEY_LEFT = "fp_key_left";
    public static final String FP_KEY_RIGHT = "fp_key_right";

    public static final String FP_KEYS_OFF = "fp_keys_off";
    public static final String FP_KEY_HOLD_OFF = "fp_key_hold_off";
    public static final String FP_KEY_LEFT_OFF = "fp_key_left_off";
    public static final String FP_KEY_RIGHT_OFF = "fp_key_right_off";

    // Holds <preference_key> -> <proc_node> mapping
    public static final Map<String, String> sBooleanNodePreferenceMap = new HashMap<>();

    // Holds <preference_key> -> <default_values> mapping
    public static final Map<String, Object> sNodeDefaultMap = new HashMap<>();

    public static final String[] sPrefKeys = {
        FP_KEYS,
        FP_KEY_HOLD,
        FP_KEY_RIGHT,
        FP_KEY_LEFT,
        FP_KEYS_OFF,
        FP_KEY_HOLD_OFF,
        FP_KEY_RIGHT_OFF,
        FP_KEY_LEFT_OFF
    };

    static {
        sNodeDefaultMap.put(FP_KEYS, "0");
        sNodeDefaultMap.put(FP_KEY_HOLD, "0");
        sNodeDefaultMap.put(FP_KEY_LEFT, "0");
        sNodeDefaultMap.put(FP_KEY_RIGHT, "0");
        sNodeDefaultMap.put(FP_KEYS_OFF, "0");
        sNodeDefaultMap.put(FP_KEY_HOLD_OFF, "0");
        sNodeDefaultMap.put(FP_KEY_LEFT_OFF, "0");
        sNodeDefaultMap.put(FP_KEY_RIGHT_OFF, "0");
    }

    public static boolean isPreferenceEnabled(Context context, String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(key, (Boolean) sNodeDefaultMap.get(key));
    }

    public static String GetPreference(Context context, String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, (String) sNodeDefaultMap.get(key));
    }
}
