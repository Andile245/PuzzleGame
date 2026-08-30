package com.example.puzzlegame.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PuzzleEngine {
    private int gridSize;
    private Bitmap originalImage;
    private List<Bitmap> tiles;
    private int[] tilePositions;
    private int emptyIndex;
    private int moves;
    private int tileSize;

    public PuzzleEngine(int gridSize, Bitmap image) {
        this.gridSize = gridSize;
        this.originalImage = image;
        this.tiles = new ArrayList<>();
        this.tilePositions = new int[gridSize * gridSize];
        this.moves = 0;

        initializeTiles();
        shufflePuzzle();
    }

    private void initializeTiles() {
        tiles.clear();

        // Scale image to fit grid
        int imageSize = 600;
        Bitmap scaledImage = Bitmap.createScaledBitmap(originalImage, imageSize, imageSize, true);
        tileSize = imageSize / gridSize;

        for (int i = 0; i < gridSize * gridSize; i++) {
            int row = i / gridSize;
            int col = i % gridSize;

            // For the last tile (empty)
            if (i == gridSize * gridSize - 1) {
                // Create blank tile
                Bitmap blankTile = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(blankTile);
                canvas.drawColor(Color.LTGRAY);
                Paint paint = new Paint();
                paint.setColor(Color.GRAY);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(2);
                canvas.drawRect(0, 0, tileSize, tileSize, paint);
                tiles.add(blankTile);
                continue;
            }

            // Extract tile from image
            Bitmap tile = Bitmap.createBitmap(scaledImage, col * tileSize, row * tileSize, tileSize, tileSize);
            tiles.add(tile);
        }

        // Set initial positions
        for (int i = 0; i < tilePositions.length; i++) {
            tilePositions[i] = i;
        }
        emptyIndex = tilePositions.length - 1;
    }

    public void shufflePuzzle() {
        // Shuffle using valid moves to ensure solvability
        int currentEmpty = emptyIndex;

        // Number of shuffles based on grid size
        int shuffleCount = 50 + gridSize * gridSize * 10;

        for (int i = 0; i < shuffleCount; i++) {
            List<Integer> neighbors = getNeighborIndices(currentEmpty);
            Collections.shuffle(neighbors);
            int randomNeighbor = neighbors.get(0);
            swapTiles(currentEmpty, randomNeighbor);
            currentEmpty = randomNeighbor;
        }

        // Make sure puzzle is not already solved
        if (isSolved()) {
            // Swap two tiles to make it unsolved
            swapTiles(0, 1);
            moves = 2;
        }

        moves = 0; // Reset move counter after shuffle
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

    private void swapTiles(int pos1, int pos2) {
        int temp = tilePositions[pos1];
        tilePositions[pos1] = tilePositions[pos2];
        tilePositions[pos2] = temp;

        if (pos1 == emptyIndex) emptyIndex = pos2;
        else if (pos2 == emptyIndex) emptyIndex = pos1;
    }

    public boolean moveTile(int position) {
        List<Integer> neighbors = getNeighborIndices(emptyIndex);

        if (neighbors.contains(position)) {
            swapTiles(emptyIndex, position);
            moves++;
            return true;
        }
        return false;
    }

    public boolean isSolved() {
        for (int i = 0; i < tilePositions.length - 1; i++) {
            if (tilePositions[i] != i) {
                return false;
            }
        }
        return tilePositions[tilePositions.length - 1] == tilePositions.length - 1;
    }

    public Bitmap getTileAt(int position) {
        int tileIndex = tilePositions[position];
        if (tileIndex == -1 || tileIndex >= tiles.size()) {
            // Return blank tile if index is invalid
            Bitmap blankTile = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(blankTile);
            canvas.drawColor(Color.LTGRAY);
            return blankTile;
        }
        return tiles.get(tileIndex);
    }

    public boolean isEmptyTile(int position) {
        return tilePositions[position] == tilePositions.length - 1;
    }

    public int getGridSize() {
        return gridSize;
    }

    public int getMoves() {
        return moves;
    }

    public void resetMoves() {
        this.moves = 0;
    }

    public Bitmap getSolvedImage() {
        return originalImage;
    }

    public int getEmptyIndex() {
        return emptyIndex;
    }

    public void setImage(Bitmap newImage) {
        this.originalImage = newImage;
        initializeTiles();
        shufflePuzzle();
        moves = 0;
    }
}