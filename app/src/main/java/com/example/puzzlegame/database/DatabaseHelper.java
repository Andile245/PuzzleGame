package com.example.puzzlegame.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.puzzlegame.models.Score;
import com.example.puzzlegame.models.User;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "PuzzleGame.db";
    private static final int DATABASE_VERSION = 1;

    // User Table
    private static final String TABLE_USERS = "users";
    private static final String COL_USER_ID = "id";
    private static final String COL_USERNAME = "username";
    private static final String COL_PASSWORD = "password";
    private static final String COL_EMAIL = "email";
    private static final String COL_CREATED_AT = "created_at";

    // Scores Table
    private static final String TABLE_SCORES = "high_scores";
    private static final String COL_SCORE_ID = "id";
    private static final String COL_SCORE_USER_ID = "user_id";
    private static final String COL_GRID_SIZE = "grid_size";
    private static final String COL_MOVES = "moves";
    private static final String COL_TIME = "time_seconds";
    private static final String COL_DATE = "date_played";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Users Table
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + "("
                + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_USERNAME + " TEXT UNIQUE NOT NULL,"
                + COL_PASSWORD + " TEXT NOT NULL,"
                + COL_EMAIL + " TEXT,"
                + COL_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP" + ")";
        db.execSQL(createUsersTable);

        // Create Scores Table
        String createScoresTable = "CREATE TABLE " + TABLE_SCORES + "("
                + COL_SCORE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_SCORE_USER_ID + " INTEGER,"
                + COL_GRID_SIZE + " INTEGER,"
                + COL_MOVES + " INTEGER,"
                + COL_TIME + " INTEGER,"
                + COL_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY(" + COL_SCORE_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ")" + ")";
        db.execSQL(createScoresTable);

        // Insert a default user for testing
        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, "test");
        values.put(COL_PASSWORD, "test123");
        db.insert(TABLE_USERS, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCORES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // ============ USER OPERATIONS ============

    public boolean registerUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, username);
        values.put(COL_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result != -1;
    }

    public User loginUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COL_USERNAME + "=? AND " + COL_PASSWORD + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{username, password});

        if (cursor.moveToFirst()) {
            User user = new User(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_USERNAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_PASSWORD)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_CREATED_AT))
            );
            cursor.close();
            db.close();
            return user;
        }
        cursor.close();
        db.close();
        return null;
    }

    public boolean checkUserExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COL_USERNAME + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{username});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    // ============ SCORE OPERATIONS ============

    public boolean saveScore(Score score) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_SCORE_USER_ID, score.getUserId());
        values.put(COL_GRID_SIZE, score.getGridSize());
        values.put(COL_MOVES, score.getMoves());
        values.put(COL_TIME, score.getTimeSeconds());

        long result = db.insert(TABLE_SCORES, null, values);
        db.close();
        return result != -1;
    }

    public Score getBestScore(int userId, int gridSize) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_SCORES + " WHERE " + COL_SCORE_USER_ID + "=? AND " + COL_GRID_SIZE + "=? ORDER BY " + COL_MOVES + " ASC, " + COL_TIME + " ASC LIMIT 1";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), String.valueOf(gridSize)});

        if (cursor.moveToFirst()) {
            Score score = new Score(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_SCORE_USER_ID)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_GRID_SIZE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_MOVES)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TIME))
            );
            score.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_SCORE_ID)));
            score.setDatePlayed(cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE)));
            cursor.close();
            db.close();
            return score;
        }
        cursor.close();
        db.close();
        return null;
    }

    public List<Score> getTopScores(int gridSize, int limit) {
        List<Score> scores = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT s.*, u." + COL_USERNAME + " FROM " + TABLE_SCORES + " s " +
                "JOIN " + TABLE_USERS + " u ON s." + COL_SCORE_USER_ID + " = u." + COL_USER_ID + " " +
                "WHERE s." + COL_GRID_SIZE + "=? ORDER BY s." + COL_MOVES + " ASC, s." + COL_TIME + " ASC LIMIT ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(gridSize), String.valueOf(limit)});

        while (cursor.moveToNext()) {
            Score score = new Score(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_SCORE_USER_ID)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_GRID_SIZE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_MOVES)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TIME))
            );
            score.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_SCORE_ID)));
            score.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(COL_USERNAME)));
            score.setDatePlayed(cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE)));
            scores.add(score);
        }
        cursor.close();
        db.close();
        return scores;
    }
}