/*
 * Copyright (C) 2020 OmniROM Project
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

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.ISearchManager;
import android.app.KeyguardManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.input.InputManager;
import android.media.AudioManager;
import android.media.session.MediaSessionLegacyHelper;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

import com.android.internal.util.ArrayUtils;
import com.android.internal.util.omni.DeviceKeyHandler;

import static org.omnirom.device.Constants.*;

import java.util.List;

public class KeyHandler implements DeviceKeyHandler {
    private static final String TAG = KeyHandler.class.getSimpleName();

    private static final int CUSTOM_SCAN_CODE = 216;
    private final static String mAssistantKey = "assistant_button";
    private static final String WAKEUP_REASON = "assistant-key";

    private final AudioManager mAudioManager;
    private final CameraManager mCameraManager;
    private final Context mContext;
    private KeyguardManager mKeyguardManager;
    private final PowerManager mPowerManager;
    private ISearchManager mSearchManagerService;
    private final Vibrator mVibrator;

    private Handler mHandler;

    private String mRearCameraId;
    private boolean mTorchEnabled;

    public KeyHandler(Context context) {
        mContext = context;
        mPowerManager = context.getSystemService(PowerManager.class);
        mCameraManager = mContext.getSystemService(CameraManager.class);
        mVibrator = context.getSystemService(Vibrator.class);
        mAudioManager = mContext.getSystemService(AudioManager.class);
        mHandler = new Handler(Looper.getMainLooper());
    }

    public boolean handleKeyEvent(KeyEvent event) {
        int scanCode = event.getScanCode();

        boolean isFPScanCode = ArrayUtils.contains(sSupportedFPGestures, scanCode);

        if ((scanCode != CUSTOM_SCAN_CODE && !isFPScanCode) || event.getAction() != KeyEvent.ACTION_UP) {
            return false;
        }

        boolean isScreenOn = mPowerManager.isScreenOn();

        Integer action = 0;
        if (scanCode == CUSTOM_SCAN_CODE) {
            action = Settings.Global.getInt(mContext.getContentResolver(), mAssistantKey, 0);
        } else if (isFPScanCode) {
            action = getFPGestureValueForScanCode(scanCode, isScreenOn);
        }

        if (action == 0) {
            return false;
        } else {
            ensureKeyguardManager();
            switch (action) {
                case ACTION_POWER:
                    toggleScreenState();
                    break;
                case ACTION_VOICE_ASSISTANT:
                    fireGoogleNow();
                    break;
                case ACTION_CAMERA:
                    launchCamera();
                    break;
                case ACTION_FLASHLIGHT:
                    toggleFlashlight();
                    break;
                case ACTION_BROWSER:
                    launchBrowser();
                    break;
                case ACTION_DIALER:
                    launchDialer();
                    break;
                case ACTION_EMAIL:
                    launchEmail();
                    break;
                case ACTION_MESSAGES:
                    launchMessages();
                    break;
                case ACTION_PLAY_PAUSE:
                    playPauseMusic();
                    break;
                case ACTION_VOLUME_UP:
                    triggerVirtualKeypress(mHandler, KeyEvent.KEYCODE_VOLUME_UP);
                    break;
                case ACTION_VOLUME_DOWN:
                    triggerVirtualKeypress(mHandler, KeyEvent.KEYCODE_VOLUME_DOWN);
                    break;
                case ACTION_PREVIOUS_TRACK:
                    dispatchMediaKeyWithWakeLock(KeyEvent.KEYCODE_MEDIA_PREVIOUS, mContext);
                    break;
                case ACTION_NEXT_TRACK:
                    dispatchMediaKeyWithWakeLock(KeyEvent.KEYCODE_MEDIA_NEXT, mContext);
                    break;
                case ACTION_HOME:
                    if (!mKeyguardManager.inKeyguardRestrictedInputMode()) {
                        triggerVirtualKeypress(mHandler, KeyEvent.KEYCODE_HOME);
                    }
                    break;
                case ACTION_BACK:
                    triggerVirtualKeypress(mHandler, KeyEvent.KEYCODE_BACK);
                    break;
                case ACTION_RECENTS:
                    if (!mKeyguardManager.inKeyguardRestrictedInputMode()) {
                        triggerVirtualKeypress(mHandler, KeyEvent.KEYCODE_APP_SWITCH);
                    }
                    break;
                case ACTION_LAST_APP:
                    if (!mKeyguardManager.inKeyguardRestrictedInputMode()) {
                        switchToLastApp(mContext);
                    }
                    break;
            }
        }

        return true;
    }

    public boolean canHandleKeyEvent(KeyEvent event) {
        int scanCode = event.getScanCode();

        boolean isFPScanCode = ArrayUtils.contains(sSupportedFPGestures, scanCode);

        if (scanCode == CUSTOM_SCAN_CODE || isFPScanCode) {
            return true;
        }
        return false;
    }

    public boolean isCameraLaunchEvent(KeyEvent event) {
        return false;
    }

    public boolean isWakeEvent(KeyEvent event) {
        return false;
    }

    public boolean isDisabledKeyEvent(KeyEvent event) {
        return false;
    }

    public Intent isActivityLaunchEvent(KeyEvent event) {
        return null;
    }

    private Integer getFPGestureValueForScanCode(int scanCode, boolean screenOn) {
        switch (scanCode) {
            case FP_TAP_SCANCODE:
                return Settings.Global.getInt(mContext.getContentResolver(), screenOn ? FP_KEYS : FP_KEYS_OFF, 0);
            case FP_HOLD_SCANCODE:
                return Settings.Global.getInt(mContext.getContentResolver(), screenOn ? FP_KEY_HOLD : FP_KEY_HOLD_OFF,
                        0);
            case FP_RIGHT_SCANCODE:
                return Settings.Global.getInt(mContext.getContentResolver(), screenOn ? FP_KEY_RIGHT : FP_KEY_RIGHT_OFF,
                        0);
            case FP_LEFT_SCANCODE:
                return Settings.Global.getInt(mContext.getContentResolver(), screenOn ? FP_KEY_LEFT : FP_KEY_LEFT_OFF,
                        0);
        }
        return 0;
    }

    private void ensureKeyguardManager() {
        if (mKeyguardManager == null) {
            mKeyguardManager =
                    (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
        }
    }

    private void toggleScreenState() {
        if (mPowerManager.isScreenOn()) {
            mPowerManager.goToSleep(SystemClock.uptimeMillis());
        } else {
            mPowerManager.wakeUp(SystemClock.uptimeMillis());
        }
    }

    private void fireGoogleNow() {
        mSearchManagerService = ISearchManager.Stub.asInterface(ServiceManager.getService(Context.SEARCH_SERVICE));
        if (mSearchManagerService != null) {
            try {
                mSearchManagerService.launchAssist(new Bundle());
                doHapticFeedback();
            } catch (RemoteException e) {
            }
        }
    }

    private void launchCamera() {
        Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
        if (getBestActivityInfo(intent) != null) {
            mContext.startActivity(intent);
            doHapticFeedback();
        }
    }

    private void launchBrowser() {
        mPowerManager.wakeUp(SystemClock.uptimeMillis(), WAKEUP_REASON);
        final Intent intent = getLaunchableIntent(new Intent(Intent.ACTION_VIEW, Uri.parse("http:")));
        startActivitySafely(intent);
        doHapticFeedback();
    }

    private void launchDialer() {
        mPowerManager.wakeUp(SystemClock.uptimeMillis(), WAKEUP_REASON);
        final Intent intent = new Intent(Intent.ACTION_DIAL, null);
        startActivitySafely(intent);
        doHapticFeedback();
    }

    private void launchEmail() {
        mPowerManager.wakeUp(SystemClock.uptimeMillis(), WAKEUP_REASON);
        final Intent intent = getLaunchableIntent(new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:")));
        startActivitySafely(intent);
        doHapticFeedback();
    }

    private void launchMessages() {
        mPowerManager.wakeUp(SystemClock.uptimeMillis(), WAKEUP_REASON);
        final Intent intent = getLaunchableIntent(new Intent(Intent.ACTION_VIEW, Uri.parse("sms:")));
        startActivitySafely(intent);
        doHapticFeedback();
    }

    private void playPauseMusic() {
        dispatchMediaKeyWithWakeLockToMediaSession(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
        doHapticFeedback();
    }

    private void toggleFlashlight() {
        String rearCameraId = getRearCameraId();
        if (rearCameraId != null) {
            try {
                mCameraManager.setTorchMode(rearCameraId, !mTorchEnabled);
                mTorchEnabled = !mTorchEnabled;
                doHapticFeedback();
            } catch (CameraAccessException e) {
                // Ignore
            }
        }
    }

    private static void dispatchMediaKeyWithWakeLock(int keycode, Context context) {
        if (ActivityManagerNative.isSystemReady()) {
            KeyEvent event = new KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), KeyEvent.ACTION_DOWN,
                    keycode, 0);
            MediaSessionLegacyHelper.getHelper(context).sendMediaButtonEvent(event, true);
            event = KeyEvent.changeAction(event, KeyEvent.ACTION_UP);
            MediaSessionLegacyHelper.getHelper(context).sendMediaButtonEvent(event, true);
        }
    }

    private void dispatchMediaKeyWithWakeLockToMediaSession(final int keycode) {
        final MediaSessionLegacyHelper helper = MediaSessionLegacyHelper.getHelper(mContext);
        if (helper == null) {
            Log.w(TAG, "Unable to send media key event");
            return;
        }
        KeyEvent event = new KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), KeyEvent.ACTION_DOWN,
                keycode, 0);
        helper.sendMediaButtonEvent(event, true);
        event = KeyEvent.changeAction(event, KeyEvent.ACTION_UP);
        helper.sendMediaButtonEvent(event, true);
    }

    private static void switchToLastApp(Context context) {
        final ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.RunningTaskInfo lastTask = getLastTask(context, am);

        if (lastTask != null) {
            am.moveTaskToFront(lastTask.id, ActivityManager.MOVE_TASK_NO_USER_ACTION);
        }
    }

    private static ActivityManager.RunningTaskInfo getLastTask(Context context, final ActivityManager am) {
        final String defaultHomePackage = resolveCurrentLauncherPackage(context);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(5);

        for (int i = 1; i < tasks.size(); i++) {
            String packageName = tasks.get(i).topActivity.getPackageName();
            if (!packageName.equals(defaultHomePackage) && !packageName.equals(context.getPackageName())
                    && !packageName.equals("com.android.systemui")) {
                return tasks.get(i);
            }
        }
        return null;
    }

    private static String resolveCurrentLauncherPackage(Context context) {
        final Intent launcherIntent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
        final PackageManager pm = context.getPackageManager();
        final ResolveInfo launcherInfo = pm.resolveActivity(launcherIntent, 0);
        return launcherInfo.activityInfo.packageName;
    }

    private String getRearCameraId() {
        if (mRearCameraId == null) {
            try {
                for (final String cameraId : mCameraManager.getCameraIdList()) {
                    final CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraId);
                    final int orientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                    if (orientation == CameraCharacteristics.LENS_FACING_BACK) {
                        mRearCameraId = cameraId;
                        break;
                    }
                }
            } catch (CameraAccessException e) {
                // Ignore
            }
        }
        return mRearCameraId;
    }

    private ActivityInfo getBestActivityInfo(Intent intent) {
        PackageManager pm = mContext.getPackageManager();
        ResolveInfo resolveInfo = pm.resolveActivity(intent, 0);
        if (resolveInfo != null) {
            return resolveInfo.activityInfo;
        } else {
            // If the resolving failed, just find our own best match
            return getBestActivityInfo(intent, null);
        }
    }

    private ActivityInfo getBestActivityInfo(Intent intent, ActivityInfo match) {
        PackageManager pm = mContext.getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);
        ActivityInfo best = null;
        if (activities.size() > 0) {
            best = activities.get(0).activityInfo;
            if (match != null) {
                String packageName = match.applicationInfo.packageName;
                for (int i = activities.size() - 1; i >= 0; i--) {
                    ActivityInfo activityInfo = activities.get(i).activityInfo;
                    if (packageName.equals(activityInfo.applicationInfo.packageName)) {
                        best = activityInfo;
                    }
                }
            }
        }
        return best;
    }

    private Intent getLaunchableIntent(Intent intent) {
        PackageManager pm = mContext.getPackageManager();
        List<ResolveInfo> resInfo = pm.queryIntentActivities(intent, 0);
        if (resInfo.isEmpty()) {
            return null;
        }
        return pm.getLaunchIntentForPackage(resInfo.get(0).activityInfo.packageName);
    }

    private void startActivitySafely(Intent intent) {
        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        try {
            UserHandle user = new UserHandle(UserHandle.USER_CURRENT);
            mContext.startActivityAsUser(intent, null, user);
        } catch (ActivityNotFoundException e) {
            // Ignore
        }
    }

    private void doHapticFeedback() {
        if (mVibrator == null || !mVibrator.hasVibrator()) {
            return;
        }

        if (mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_SILENT) {
            mVibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }

    private void triggerVirtualKeypress(final Handler handler, final int keyCode) {
        final InputManager im = InputManager.getInstance();
        long now = SystemClock.uptimeMillis();

        final KeyEvent downEvent = new KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0, 0,
                KeyCharacterMap.VIRTUAL_KEYBOARD, 0, KeyEvent.FLAG_FROM_SYSTEM, InputDevice.SOURCE_CLASS_BUTTON);
        final KeyEvent upEvent = KeyEvent.changeAction(downEvent, KeyEvent.ACTION_UP);

        // add a small delay to make sure everything behind got focus
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                im.injectInputEvent(downEvent, InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
            }
        }, 10);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                im.injectInputEvent(upEvent, InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
            }
        }, 20);
    }
}