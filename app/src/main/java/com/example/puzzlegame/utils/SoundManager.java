package com.example.puzzlegame.utils;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Vibrator;

import com.example.puzzlegame.R;

public class SoundManager {
    private MediaPlayer backgroundMusic;
    private SoundPool soundPool;
    private int slideSoundId;
    private int winSoundId;
    private int clickSoundId;

    private boolean isMusicEnabled = true;
    private boolean isSfxEnabled = true;
    private Context context;
    private Vibrator vibrator;
    private boolean isMusicLoaded = false;

    public SoundManager(Context context) {
        this.context = context;
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        loadSounds();
    }

    private void loadSounds() {
        // Initialize SoundPool (for short sound effects)
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

        // Load sound effects from raw folder
        try {
            slideSoundId = soundPool.load(context, R.raw.tile_slide, 1);
            winSoundId = soundPool.load(context, R.raw.win, 1);
            clickSoundId = soundPool.load(context, R.raw.click, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Load background music
        loadBackgroundMusic();
    }

    private void loadBackgroundMusic() {
        try {
            if (backgroundMusic != null) {
                backgroundMusic.release();
                backgroundMusic = null;
            }
            backgroundMusic = MediaPlayer.create(context, R.raw.background_music);
            if (backgroundMusic != null) {
                backgroundMusic.setLooping(true);
                backgroundMusic.setVolume(0.5f, 0.5f);
                isMusicLoaded = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            isMusicLoaded = false;
        }
    }

    public void playSlideSound() {
        if (isSfxEnabled && soundPool != null && slideSoundId != 0) {
            soundPool.play(slideSoundId, 0.8f, 0.8f, 1, 0, 1.0f);
        } else if (vibrator != null) {
            vibrator.vibrate(20);
        }
    }

    public void playWinSound() {
        if (isSfxEnabled && soundPool != null && winSoundId != 0) {
            soundPool.play(winSoundId, 1.0f, 1.0f, 1, 0, 1.0f);
        } else if (vibrator != null) {
            vibrator.vibrate(200);
        }
    }

    public void playClickSound() {
        if (isSfxEnabled && soundPool != null && clickSoundId != 0) {
            soundPool.play(clickSoundId, 0.5f, 0.5f, 1, 0, 1.0f);
        } else if (vibrator != null) {
            vibrator.vibrate(10);
        }
    }

    public void startBackgroundMusic() {
        if (isMusicEnabled) {
            try {
                // Reload music if it was released
                if (backgroundMusic == null) {
                    loadBackgroundMusic();
                }
                if (backgroundMusic != null && !backgroundMusic.isPlaying()) {
                    backgroundMusic.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void pauseBackgroundMusic() {
        try {
            if (backgroundMusic != null && backgroundMusic.isPlaying()) {
                backgroundMusic.pause();
            }
        } catch (Exception e) {
            e.printStackTrace();
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
                isMusicLoaded = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void toggleMusic(boolean enable) {
        isMusicEnabled = enable;
        if (enable) {
            startBackgroundMusic();
        } else {
            pauseBackgroundMusic();
        }
    }

    public void toggleSfx(boolean enable) {
        isSfxEnabled = enable;
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
            }
            if (backgroundMusic != null) {
                backgroundMusic.release();
                backgroundMusic = null;
                isMusicLoaded = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}