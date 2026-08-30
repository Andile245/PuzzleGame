package com.example.puzzlegame.activities;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.puzzlegame.R;
import com.example.puzzlegame.database.DatabaseHelper;
import com.example.puzzlegame.models.Score;
import com.example.puzzlegame.utils.SessionManager;
import com.example.puzzlegame.utils.SoundManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameActivity extends AppCompatActivity {

    private GridLayout gridLayout;
    private TextView tvMoves;
    private TextView tvTimer;
    private TextView tvBestScore;
    private MaterialButton btnShuffle;
    private MaterialButton btnHint;
    private MaterialButton btnSettings;

    private int[] board; // 0 = empty space
    private int gridSize = 4;
    private int emptyIndex = 15;
    private int moves = 0;
    private boolean gameSolved = false;
    private boolean isAnimating = false;

    private int timerSeconds = 0;
    private Handler timerHandler = new Handler();
    private Runnable timerRunnable;
    private boolean isTimerRunning = false;

    private SessionManager sessionManager;
    private DatabaseHelper dbHelper;
    private SoundManager soundManager;
    private SharedPreferences prefs;

    private TextView[] tileViews;
    private int tileSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Initialize managers
        sessionManager = new SessionManager(this);
        dbHelper = new DatabaseHelper(this);
        soundManager = new SoundManager(this);
        prefs = getSharedPreferences("PuzzleGamePrefs", Context.MODE_PRIVATE);

        // Initialize views
        gridLayout = findViewById(R.id.gridLayout);
        tvMoves = findViewById(R.id.tvMoves);
        tvTimer = findViewById(R.id.tvTimer);
        tvBestScore = findViewById(R.id.tvBestScore);
        btnShuffle = findViewById(R.id.btnShuffle);
        btnHint = findViewById(R.id.btnHint);
        btnSettings = findViewById(R.id.btnSettings);

        // Initialize board and game
        initializeBoard();
        setupTimer();
        setupGrid();
        loadBestScore();

        // Apply sound settings
        applySoundSettings();

        // ============ BUTTON LISTENERS ============

        // Shuffle Button
        btnShuffle.setOnClickListener(v -> shufflePuzzle());

        // Hint Button
        btnHint.setOnClickListener(v -> showHint());

        // ============ FIXED SETTINGS BUTTON ============
        btnSettings.setOnClickListener(v -> {
            // Play click sound
            soundManager.playClickSound();

            // Create intent to open SettingsActivity
            Intent intent = new Intent(GameActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // Show instructions on first launch
        if (!prefs.getBoolean("instructions_shown", false)) {
            showInstructions();
            prefs.edit().putBoolean("instructions_shown", true).apply();
        }

        // Start timer
        startTimer();
    }

    // ============ APPLY SOUND SETTINGS ============
    private void applySoundSettings() {
        // Apply music setting
        boolean musicEnabled = sessionManager.isMusicEnabled();
        if (musicEnabled) {
            soundManager.startBackgroundMusic();
        } else {
            soundManager.pauseBackgroundMusic();
        }

        // Apply SFX setting
        boolean sfxEnabled = sessionManager.isSfxEnabled();
        soundManager.toggleSfx(sfxEnabled);
    }

    private void initializeBoard() {
        board = new int[gridSize * gridSize];
        for (int i = 0; i < board.length - 1; i++) {
            board[i] = i + 1;
        }
        board[board.length - 1] = 0;
        emptyIndex = board.length - 1;
        moves = 0;
        gameSolved = false;
        timerSeconds = 0;
        isAnimating = false;
    }

    private void setupTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (!gameSolved && isTimerRunning) {
                    timerSeconds++;
                    updateTimerDisplay();
                    timerHandler.postDelayed(this, 1000);
                }
            }
        };
    }

    private void startTimer() {
        if (!isTimerRunning && !gameSolved) {
            isTimerRunning = true;
            timerHandler.postDelayed(timerRunnable, 1000);
        }
    }

    private void stopTimer() {
        isTimerRunning = false;
        timerHandler.removeCallbacks(timerRunnable);
    }

    private void resetTimer() {
        stopTimer();
        timerSeconds = 0;
        updateTimerDisplay();
        isTimerRunning = false;
    }

    private void updateTimerDisplay() {
        int minutes = timerSeconds / 60;
        int seconds = timerSeconds % 60;
        tvTimer.setText(String.format("Time: %02d:%02d", minutes, seconds));
    }

    private void setupGrid() {
        gridLayout.removeAllViews();
        gridLayout.setColumnCount(gridSize);
        gridLayout.setRowCount(gridSize);

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        tileSize = (screenWidth - 80) / gridSize;

        tileViews = new TextView[board.length];

        for (int position = 0; position < board.length; position++) {
            TextView tile = new TextView(this);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = tileSize;
            params.height = tileSize;
            params.setMargins(3, 3, 3, 3);
            params.rowSpec = GridLayout.spec(position / gridSize);
            params.columnSpec = GridLayout.spec(position % gridSize);
            tile.setLayoutParams(params);

            tile.setGravity(Gravity.CENTER);
            tile.setTextSize(tileSize * 0.35f);
            tile.setTypeface(null, android.graphics.Typeface.BOLD);
            tile.setPadding(4, 4, 4, 4);

            tileViews[position] = tile;
            updateTile(position);

            final int pos = position;
            tile.setOnClickListener(v -> {
                if (!isAnimating) {
                    onTileClick(pos);
                }
            });

            gridLayout.addView(tile);
        }
    }

    private void updateTile(int position) {
        if (position >= tileViews.length || tileViews[position] == null) return;

        TextView tile = tileViews[position];
        int tileNumber = board[position];

        if (tileNumber == 0) {
            tile.setText("");
            tile.setBackgroundColor(Color.LTGRAY);
            tile.setClickable(false);
        } else {
            tile.setText(String.valueOf(tileNumber));
            tile.setClickable(true);
            int hue = ((tileNumber - 1) * 24) % 360;
            int color = android.graphics.Color.HSVToColor(230, new float[]{hue, 0.8f, 0.7f});
            tile.setBackgroundColor(color);
            tile.setTextColor(Color.WHITE);
            tile.setShadowLayer(2, 1, 1, Color.BLACK);
        }
    }

    private void updateAllTiles() {
        for (int i = 0; i < board.length; i++) {
            updateTile(i);
        }
    }

    private void onTileClick(int position) {
        // Don't allow moves if game is solved or animation is running
        if (gameSolved || isAnimating) {
            return;
        }

        // Check if the clicked tile is adjacent to the empty space
        if (!isAdjacentToEmpty(position)) {
            Toast.makeText(this, "❌ Tile must be next to the empty space!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Valid move - animate the tile sliding into the empty space
        isAnimating = true;

        // Get the empty position before swap
        final int oldEmptyIndex = emptyIndex;
        final int tileToMove = position;

        // Animate the tile moving to the empty space position
        animateTileMovement(tileViews[tileToMove], tileViews[oldEmptyIndex], () -> {
            // Swap in board array
            board[oldEmptyIndex] = board[tileToMove];
            board[tileToMove] = 0;
            emptyIndex = tileToMove;

            // Update UI
            updateTile(tileToMove);
            updateTile(oldEmptyIndex);

            // Update move counter
            moves++;
            tvMoves.setText("Moves: " + moves);

            isAnimating = false;

            // Check for win
            if (isSolved()) {
                onPuzzleSolved();
            }

            if (!isTimerRunning) {
                startTimer();
            }
        });
    }

    private void animateTileMovement(View tileToMove, View targetPosition, Runnable onComplete) {
        if (tileToMove == null || targetPosition == null) {
            if (onComplete != null) onComplete.run();
            return;
        }

        // Get the current position of the tile and the target position
        float startX = tileToMove.getX();
        float startY = tileToMove.getY();
        float targetX = targetPosition.getX();
        float targetY = targetPosition.getY();

        // Calculate the difference
        float deltaX = targetX - startX;
        float deltaY = targetY - startY;

        // Animate the tile moving to the target position
        ObjectAnimator moveX = ObjectAnimator.ofFloat(tileToMove, "translationX", 0, deltaX);
        ObjectAnimator moveY = ObjectAnimator.ofFloat(tileToMove, "translationY", 0, deltaY);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(moveX, moveY);
        animatorSet.setDuration(200);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());

        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                // Reset translation
                tileToMove.setTranslationX(0);
                tileToMove.setTranslationY(0);
                if (onComplete != null) onComplete.run();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                tileToMove.setTranslationX(0);
                tileToMove.setTranslationY(0);
                if (onComplete != null) onComplete.run();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });

        animatorSet.start();
    }

    private boolean isAdjacentToEmpty(int position) {
        int emptyRow = emptyIndex / gridSize;
        int emptyCol = emptyIndex % gridSize;
        int tileRow = position / gridSize;
        int tileCol = position % gridSize;

        // Check if adjacent (up, down, left, right) - NOT diagonal
        int rowDiff = Math.abs(tileRow - emptyRow);
        int colDiff = Math.abs(tileCol - emptyCol);

        return (rowDiff == 1 && colDiff == 0) || (rowDiff == 0 && colDiff == 1);
    }

    private boolean isSolved() {
        for (int i = 0; i < board.length - 1; i++) {
            if (board[i] != i + 1) {
                return false;
            }
        }
        return board[board.length - 1] == 0;
    }

    private void shufflePuzzle() {
        if (isAnimating) return;

        if (gameSolved) {
            gameSolved = false;
        }

        // Start from solved state
        for (int i = 0; i < board.length; i++) {
            board[i] = (i < board.length - 1) ? i + 1 : 0;
        }
        emptyIndex = board.length - 1;

        // Perform random valid moves to shuffle (guarantees solvable)
        int shuffleCount = 200;
        int currentEmpty = emptyIndex;

        for (int i = 0; i < shuffleCount; i++) {
            List<Integer> neighbors = getNeighborIndices(currentEmpty);
            Collections.shuffle(neighbors);
            int randomNeighbor = neighbors.get(0);

            board[currentEmpty] = board[randomNeighbor];
            board[randomNeighbor] = 0;
            currentEmpty = randomNeighbor;
        }
        emptyIndex = currentEmpty;

        // Make sure it's not solved
        if (isSolved()) {
            int temp = board[0];
            board[0] = board[1];
            board[1] = temp;
        }

        moves = 0;
        timerSeconds = 0;
        gameSolved = false;
        tvMoves.setText("Moves: 0");
        updateTimerDisplay();

        updateAllTiles();

        resetTimer();
        startTimer();

        soundManager.playClickSound();
        Toast.makeText(this, "🔄 Puzzle shuffled! Tap tiles next to the empty space.", Toast.LENGTH_LONG).show();
    }

    private List<Integer> getNeighborIndices(int position) {
        List<Integer> neighbors = new ArrayList<>();
        int row = position / gridSize;
        int col = position % gridSize;

        if (row > 0) neighbors.add(position - gridSize);
        if (row < gridSize - 1) neighbors.add(position + gridSize);
        if (col > 0) neighbors.add(position - 1);
        if (col < gridSize - 1) neighbors.add(position + 1);

        return neighbors;
    }

    private void showHint() {
        if (gameSolved) {
            Toast.makeText(this, "🎉 Puzzle already solved!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isAnimating) return;

        List<Integer> neighbors = getNeighborIndices(emptyIndex);
        if (!neighbors.isEmpty()) {
            int hintTile = neighbors.get(0);
            if (hintTile < tileViews.length && tileViews[hintTile] != null) {
                View tile = tileViews[hintTile];

                ObjectAnimator pulseX = ObjectAnimator.ofFloat(tile, "scaleX", 1.0f, 1.3f, 1.0f);
                ObjectAnimator pulseY = ObjectAnimator.ofFloat(tile, "scaleY", 1.0f, 1.3f, 1.0f);
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playTogether(pulseX, pulseY);
                animatorSet.setDuration(400);
                animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
                animatorSet.start();
            }

            int tileNumber = board[hintTile];
            Toast.makeText(this, "💡 Tap tile " + tileNumber, Toast.LENGTH_SHORT).show();
        }

        soundManager.playClickSound();
    }

    private void celebrateWin() {
        for (int i = 0; i < tileViews.length; i++) {
            if (tileViews[i] != null) {
                View child = tileViews[i];
                ObjectAnimator rotate = ObjectAnimator.ofFloat(child, "rotation", 0f, 360f);
                ObjectAnimator scaleX = ObjectAnimator.ofFloat(child, "scaleX", 1.0f, 1.2f, 1.0f);
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(child, "scaleY", 1.0f, 1.2f, 1.0f);

                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playTogether(rotate, scaleX, scaleY);
                animatorSet.setDuration(500);
                animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
                animatorSet.setStartDelay(i * 30);
                animatorSet.start();
            }
        }
    }

    private void onPuzzleSolved() {
        gameSolved = true;
        stopTimer();
        soundManager.playWinSound();

        celebrateWin();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🎉 Puzzle Solved!");
        builder.setMessage(
                "Congratulations!\n\n" +
                        "Moves: " + moves + "\n" +
                        "Time: " + timerSeconds + " seconds\n\n" +
                        "Great job!"
        );
        builder.setPositiveButton("Play Again", (dialog, which) -> shufflePuzzle());
        builder.setNegativeButton("Continue", (dialog, which) -> dialog.dismiss());
        builder.setCancelable(true);
        builder.show();

        int userId = sessionManager.getUserId();
        if (userId != -1) {
            Score score = new Score(userId, gridSize, moves, timerSeconds);
            dbHelper.saveScore(score);
            loadBestScore();
        }
    }

    private void loadBestScore() {
        int userId = sessionManager.getUserId();
        if (userId != -1) {
            Score best = dbHelper.getBestScore(userId, gridSize);
            if (best != null) {
                tvBestScore.setText("Best: " + best.getMoves() + " moves (" + best.getTimeSeconds() + "s)");
            } else {
                tvBestScore.setText("Best: --");
            }
        }
    }

    private void showInstructions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🧩 How to Play");
        builder.setMessage(
                "1. Find the empty (gray) space\n" +
                        "2. Tap a tile next to it to move it\n" +
                        "3. Arrange numbers 1 to 15 in order\n" +
                        "4. Solve the puzzle!\n\n" +
                        "💡 Use Shuffle to start a new game\n" +
                        "💡 Use Hint if you get stuck"
        );
        builder.setPositiveButton("Got it!", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        soundManager.pauseBackgroundMusic();
        stopTimer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        applySoundSettings();
        if (!gameSolved && !isTimerRunning && moves > 0) {
            startTimer();
        }
        loadBestScore();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundManager.release();
        stopTimer();
        if (timerHandler != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
    }
}