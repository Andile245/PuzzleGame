package com.example.puzzlegame.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "PuzzleGamePrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_MUSIC_ENABLED = "musicEnabled";
    private static final String KEY_SFX_ENABLED = "sfxEnabled";
    private static final String KEY_GRID_SIZE = "gridSize";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createLoginSession(int userId, String username) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.apply();
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public int getUserId() {
        return pref.getInt(KEY_USER_ID, -1);
    }

    public String getUsername() {
        return pref.getString(KEY_USERNAME, "");
    }

    public void setMusicEnabled(boolean enabled) {
        editor.putBoolean(KEY_MUSIC_ENABLED, enabled);
        editor.apply();
    }

    public boolean isMusicEnabled() {
        return pref.getBoolean(KEY_MUSIC_ENABLED, true);
    }

    public void setSfxEnabled(boolean enabled) {
        editor.putBoolean(KEY_SFX_ENABLED, enabled);
        editor.apply();
    }

    public boolean isSfxEnabled() {
        return pref.getBoolean(KEY_SFX_ENABLED, true);
    }

    public void setGridSize(int size) {
        editor.putInt(KEY_GRID_SIZE, size);
        editor.apply();
    }

    public int getGridSize() {
        return pref.getInt(KEY_GRID_SIZE, 4);
    }
}