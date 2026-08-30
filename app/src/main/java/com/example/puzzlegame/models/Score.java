package com.example.puzzlegame.models;

public class Score {
    private int id;
    private int userId;
    private String username;
    private int gridSize;
    private int moves;
    private int timeSeconds;
    private String datePlayed;

    public Score(int userId, int gridSize, int moves, int timeSeconds) {
        this.userId = userId;
        this.gridSize = gridSize;
        this.moves = moves;
        this.timeSeconds = timeSeconds;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getGridSize() { return gridSize; }
    public void setGridSize(int gridSize) { this.gridSize = gridSize; }

    public int getMoves() { return moves; }
    public void setMoves(int moves) { this.moves = moves; }

    public int getTimeSeconds() { return timeSeconds; }
    public void setTimeSeconds(int timeSeconds) { this.timeSeconds = timeSeconds; }

    public String getDatePlayed() { return datePlayed; }
    public void setDatePlayed(String datePlayed) { this.datePlayed = datePlayed; }
}