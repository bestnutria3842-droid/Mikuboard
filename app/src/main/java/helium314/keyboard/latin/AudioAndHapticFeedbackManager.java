/*
 * Copyright (C) 2012 The Android Open Source Project
 * modified
 * SPDX-License-Identifier: Apache-2.0 AND GPL-3.0-only
 */

package helium314.keyboard.latin;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.view.HapticFeedbackConstants;
import android.view.View;

import helium314.keyboard.event.HapticEvent;
import helium314.keyboard.latin.settings.SettingsValues;

/**
 * Handles audio and haptic feedback.
 */
public final class AudioAndHapticFeedbackManager {
    private AudioManager mAudioManager;
    private Vibrator mVibrator;
    private MediaPlayer mMikuPlayer;
    private Context mContext;

    private SettingsValues mSettingsValues;
    private boolean mSoundOn;
    private boolean mDoNotDisturb;

    private static final AudioAndHapticFeedbackManager sInstance =
            new AudioAndHapticFeedbackManager();

    public static AudioAndHapticFeedbackManager getInstance() {
        return sInstance;
    }

    private AudioAndHapticFeedbackManager() {
        // Intentional empty constructor for singleton.
    }

    public static void init(final Context context) {
        sInstance.initInternal(context);
    }

    private void initInternal(final Context context) {
        mContext = context;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void performHapticAndAudioFeedback(
            final int code,
            final View viewToPerformHapticFeedbackOn,
            final HapticEvent hapticEvent
    ) {
        performHapticFeedback(viewToPerformHapticFeedbackOn, hapticEvent);
        performAudioFeedback(code, hapticEvent);
    }

    public boolean hasVibrator() {
        return mVibrator != null && mVibrator.hasVibrator();
    }

    public void vibrate(final long milliseconds) {
        if (mVibrator == null || milliseconds <= 0) {
            return;
        }
        mVibrator.vibrate(milliseconds);
    }

    private boolean reevaluateIfSoundIsOn() {
        if (mSettingsValues == null
                || !mSettingsValues.mSoundOn
                || mAudioManager == null
                || mDoNotDisturb) {
            return false;
        }
        return mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL;
    }

    public void performAudioFeedback(final int code, final HapticEvent hapticEvent) {
        if (mAudioManager == null) {
            return;
        }

        if (!mSoundOn) {
            return;
        }

        if (hapticEvent != HapticEvent.KEY_PRESS) {
            return;
        }

        // Play the custom Miku sound
        if (mMikuPlayer == null) {
            mMikuPlayer = MediaPlayer.create(mContext, R.raw.miku);
        }

        if (mMikuPlayer != null && !mMikuPlayer.isPlaying()) {
            mMikuPlayer.start();
        }
    }

    public void performHapticFeedback(
            final View viewToPerformHapticFeedbackOn,
            final HapticEvent hapticEvent
    ) {
        if (!mSettingsValues.mVibrateOn
                || (mDoNotDisturb && !mSettingsValues.mVibrateInDndMode)) {
            return;
        }

        if (hapticEvent == HapticEvent.NO_HAPTICS
