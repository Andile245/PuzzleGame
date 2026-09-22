# Puzzle Game

Puzzle Game is an Android implementation of a sliding-number puzzle. Players log in, choose a difficulty, shuffle the board, and move numbered tiles into the correct order.

## Features

- User registration and login with locally stored accounts
- Three difficulty levels: Easy (3x3), Medium (4x4), and Hard (5x5)
- Solvable shuffled puzzle boards
- Move counter and game timer
- Hint that identifies a valid next tile
- Animated tile movement and completion feedback
- Background music and sound-effect controls
- Locally saved best scores for each player and difficulty
- Logout and persistent session settings

## How to play

1. Launch the app and register an account, or sign in with an existing account.
2. Press **Shuffle** to start a new puzzle.
3. Tap a numbered tile next to the empty gray space to move it.
4. Arrange the tiles in ascending order, leaving the empty space in the final position.
5. Use **Hint** when needed, and open **Settings** to change difficulty or sound preferences.

## Test account

The local database creates a test user on first run:

- Username: `test`
- Password: `test123`

## Requirements

- Android Studio
- Android SDK 34
- JDK 11
- Minimum Android SDK 24

## Running the project

1. Open this folder in Android Studio.
2. Allow Gradle sync to finish.
3. Choose an Android emulator or connected Android device.
4. Run the `app` configuration.

## Technology

- Java
- Android SDK and AndroidX
- Material Components
- SQLite for local users and high scores

## Project structure

- `app/src/main/java` contains activities, models, database access, and game utilities.
- `app/src/main/res` contains layouts, colours, themes, icons, and game audio.
