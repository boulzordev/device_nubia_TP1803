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

import android.app.ISearchManager;
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
import android.media.AudioManager;
import android.media.session.MediaSessionLegacyHelper;
import android.net.Uri;
import android.os.Bundle;
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
import android.view.KeyEvent;

import com.android.internal.util.omni.DeviceKeyHandler;

import java.util.List;

public class KeyHandler implements DeviceKeyHandler {
    private static final String TAG = KeyHandler.class.getSimpleName();

    private static final int CUSTOM_SCAN_CODE = 216;
    private final static String mAssistantKey = "assistant_button";
    private static final String WAKEUP_REASON = "assistant-key";

    private final AudioManager mAudioManager;
    private final CameraManager mCameraManager;
    private final Context mContext;
    private final PowerManager mPowerManager;
    private ISearchManager mSearchManagerService;
    private final Vibrator mVibrator;

    private String mRearCameraId;
    private boolean mTorchEnabled;

    public KeyHandler(Context context) {
        mContext = context;
        mPowerManager = context.getSystemService(PowerManager.class);
        mCameraManager = mContext.getSystemService(CameraManager.class);
        mVibrator = context.getSystemService(Vibrator.class);
        mAudioManager = mContext.getSystemService(AudioManager.class);
    }

    public boolean handleKeyEvent(KeyEvent event) {
        int scanCode = event.getScanCode();

        if (scanCode != CUSTOM_SCAN_CODE || event.getAction() != KeyEvent.ACTION_UP) {
            return false;
        }

        Integer action = Settings.Global.getInt(mContext.getContentResolver(),
                mAssistantKey, 0);

        if (action == 0) {
            return false;
        } else {
            switch (action) {
                case 108:
                    fireGoogleNow();
                    break;
                case 111:
                    launchCamera();
                    break;
                case 110:
                    toggleFlashlight();
                    break;
                case 116:
                    launchBrowser();
                    break;
                case 117:
                    launchDialer();
                    break;
                case 118:
                    launchEmail();
                    break;
                case 119:
                    launchMessages();
                    break;
                case 107:
                    playPauseMusic();
                    break;
            }
        }

        return true;
    }

    public boolean canHandleKeyEvent(KeyEvent event) {
        int scanCode = event.getScanCode();

        if (scanCode == CUSTOM_SCAN_CODE) {
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
        final Intent intent = getLaunchableIntent(
                new Intent(Intent.ACTION_VIEW, Uri.parse("http:")));
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
        final Intent intent = getLaunchableIntent(
                new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:")));
        startActivitySafely(intent);
        doHapticFeedback();
    }

    private void launchMessages() {
        mPowerManager.wakeUp(SystemClock.uptimeMillis(), WAKEUP_REASON);
        final Intent intent = getLaunchableIntent(
                new Intent(Intent.ACTION_VIEW, Uri.parse("sms:")));
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

    private void dispatchMediaKeyWithWakeLockToMediaSession(final int keycode) {
        final MediaSessionLegacyHelper helper = MediaSessionLegacyHelper.getHelper(mContext);
        if (helper == null) {
            Log.w(TAG, "Unable to send media key event");
            return;
        }
        KeyEvent event = new KeyEvent(SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(), KeyEvent.ACTION_DOWN, keycode, 0);
        helper.sendMediaButtonEvent(event, true);
        event = KeyEvent.changeAction(event, KeyEvent.ACTION_UP);
        helper.sendMediaButtonEvent(event, true);
    }

    private String getRearCameraId() {
        if (mRearCameraId == null) {
            try {
                for (final String cameraId : mCameraManager.getCameraIdList()) {
                    final CameraCharacteristics characteristics =
                            mCameraManager.getCameraCharacteristics(cameraId);
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
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
            mVibrator.vibrate(VibrationEffect.createOneShot(50,
                    VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }
}
