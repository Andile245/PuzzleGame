package com.example.puzzlegame.utils;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Vibrator;
import android.util.Log;

import com.example.puzzlegame.R;

public class SoundManager {
    private static final String TAG = "SoundManager";
    private MediaPlayer backgroundMusic;
    private SoundPool soundPool;
    private int slideSoundId;
    private int winSoundId;
    private int clickSoundId;

    private boolean isMusicEnabled = true;
    private boolean isSfxEnabled = true;
    private Context context;
    private Vibrator vibrator;

    public SoundManager(Context context) {
        this.context = context;
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        loadSounds();
    }

    private void loadSounds() {
        try {
            Log.d(TAG, "Loading sounds...");

            // Initialize SoundPool
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build();
                soundPool = new SoundPool.Builder()
                        .setMaxStreams(10)
                        .setAudioAttributes(audioAttributes)
                        .build();
            } else {
                soundPool = new SoundPool(10, android.media.AudioManager.STREAM_MUSIC, 0);
            }

            // Load sound effects - check if they exist
            try {
                slideSoundId = soundPool.load(context, R.raw.tile_slide, 1);
                Log.d(TAG, "slideSoundId loaded: " + slideSoundId);
            } catch (Exception e) {
                Log.e(TAG, "Failed to load tile_slide: " + e.getMessage());
                slideSoundId = 0;
            }

            try {
                winSoundId = soundPool.load(context, R.raw.win, 1);
                Log.d(TAG, "winSoundId loaded: " + winSoundId);
            } catch (Exception e) {
                Log.e(TAG, "Failed to load win: " + e.getMessage());
                winSoundId = 0;
            }

            try {
                clickSoundId = soundPool.load(context, R.raw.click, 1);
                Log.d(TAG, "clickSoundId loaded: " + clickSoundId);
            } catch (Exception e) {
                Log.e(TAG, "Failed to load click: " + e.getMessage());
                clickSoundId = 0;
            }

            // Load background music
            try {
                backgroundMusic = MediaPlayer.create(context, R.raw.background_music);
                if (backgroundMusic != null) {
                    backgroundMusic.setLooping(true);
                    backgroundMusic.setVolume(0.5f, 0.5f);
                    Log.d(TAG, "Background music loaded successfully");
                } else {
                    Log.e(TAG, "Background music is null");
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to load background music: " + e.getMessage());
                backgroundMusic = null;
            }

            Log.d(TAG, "Sound loading complete");
        } catch (Exception e) {
            Log.e(TAG, "Error loading sounds: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void playSlideSound() {
        Log.d(TAG, "playSlideSound called - isSfxEnabled: " + isSfxEnabled + ", slideSoundId: " + slideSoundId);
        try {
            if (isSfxEnabled && soundPool != null && slideSoundId != 0) {
                int result = soundPool.play(slideSoundId, 0.8f, 0.8f, 1, 0, 1.0f);
                Log.d(TAG, "Slide sound play result: " + result);
            } else if (vibrator != null) {
                vibrator.vibrate(20);
                Log.d(TAG, "Using vibration fallback for slide");
            } else {
                Log.d(TAG, "No sound or vibration available");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error playing slide sound: " + e.getMessage());
            if (vibrator != null) {
                vibrator.vibrate(20);
            }
        }
    }

    public void playWinSound() {
        Log.d(TAG, "playWinSound called - isSfxEnabled: " + isSfxEnabled + ", winSoundId: " + winSoundId);
        try {
            if (isSfxEnabled && soundPool != null && winSoundId != 0) {
                int result = soundPool.play(winSoundId, 1.0f, 1.0f, 1, 0, 1.0f);
                Log.d(TAG, "Win sound play result: " + result);
            } else if (vibrator != null) {
                vibrator.vibrate(200);
                Log.d(TAG, "Using vibration fallback for win");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error playing win sound: " + e.getMessage());
            if (vibrator != null) {
                vibrator.vibrate(200);
            }
        }
    }

    public void playClickSound() {
        Log.d(TAG, "playClickSound called - isSfxEnabled: " + isSfxEnabled + ", clickSoundId: " + clickSoundId);
        try {
            if (isSfxEnabled && soundPool != null && clickSoundId != 0) {
                int result = soundPool.play(clickSoundId, 0.5f, 0.5f, 1, 0, 1.0f);
                Log.d(TAG, "Click sound play result: " + result);
            } else if (vibrator != null) {
                vibrator.vibrate(10);
                Log.d(TAG, "Using vibration fallback for click");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error playing click sound: " + e.getMessage());
            if (vibrator != null) {
                vibrator.vibrate(10);
            }
        }
    }

    public void startBackgroundMusic() {
        Log.d(TAG, "startBackgroundMusic called - isMusicEnabled: " + isMusicEnabled);
        try {
            if (isMusicEnabled && backgroundMusic != null) {
                if (!backgroundMusic.isPlaying()) {
                    backgroundMusic.start();
                    Log.d(TAG, "Background music started");
                } else {
                    Log.d(TAG, "Background music already playing");
                }
            } else {
                Log.d(TAG, "Music disabled or backgroundMusic is null");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error starting background music: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void pauseBackgroundMusic() {
        try {
            if (backgroundMusic != null && backgroundMusic.isPlaying()) {
                backgroundMusic.pause();
                Log.d(TAG, "Background music paused");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error pausing background music: " + e.getMessage());
        }
    }

    public void stopBackgroundMusic() {
        try {
            if (backgroundMusic != null) {
                if (backgroundMusic.isPlaying()) {
                    backgroundMusic.stop();
                }
                backgroundMusic.release();
                backgroundMusic = null;
                Log.d(TAG, "Background music stopped and released");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error stopping background music: " + e.getMessage());
        }
    }

    public void toggleMusic(boolean enable) {
        isMusicEnabled = enable;
        Log.d(TAG, "Music toggled to: " + enable);
        if (enable) {
            startBackgroundMusic();
        } else {
            pauseBackgroundMusic();
        }
    }

    public void toggleSfx(boolean enable) {
        isSfxEnabled = enable;
        Log.d(TAG, "SFX toggled to: " + enable);
    }

    public boolean isMusicEnabled() {
        return isMusicEnabled;
    }

    public boolean isSfxEnabled() {
        return isSfxEnabled;
    }

    public void release() {
        try {
            if (soundPool != null) {
                soundPool.release();
                soundPool = null;
                Log.d(TAG, "SoundPool released");
            }
            if (backgroundMusic != null) {
                backgroundMusic.release();
                backgroundMusic = null;
                Log.d(TAG, "Background music released");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error releasing sounds: " + e.getMessage());
        }
    }
}